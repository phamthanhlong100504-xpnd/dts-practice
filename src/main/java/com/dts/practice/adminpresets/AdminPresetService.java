package com.dts.practice.adminpresets;

import com.dts.practice.adminpresets.AdminPresetModels.*;
import com.dts.practice.entity.PresetExamSet;
import com.dts.practice.entity.PresetExamSetQuestion;
import com.dts.practice.enums.PresetExamSetStatus;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.service.CurrentQuestionBank;
import com.dts.practice.service.ExamMatrix;
import com.dts.practice.service.PresetExamSetValidator;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Isolated editorial workspace. No method writes preset_exam_sets, exams, Result or EXP.
 * Publication/activation deliberately disabled until BE02 and EXP cross-version contracts exist.
 */
@Service
public class AdminPresetService {
    private static final String BANK = CurrentQuestionBank.currentVersion();
    private static final String DRAFT_FIELDS = "d.id,d.logical_set_id,d.source_published_set_id,d.code,d.title,"+
        "d.license_class,d.bank_version,d.version_number,d.total_questions,d.duration_minutes,"+
        "d.display_order,COALESCE(r.state,d.status) AS status,d.revision,d.last_validated_revision,d.updated_at";
    private static final String DRAFT_FROM = " FROM admin_preset_drafts d LEFT JOIN admin_preset_reviews r ON r.draft_id=d.id ";
    private static final String PUBLISHED_FIELDS = "p.id,p.logical_set_id,p.source_version_id,"+
        "p.version_number,p.code,p.title,p.license_class,p.bank_version,p.total_questions,"+
        "p.duration_minutes,p.display_order,p.updated_at,"+
        "CASE WHEN a.active_version_id=p.id THEN 'PUBLISHED' ELSE 'ARCHIVED' END AS status";
    private static final String PUBLISHED_FROM = " FROM preset_exam_sets p LEFT JOIN "+
        "admin_preset_active_versions a ON a.logical_set_id=p.logical_set_id ";
    private final JdbcTemplate jdbc;
    private final PresetExamSetValidator validator;
    public AdminPresetService(JdbcTemplate jdbc, PresetExamSetValidator validator) {
        this.jdbc=jdbc;this.validator=validator;
    }

    @Transactional(readOnly=true)
    public Page list(String licenseClass,String bankVersion,String status,String search,int limit,int offset) {
        int take=Math.max(1,Math.min(100,limit));int skip=Math.max(0,offset);
        String like="%"+(search==null?"":search.trim().toLowerCase(Locale.ROOT))+"%";
        List<Summary> combined=new ArrayList<>();
        String draftSql="SELECT "+DRAFT_FIELDS+DRAFT_FROM+" WHERE COALESCE(r.state,'DRAFT')<>'PUBLISHED' AND "+
            "(CAST(? AS text) IS NULL OR license_class=?) AND (CAST(? AS text) IS NULL OR bank_version=?) "+
            "AND (CAST(? AS text) IS NULL OR COALESCE(r.state,d.status)=?) " +
            "AND (lower(d.code) LIKE ? OR lower(d.title) LIKE ?)";
        combined.addAll(jdbc.query(draftSql, DRAFT_MAPPER,
            blankToNull(licenseClass),blankToNull(licenseClass), blankToNull(bankVersion),blankToNull(bankVersion),
            blankToNull(status),blankToNull(status),like,like));
        if(status==null || status.isBlank() || "PUBLISHED".equals(status) || "ARCHIVED".equals(status)) {
            String sql="SELECT "+PUBLISHED_FIELDS+PUBLISHED_FROM+" WHERE "+
                "(CAST(? AS text) IS NULL OR p.license_class=?) AND (CAST(? AS text) IS NULL OR p.bank_version=?) "+
                "AND (CAST(? AS text) IS NULL OR (CASE WHEN a.active_version_id=p.id THEN 'PUBLISHED' ELSE 'ARCHIVED' END)=?) "+
                "AND (lower(p.code) LIKE ? OR lower(p.title) LIKE ?)";
            combined.addAll(jdbc.query(sql, PUBLISHED_MAPPER,
                blankToNull(licenseClass),blankToNull(licenseClass),blankToNull(bankVersion),blankToNull(bankVersion),
                blankToNull(status),blankToNull(status),like,like));
        }
        combined.sort(java.util.Comparator.comparing(Summary::updatedAt).reversed()
            .thenComparing(Summary::id));
        long total=combined.size();
        List<Summary> page=skip>=combined.size()?List.of():
            List.copyOf(combined.subList(skip,Math.min(combined.size(),skip+take)));
        return new Page(total,page,take,skip);
    }

    @Transactional(readOnly=true)
    public Detail detail(UUID id) {
        List<Summary> drafts=jdbc.query("SELECT "+DRAFT_FIELDS+DRAFT_FROM+
            " WHERE d.id=? AND COALESCE(r.state,'DRAFT')<>'PUBLISHED'",
            DRAFT_MAPPER,id);
        if(!drafts.isEmpty()) return new Detail(drafts.getFirst(),draftSources(id));
        List<Summary> old=jdbc.query("SELECT "+PUBLISHED_FIELDS+PUBLISHED_FROM+" WHERE p.id=?",PUBLISHED_MAPPER,id);
        if(old.isEmpty())throw BusinessException.notFound("Preset not found");
        return new Detail(old.getFirst(),jdbc.query(
            "SELECT q.source_question_id FROM preset_exam_set_questions pq "+
            "JOIN questions q ON q.id=pq.question_id "+
            "WHERE pq.preset_exam_set_id=? ORDER BY pq.question_order",
            (rs,i)->rs.getInt(1),id));
    }

    @Transactional
    public Detail create(Create input,UUID actor) {
        String license=license(input.licenseClass());
        String code=checkText(input.code(),32,"code");
        String title=checkText(input.title(),200,"title");
        int display=positive(input.displayOrder(),"displayOrder");
        if(jdbc.queryForObject("SELECT COUNT(*) FROM preset_exam_sets WHERE code=? AND license_class=? "+
                "AND bank_version=?",Long.class,code,license,BANK)>0L)
            throw BusinessException.conflict("Code already exists in the published bank");
        ExamMatrix.Rule rule=ExamMatrix.forClass(license);
        UUID draft=UUID.randomUUID(),logical=UUID.randomUUID();
        jdbc.update("INSERT INTO admin_preset_drafts(id,logical_set_id,code,title,license_class,"+
            "bank_version,version_number,total_questions,duration_minutes,display_order,"+
            "created_by,updated_by) VALUES (?,?,?,?,?,?,1,?,?,?,?,?)",
            draft,logical,code,title,license,BANK,rule.totalQuestions(),rule.durationMinutes(),display,actor,actor);
        audit(draft,actor,"CREATE",null,1);
        return detail(draft);
    }

    /** Clone a real published set or an editorial draft, preserving logicalSetId for EXP reconciliation. */
    @Transactional
    public Detail cloneVersion(UUID source,UUID actor) {
        Detail origin=detail(source);
        Summary s=origin.metadata();
        if(!BANK.equals(s.bankVersion()))throw BusinessException.conflict("Inactive bank cannot be cloned");
        if(!"PUBLISHED".equals(s.status()) && !"DRAFT".equals(s.status()))
            throw BusinessException.conflict("Only published or draft presets can be cloned");
        UUID logical=s.logicalSetId();
        // Serialize version allocation for one stable logicalSetId.
        jdbc.query("SELECT pg_advisory_xact_lock(hashtextextended(?::text,0))",rs->{},logical.toString());
        Integer next=jdbc.queryForObject(
            "SELECT COALESCE(MAX(version_number),0)+1 FROM admin_preset_drafts WHERE logical_set_id=?",
            Integer.class,logical);
        // The existing published set is version 1; the first clone must be V2.
        next=Math.max(2,next==null?2:next);
        UUID draft=UUID.randomUUID();
        jdbc.update("INSERT INTO admin_preset_drafts(id,logical_set_id,source_published_set_id,"+
            "code,title,license_class,bank_version,version_number,total_questions,duration_minutes,"+
            "display_order,created_by,updated_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
            draft,logical,"PUBLISHED".equals(s.status())?s.id():s.sourcePublishedSetId(),
            s.code(),s.title(),s.licenseClass(),s.bankVersion(),
            next,s.totalQuestions(),s.durationMinutes(),s.displayOrder(),actor,actor);
        if(!origin.sourceQuestionIds().isEmpty())saveEntries(draft,s.bankVersion(),s.licenseClass(),origin.sourceQuestionIds());
        audit(draft,actor,"CLONE",null,1);
        return detail(draft);
    }

    @Transactional
    public Detail edit(UUID id,Edit patch,UUID actor) {
        Summary old=lockDraft(id,patch.expectedRevision());
        requireEditable(old);
        String code=checkText(patch.code(),32,"code");
        String title=checkText(patch.title(),200,"title");
        int order=positive(patch.displayOrder(),"displayOrder");
        jdbc.update("UPDATE admin_preset_drafts SET code=?,title=?,display_order=?,"+
            "revision=revision+1,last_validated_revision=NULL,updated_by=?,updated_at=NOW() WHERE id=?",
            code,title,order,actor,id);
        audit(id,actor,"EDIT",old.revision(),old.revision()+1);
        return detail(id);
    }

    @Transactional
    public Detail questions(UUID id,Questions request,UUID actor) {
        Summary old=lockDraft(id,request.expectedRevision());
        requireEditable(old);
        List<Integer> sources=Objects.requireNonNull(request.sourceQuestionIds(),"sourceQuestionIds");
        if(sources.size()>60 || new HashSet<>(sources).size()!=sources.size() ||
            sources.stream().anyMatch(x->x==null || x<1 || x>600))
            throw BusinessException.badRequest("Duplicate, invalid or too many source question IDs");
        // Draft may be incomplete while editing, but every selected source question must be eligible.
        jdbc.update("DELETE FROM admin_preset_draft_questions WHERE draft_id=?",id);
        saveEntries(id,old.bankVersion(),old.licenseClass(),sources);
        jdbc.update("UPDATE admin_preset_drafts SET revision=revision+1,"+
            "last_validated_revision=NULL,updated_by=?,updated_at=NOW() WHERE id=?",actor,id);
        audit(id,actor,"QUESTIONS",old.revision(),old.revision()+1);
        return detail(id);
    }

    @Transactional
    public Validation validate(UUID id,UUID actor) {
        Summary summary=lockDraftAny(id);
        requireEditable(summary);
        PresetExamSet set=PresetExamSet.builder().id(id).code(summary.code())
            .licenseClass(summary.licenseClass()).bankVersion(summary.bankVersion())
            .totalQuestions(summary.totalQuestions()).durationMinutes(summary.durationMinutes())
            .status(PresetExamSetStatus.DRAFT).build();
        List<PresetExamSetQuestion> entries=jdbc.query(
            "SELECT question_order,question_id FROM admin_preset_draft_questions WHERE draft_id=? ORDER BY question_order",
            (rs,i)->PresetExamSetQuestion.builder().presetExamSetId(id)
                .questionOrder(rs.getInt("question_order")).questionId(rs.getInt("question_id")).build(),id);
        var result=validator.validateDraft(set,entries);
        jdbc.update("UPDATE admin_preset_drafts SET revision=revision+1,"+
            "last_validated_revision=revision+1,updated_by=?,updated_at=NOW() WHERE id=?",actor,id);
        audit(id,actor,"VALIDATE",summary.revision(),summary.revision()+1);
        return new Validation(id,summary.revision()+1,true,result.questions().size(),1,
            "REVIEW_AND_RELEASE_CONTRACT_NOT_IMPLEMENTED",false);
    }

    /** Read-only catalog for manual selection, exposing stable source IDs only. */
    @Transactional(readOnly=true)
    public QuestionPage availableQuestions(String licenseClass,Integer chapter,Integer sourceId,
                                           String search,int limit,int offset) {
        String lic=license(licenseClass);
        int take=Math.max(1,Math.min(100,limit)),skip=Math.max(0,offset);
        if(chapter!=null && (chapter<1 || chapter>6))
            throw BusinessException.badRequest("Invalid chapter");
        if(sourceId!=null && (sourceId<1 || sourceId>600))
            throw BusinessException.badRequest("Invalid sourceQuestionId");
        String where=" WHERE bank_version=? AND jsonb_exists(applicable_licenses,?) "+
            "AND (CAST(? AS integer) IS NULL OR chapter=?) "+
            "AND (CAST(? AS integer) IS NULL OR source_question_id=?) "+
            "AND lower(question_text) LIKE ?";
        String like="%"+(search==null?"":search.trim().toLowerCase(Locale.ROOT))+"%";
        long total=jdbc.queryForObject("SELECT count(*) FROM questions"+where,Long.class,
            BANK,lic,chapter,chapter,sourceId,sourceId,like);
        List<SelectableQuestion> items=jdbc.query(
            "SELECT source_question_id,chapter,question_text,media_file_id, "+
            "(is_critical AND jsonb_exists(COALESCE(critical_licenses,'[]'::jsonb),?)) AS critical_here "+
            "FROM questions"+where+" ORDER BY source_question_id LIMIT ? OFFSET ?",
            (rs,i)->new SelectableQuestion(rs.getInt("source_question_id"),rs.getInt("chapter"),
                rs.getString("question_text"),rs.getBoolean("critical_here"),
                rs.getObject("media_file_id")!=null),
            lic,BANK,lic,chapter,chapter,sourceId,sourceId,like,take,skip);
        return new QuestionPage(total,items,take,skip);
    }

    @Transactional(readOnly=true)
    public Matrix matrix(String licenseClass) {
        String license=license(licenseClass);
        ExamMatrix.Rule rule=ExamMatrix.forClass(license);
        return new Matrix(license,BANK,null,rule.totalQuestions(),rule.durationMinutes(),
            rule.requiredCorrect(),1,new TreeMap<>(rule.ordinaryByChapter()),
            null,null,false,false);
    }

    private static String blankToNull(String value){return value==null||value.isBlank()?null:value.trim();}
    private static String checkText(String value,int max,String name){
        if(value==null||value.isBlank()||value.strip().length()>max)
            throw BusinessException.badRequest("Invalid "+name);
        return value.strip();
    }
    private static int positive(Integer number,String label){
        if(number==null||number<1)throw BusinessException.badRequest("Invalid "+label);
        return number;
    }
    private static String license(String input){
        if(input==null)throw BusinessException.badRequest("licenseClass required");
        String normalized=input.strip().toUpperCase(Locale.ROOT);
        ExamMatrix.forClass(normalized);
        return normalized;
    }

    private void saveEntries(UUID id,String bank,String license,List<Integer> sources) {
        for(int i=0;i<sources.size();i++) {
            Integer source=sources.get(i);
            List<Integer> matched=jdbc.query("SELECT id FROM questions WHERE bank_version=? "+
                "AND source_question_id=? AND jsonb_exists(applicable_licenses,?)",
                (rs,row)->rs.getInt(1),bank,source,license);
            if(matched.size()!=1)throw BusinessException.conflict(
                "Source question "+source+" is not eligible in bank/license");
            jdbc.update("INSERT INTO admin_preset_draft_questions"+
                "(draft_id,question_order,question_id,source_question_id) VALUES(?,?,?,?)",
                id,i+1,matched.getFirst(),source);
        }
    }
    private List<Integer> draftSources(UUID id){
        return jdbc.query("SELECT source_question_id FROM admin_preset_draft_questions "+
            "WHERE draft_id=? ORDER BY question_order",(rs,i)->rs.getInt(1),id);
    }
    private static void requireEditable(Summary draft){
        if(!"DRAFT".equals(draft.status()) && !"REJECTED".equals(draft.status()))
            throw BusinessException.conflict("Submitted or published preset cannot be edited");
    }
    private Summary lockDraft(UUID id,long revision){
        Summary draft=lockDraftAny(id);
        if(draft.revision()!=revision)throw BusinessException.conflict(
            "Stale draft revision; reload before editing");
        return draft;
    }
    private Summary lockDraftAny(UUID id){
        List<Summary> drafts=jdbc.query("SELECT "+DRAFT_FIELDS+DRAFT_FROM+
            " WHERE d.id=? FOR UPDATE OF d",DRAFT_MAPPER,id);
        if(drafts.isEmpty())throw BusinessException.notFound("Editable preset draft not found");
        return drafts.getFirst();
    }
    private void audit(UUID draft,UUID actor,String action,Long before,long after){
        jdbc.update("INSERT INTO admin_preset_draft_audit(draft_id,actor_id,action,"+
            "old_revision,new_revision) VALUES (?,?,?,?,?)",draft,actor,action,before,after);
    }
    private static final RowMapper<Summary> DRAFT_MAPPER=(rs,i)->{
        long rev=rs.getLong("revision");
        long validated=rs.getLong("last_validated_revision");
        boolean valid=!rs.wasNull() && validated==rev;
        return new Summary((UUID)rs.getObject("id"),(UUID)rs.getObject("logical_set_id"),
            (UUID)rs.getObject("source_published_set_id"),rs.getString("code"),rs.getString("title"),
            rs.getString("license_class"),rs.getString("bank_version"),rs.getInt("version_number"),
            rs.getInt("total_questions"),rs.getInt("duration_minutes"),rs.getInt("display_order"),
            rs.getString("status"),rev,valid?"VALID":"NEEDS_VALIDATION",
            rs.getTimestamp("updated_at").toInstant(),valid && "APPROVED".equals(rs.getString("status")));
    };
    private static final RowMapper<Summary> PUBLISHED_MAPPER=(rs,i)->new Summary(
        (UUID)rs.getObject("id"),(UUID)rs.getObject("logical_set_id"),
        (UUID)rs.getObject("source_version_id"),rs.getString("code"),rs.getString("title"),
        rs.getString("license_class"),rs.getString("bank_version"),rs.getInt("version_number"),
        rs.getInt("total_questions"),rs.getInt("duration_minutes"),rs.getInt("display_order"),
        rs.getString("status"),0,"IMMUTABLE",rs.getTimestamp("updated_at").toInstant(),false);
}
