package com.dts.practice.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.datasource.AbstractDataSource;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/** Native Flyway runner. No Spring application, clean, repair, baseline or older migration execution. */
public final class SimulationExamMigrationTool {
    private SimulationExamMigrationTool() {}
    public static void main(String[] args) throws Exception {
        var json=new ObjectMapper().findAndRegisterModules();
        try {
            if(args.length!=1 || !Set.of("check","migrate").contains(args[0]))throw new IllegalArgumentException();
            String url=System.getenv("DTS_SIMULATION_DB_URL"),user=System.getenv("DTS_SIMULATION_DB_USER"),password=System.getenv("DTS_SIMULATION_DB_PASSWORD");
            if(url==null || !url.matches("jdbc:postgresql://127\\.0\\.0\\.1:[0-9]{1,5}/dts_practice") || user==null || password==null)throw new IllegalArgumentException();
            DataSource source=new AbstractDataSource() {
                @Override public Connection getConnection() throws SQLException {return DriverManager.getConnection(url,user,password);}
                @Override public Connection getConnection(String u,String p)throws SQLException {return getConnection();}
            };
            System.out.println(json.writeValueAsString(execute(source,args[0])));
        } catch(Exception e) {
            System.out.println(json.writeValueAsString(Map.of("Passed",false,"ErrorType",e.getClass().getSimpleName(),"Message","Migration guard or Flyway validation failed; no credentials were logged")));
            System.exit(1);
        }
    }
    static Map<String,Object> execute(DataSource source,String mode) throws Exception {
        if(!Set.of("check","migrate").contains(mode))throw new IllegalArgumentException();
        try(var guard=source.getConnection()) {
            if(!"dts_practice".equals(scalar(guard,"SELECT current_database()")) || !"public".equals(scalar(guard,"SELECT current_schema()")))throw new IllegalStateException("Wrong database/schema");
            if(mode.equals("check"))guard.setReadOnly(true);else try(var s=guard.createStatement()){s.execute("SELECT pg_advisory_lock(1146377027,32)");}
            var before=history(guard);boolean installed=shape(guard,before);var protectedBefore=protectedData(guard);
            DataSource readonly=new AbstractDataSource() {
                @Override public Connection getConnection() throws SQLException {var c=source.getConnection();c.setReadOnly(true);return c;}
                @Override public Connection getConnection(String u,String p)throws SQLException {return getConnection();}
            };
            flyway(readonly,installed?"32":"31").validate();
            int count=0;
            if(mode.equals("migrate")) {
                var result=flyway(source,"32").migrate();count=result.migrationsExecuted;
                if(count!=(installed?0:1) || (count==1 && !"32".equals(result.migrations.getFirst().version)))throw new IllegalStateException("Unexpected migration scope");
            }
            var after=history(guard);boolean finalInstalled=shape(guard,after);
            if(!protectedBefore.equals(protectedData(guard)))throw new IllegalStateException("Existing theory or learning tables changed");
            var oldBefore=before.stream().filter(r->!"32".equals(r.get("version"))).toList();var oldAfter=after.stream().filter(r->!"32".equals(r.get("version"))).toList();
            if(!oldBefore.equals(oldAfter) || (mode.equals("check")&&!before.equals(after)) || (mode.equals("migrate")&&!finalInstalled))throw new IllegalStateException("History changed outside V32");
            var report=new LinkedHashMap<String,Object>();report.put("Mode",mode);report.put("Passed",true);report.put("DatabaseName","dts_practice");report.put("Schema","public");
            report.put("HistoryRowsBefore",before.size());report.put("HistoryRowsAfter",after.size());report.put("MigrationsExecuted",count);report.put("Version32Recorded",finalInstalled);
            report.put("ExistingMigrationsValidated",true);report.put("ExistingTheoryAndLearningRowsUnchanged",true);report.put("ExistingHistoryRowsUnchanged",true);report.put("ReadOnlyValidationConnection",true);
            report.put("OlderMigrationsExecuted",false);report.put("BaselineRepairOrCleanRun",false);report.put("SpringApplicationStarted",false);return report;
        }
    }
    static Flyway flyway(DataSource source,String target) {
        return Flyway.configure().dataSource(source).locations("classpath:db/migration").target(target).ignoreMigrationPatterns("*:pending").outOfOrder(false).baselineOnMigrate(false).cleanDisabled(true).load();
    }
    private static boolean shape(Connection c,List<Map<String,Object>> rows)throws Exception {
        Set<String> expected=new HashSet<>();for(int i=1;i<=31;i++)expected.add(String.valueOf(i));expected.add("27.1");
        boolean installed=rows.stream().anyMatch(r->"32".equals(r.get("version")));if(installed)expected.add("32");
        Set<String> actual=new HashSet<>();int rank=0;
        for(var r:rows) {
            String version=Objects.toString(r.get("version"),"");
            if(!actual.add(version) || !"SQL".equals(r.get("type")) || !Boolean.TRUE.equals(r.get("success")) || r.get("checksum")==null
                    || ((Number)r.get("installed_rank")).intValue()!=++rank)throw new IllegalStateException("Invalid migration history");
        }
        if(!actual.equals(expected))throw new IllegalStateException("Expected complete V1-V31 plus V27.1, optionally V32");
        for(String name:List.of("simulation_exam_sessions","simulation_exam_positions","simulation_exam_events","simulation_exam_results")) {
            if((scalar(c,"SELECT to_regclass('public."+name+"')")!=null)!=installed)throw new IllegalStateException("Partial simulation schema");
        }
        for(String name:List.of("simulation_attempts","simulation_attempt_events","simulation_bookmarks","simulation_cursors"))if(scalar(c,"SELECT to_regclass('public."+name+"')")==null)throw new IllegalStateException("V31 tables required");
        return installed;
    }
    private static Map<String,String> protectedData(Connection c)throws SQLException {
        var tables=new ArrayList<String>();try(var s=c.createStatement();var r=s.executeQuery("SELECT tablename FROM pg_tables WHERE schemaname='public' AND tablename NOT LIKE 'simulation_exam_%' AND tablename<>'flyway_schema_history' ORDER BY tablename")){while(r.next())tables.add(r.getString(1));}
        var result=new TreeMap<String,String>();for(String table:tables){if(!table.matches("[a-z_]+"))throw new IllegalStateException("Unexpected table name");
            result.put(table,Objects.toString(scalar(c,"SELECT count(*)::text||':'||md5(coalesce(string_agg(to_jsonb(t)::text,E'\\n' ORDER BY to_jsonb(t)::text),'')) FROM "+table+" t")));}
        return result;
    }
    private static Object scalar(Connection c,String sql)throws SQLException {try(var s=c.createStatement();var r=s.executeQuery(sql)){r.next();return r.getObject(1);}}
    private static List<Map<String,Object>> history(Connection c)throws SQLException {
        var result=new ArrayList<Map<String,Object>>();
        try(var s=c.createStatement();var r=s.executeQuery("SELECT installed_rank,version,description,type,script,checksum,installed_by,installed_on,execution_time,success FROM public.flyway_schema_history ORDER BY installed_rank")) {
            while(r.next()) {var row=new LinkedHashMap<String,Object>();for(int i=1;i<=10;i++)row.put(r.getMetaData().getColumnLabel(i),r.getObject(i));result.add(row);}
        }
        return result;
    }
}
