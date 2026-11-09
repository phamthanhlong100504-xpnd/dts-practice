#!/usr/bin/env python3
"""Run reviewed V32 via native Flyway outside the repository. No source/Git writes."""
import argparse,hashlib,json,os,re,shutil,subprocess,sys,tempfile,xml.etree.ElementTree as ET
from pathlib import Path

SQL='src/main/resources/db/migration/V32__add_simulation_exam_sessions.sql'
SQL_SHA='7fa228a16786d3f39730ce4109910cb1ae9b496c8076629f37f53d0d214a3c14'
CLASS='com.dts.practice.simulation.SimulationExamMigrationTool'
TEST='com.dts.practice.simulation.SimulationExamMigrationToolPostgresTest'
def sha(data):return hashlib.sha256(data).hexdigest()
def normalized(data):return sha(data.replace(b'\r\n',b'\n'))
def git(root,*args):
    p=subprocess.run(['git','--no-optional-locks','-C',str(root),*args],capture_output=True)
    if p.returncode:raise RuntimeError('Git read failed: '+args[0])
    return p.stdout
def files(root):
    result=[]
    for folder in ('src/main','src/test','.mvn','tools/simulation-exam-migration'):
        for p in (root/folder).rglob('*'):
            if p.is_symlink():raise RuntimeError('Source symlink refused')
            if p.is_file():result.append(p)
    for n in ('pom.xml','mvnw','mvnw.cmd'):
        p=root/n
        if p.is_file():result.append(p)
    return sorted(result)
def snapshot(root):
    state={p.relative_to(root).as_posix():sha(p.read_bytes()) for p in files(root)}
    for name,args in {'Head':('rev-parse','HEAD'),'Refs':('for-each-ref','--format=%(refname) %(objectname)'),'Index':('ls-files','--stage','-z')}.items():state['Git'+name]=sha(git(root,*args))
    for name in ('config','index'):
        p=Path(git(root,'rev-parse','--git-path',name).decode().strip());p=p if p.is_absolute() else root/p
        state['GitFile'+name]=sha(p.read_bytes()) if p.is_file() else None
    return state
def command(args,cwd,log,env=None):
    with log.open('a',encoding='utf-8') as output:return subprocess.run(args,cwd=cwd,stdout=output,stderr=subprocess.STDOUT,env=env).returncode
def tested(audit):
    path=audit/'source/target/surefire-reports'/('TEST-'+TEST+'.xml')
    if not path.is_file():return False
    r=ET.parse(path).getroot();return int(r.get('tests','0'))>=5 and all(int(r.get(k,'-1'))==0 for k in ('failures','errors','skipped'))
def compiled_copy(root,args,folder):
    if args.compiled_audit:
        audit=Path(args.compiled_audit).resolve();result=json.loads((audit/'result.json').read_text())
        if not args.expected_payload or not(result.get('Passed')is True and result.get('Candidate')is True and result.get('Suite')=='all' and result.get('MavenExit')==0
                and result.get('PayloadFingerprint')==args.expected_payload and result.get('OriginalSourceAndGitUnchanged')is True and tested(audit)):
            raise RuntimeError('Matching compiled candidate and migration-tool PostgreSQL tests required')
        staged=audit/'source'
        for p in files(root):
            name=p.relative_to(root);q=staged/name
            if not q.is_file():raise RuntimeError('Compiled candidate source missing: '+name.as_posix())
            data=p.read_bytes()
            if name.as_posix()=='pom.xml':data=data.decode('utf-8-sig').replace('<testFailureIgnore>true</testFailureIgnore>','<testFailureIgnore>false</testFailureIgnore>').encode('utf-8')
            if normalized(data)!=normalized(q.read_bytes()):raise RuntimeError('Compiled candidate source differs: '+name.as_posix())
        if not (staged/'target/test-classes'/Path(CLASS.replace('.','/')+'.class')).is_file():raise RuntimeError('Compiled migration tool missing')
        return staged
    staged=folder/'source'
    for p in files(root):
        q=staged/p.relative_to(root);q.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,q)
    pom=staged/'pom.xml';data=pom.read_text(encoding='utf-8-sig');pom.write_text(data.replace('<testFailureIgnore>true</testFailureIgnore>','<testFailureIgnore>false</testFailureIgnore>'),encoding='utf-8')
    config=staged/'src/test/resources/docker-java.properties'
    if config.exists():raise RuntimeError('Existing Docker test override refused')
    config.parent.mkdir(parents=True,exist_ok=True);config.write_text('api.version=1.44\n')
    mvn=shutil.which('mvn.cmd') or shutil.which('mvn')
    if not mvn:raise RuntimeError('Maven is required')
    if command([mvn,'-B','-f',str(pom),'-Dtest='+TEST,'-Dmaven.test.failure.ignore=false','-Dapi.version=1.44','test'],staged,folder/'maven.log')!=0 or not tested(folder):
        raise RuntimeError('Standalone migration-tool PostgreSQL tests failed; inspect maven.log')
    return staged
def credentials(container):
    p=subprocess.run(['docker','inspect',container],capture_output=True)
    if p.returncode:raise RuntimeError('Database container inspection failed')
    rows=json.loads(p.stdout)
    if len(rows)!=1 or not rows[0].get('State',{}).get('Running'):raise RuntimeError('Database container is not running')
    record=rows[0];values={}
    for raw in record.get('Config',{}).get('Env',[]):
        key,_,value=raw.partition('=')
        if key in {'POSTGRES_DB','POSTGRES_USER','POSTGRES_PASSWORD'}:values[key]=value
    ports={v['HostPort'] for v in record.get('NetworkSettings',{}).get('Ports',{}).get('5432/tcp',[])}
    if values.get('POSTGRES_DB')!='dts_practice' or not values.get('POSTGRES_USER') or not values.get('POSTGRES_PASSWORD') or len(ports)!=1 or not re.fullmatch('[0-9]{1,5}',next(iter(ports))):
        raise RuntimeError('Expected dts_practice PostgreSQL container with one published port and configured credentials')
    return 'jdbc:postgresql://127.0.0.1:'+next(iter(ports))+'/dts_practice',values['POSTGRES_USER'],values['POSTGRES_PASSWORD']
def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--root',required=True);parser.add_argument('--audit-root',required=True)
    parser.add_argument('--mode',choices=['check','migrate'],required=True);parser.add_argument('--check-audit');parser.add_argument('--compiled-audit');parser.add_argument('--expected-payload')
    parser.add_argument('--container',default='dts-practice-postgres');args=parser.parse_args()
    root=Path(args.root).resolve();audit=Path(args.audit_root).resolve()
    if Path(git(root,'rev-parse','--show-toplevel').decode().strip()).resolve()!=root:raise RuntimeError('Use the repository root')
    if audit==root or root in audit.parents:raise RuntimeError('Audit directory must be outside the repository')
    if normalized((root/SQL).read_bytes())!=SQL_SHA:raise RuntimeError('V32 SQL differs from the reviewed migration')
    before=snapshot(root)
    if args.mode=='migrate':
        if not args.check_audit:raise RuntimeError('A passing --check-audit is required')
        check=Path(args.check_audit).resolve();prior=json.loads((check/'result.json').read_text())
        if not(prior.get('Passed')is True and prior.get('Mode')=='check' and prior.get('Root')==str(root) and prior.get('MigrationSha256')==SQL_SHA
               and prior.get('OriginalSourceAndGitUnchanged')is True and json.loads((check/'source-state.json').read_text())==before):raise RuntimeError('Check audit does not match the current source')
    audit.mkdir(parents=True,exist_ok=True);folder=Path(tempfile.mkdtemp(prefix='simulation-v32-'+args.mode+'-',dir=audit))
    result={'Mode':args.mode,'Passed':False,'Root':str(root),'MigrationSha256':SQL_SHA,'AuditPath':str(folder),'DatabaseMigrationRequested':args.mode=='migrate',
            'LiveDatabaseExecutionStarted':False,'DatabaseMayHaveChanged':False,'PasswordLogged':False,'ServicesRestarted':False,'GitMutationsRun':False,'CommitOrPushRun':False}
    try:
        staged=compiled_copy(root,args,folder)
        mvn=shutil.which('mvn.cmd') or shutil.which('mvn')
        if not mvn:raise RuntimeError('Maven is required')
        cp=folder/'classpath.txt'
        if command([mvn,'-q','-B','-f',str(staged/'pom.xml'),'dependency:build-classpath','-Dmdep.includeScope=test','-Dmdep.outputFile='+str(cp)],staged,folder/'maven.log')!=0 or not cp.is_file():raise RuntimeError('Cannot resolve test runtime classpath')
        java=Path(os.environ.get('JAVA_HOME',''))/'bin'/('java.exe' if os.name=='nt' else 'java')
        executable=str(java) if java.is_file() else shutil.which('java')
        if not executable:raise RuntimeError('Java 21 is required')
        url,user,password=credentials(args.container);env=dict(os.environ);env.update(DTS_SIMULATION_DB_URL=url,DTS_SIMULATION_DB_USER=user,DTS_SIMULATION_DB_PASSWORD=password)
        classpath=os.pathsep.join([str(staged/'target/test-classes'),str(staged/'target/classes'),cp.read_text().strip()])
        result['LiveDatabaseExecutionStarted']=True;result['DatabaseMayHaveChanged']=args.mode=='migrate'
        try:code=command([executable,'-cp',classpath,CLASS,args.mode],staged,folder/'migration.log',env)
        finally:password=None;env.pop('DTS_SIMULATION_DB_PASSWORD',None)
        native=None
        for line in reversed((folder/'migration.log').read_text(encoding='utf-8',errors='replace').splitlines()):
            try:
                value=json.loads(line)
                if isinstance(value,dict) and 'Passed' in value:native=value;break
            except ValueError:pass
        result.update(JavaExit=code,DatabaseResult=native,Passed=code==0 and native is not None and native.get('Passed')is True)
    except Exception as e:result['Message']=str(e) if isinstance(e,RuntimeError) else 'Migration runner failed; inspect sanitized logs'
    finally:
        result['OriginalSourceAndGitUnchanged']=before==snapshot(root);result['Passed']=result['Passed'] and result['OriginalSourceAndGitUnchanged']
        (folder/'source-state.json').write_text(json.dumps(before,sort_keys=True),encoding='utf-8');(folder/'result.json').write_text(json.dumps(result,indent=2),encoding='utf-8');print(json.dumps(result,indent=2))
    return 0 if result['Passed'] else 1
if __name__=='__main__':
    try:raise SystemExit(main())
    except Exception as e:print('STOP: '+str(e),file=sys.stderr);raise SystemExit(1)
