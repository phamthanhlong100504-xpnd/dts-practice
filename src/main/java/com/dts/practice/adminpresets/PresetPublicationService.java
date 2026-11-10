package com.dts.practice.adminpresets;

import com.dts.practice.adminpresets.AdminPresetModels.*;
import com.dts.practice.entity.PresetExamSet;
import com.dts.practice.entity.PresetExamSetQuestion;
import com.dts.practice.enums.PresetExamSetStatus;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.service.CurrentQuestionBank;
import com.dts.practice.service.PresetExamSetValidator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Approval is bound to the exact draft revision AND its immutable source question content.
 * Publication, the active pointer and audit commit in one Practice transaction.
 * The legacy published rows and Result EXP rules are never rewritten.
 */
@Service
public class PresetPublicationService {
    private final JdbcTemplate jdbc;
    private final PresetExamSetValidator validator;
    private static final String DRAFT_SQL = "SELECT id,logical_set_id,source_published_set_id,code,title,license_class,"+
        "bank_version,version_number,total_questions,duration_minutes,display_order,revision,"+
        "last_validated_revision,created_by,updated_by FROM admin_preset_drafts WHERE id=? FOR UPDATE";

    public PresetPublicationService(JdbcTemplate jdbc, PresetExamSetValidator validator) {
        this.jdbc=jdbc;this.validator=validator;
    }

    public record Review(UUID draftId, String state, long revision, String fingerprint,
                         UUID submittedBy, UUID approvedBy, UUID publishedVersionId, String rejectionReason) {}
    public record Receipt(UUID logicalSetId, UUID versionId, String state,
                          String snapshotSha256, Instant at) {}
    public record Version(UUID id, UUID logicalSetId, int versionNumber, String code,
                          String bankVersion, String status, boolean active) {}

    private record Draft(UUID id,UUID logical,UUID source,String code,String title,String license,
                         String bank,int number,int total,int duration,int display,long revision,
                         Long validated,UUID created,UUID updated) {}
    private record Approval(String state,long revision,String fingerprint,UUID submitter,UUID approver) {}

    @Transactional
    public Review submit(UUID id,long expectedRevision,UUID actor) {
        Draft d=lock(id);
        if(d.revision!=expectedRevision)throw BusinessException.conflict("Draft revision changed");
        if(d.validated==null||d.validated!=d.revision)
            throw BusinessException.conflict("Validate the current draft before submitting");
        // Lock source questions for the transaction and re-run the exact learner validator.
        validate(d);
        String hash=fingerprint(d);
        List<Approval> prior=approval(id,true);
        if(!prior.isEmpty() && !"REJECTED".equals(prior.getFirst().state))
            throw BusinessException.conflict("Draft is already in review or published");
        jdbc.update("INSERT INTO admin_preset_reviews(draft_id,state,draft_revision,fingerprint,submitted_by,"+
            "submitted_at,approved_by,approved_at,rejected_by,rejected_at,rejection_reason,published_version_id) "+
            "VALUES(?,'IN_REVIEW',?,?,?,NOW(),NULL,NULL,NULL,NULL,NULL,NULL) "+
            "ON CONFLICT(draft_id) DO UPDATE SET state='IN_REVIEW',draft_revision=EXCLUDED.draft_revision,"+
            "fingerprint=EXCLUDED.fingerprint,submitted_by=EXCLUDED.submitted_by,submitted_at=NOW(),"+
            "approved_by=NULL,approved_at=NULL,rejected_by=NULL,rejected_at=NULL,rejection_reason=NULL,"+
            "published_version_id=NULL",id,d.revision,hash,actor);
        audit(d,actor,"SUBMIT",hash,null);
        return review(id);
    }

    @Transactional
    public Review approve(UUID id,UUID actor) {
        Draft d=lock(id);
        Approval proof=requireReview(id,"IN_REVIEW");
        if(actor.equals(d.created)||actor.equals(d.updated)||actor.equals(proof.submitter))
            throw BusinessException.forbidden("Author, last editor and submitter cannot approve");
        if(d.revision!=proof.revision || !fingerprint(d).equals(proof.fingerprint))
            throw BusinessException.conflict("Draft or bank source changed since submission");
        validate(d);
        jdbc.update("UPDATE admin_preset_reviews SET state='APPROVED',approved_by=?,approved_at=NOW() "+
            "WHERE draft_id=? AND state='IN_REVIEW'",actor,id);
        audit(d,actor,"APPROVE",proof.fingerprint,null);
        return review(id);
    }

    @Transactional
    public Review reject(UUID id,UUID actor,String reason) {
        Draft d=lock(id);
        Approval proof=requireReview(id,"IN_REVIEW");
        if(reason==null||reason.isBlank()||reason.strip().length()>1000)
            throw BusinessException.badRequest("A rejection reason is required");
        jdbc.update("UPDATE admin_preset_reviews SET state='REJECTED',rejected_by=?,rejected_at=NOW(),"+
            "rejection_reason=? WHERE draft_id=? AND state='IN_REVIEW'",actor,reason.strip(),id);
        audit(d,actor,"REJECT",proof.fingerprint,reason.strip());
        return review(id);
    }

    @Transactional
    public Receipt publish(UUID id,UUID actor) {
        Draft d=lock(id);
        List<Approval> records=approval(id,true);
        if(!records.isEmpty() && "PUBLISHED".equals(records.getFirst().state)) {
            List<UUID> current=jdbc.query("SELECT active_version_id FROM admin_preset_active_versions " +
                "WHERE logical_set_id=? FOR SHARE",(r,i)->(UUID)r.getObject(1),d.logical);
            if(!current.isEmpty() && id.equals(current.getFirst()))
                return new Receipt(d.logical,id,"ACTIVE",records.getFirst().fingerprint,Instant.now());
            throw BusinessException.conflict("This version was already published and replaced or archived");
        }
        Approval proof=requireReview(id,"APPROVED");
        if(d.validated==null || d.validated!=d.revision || proof.revision!=d.revision)
            throw BusinessException.conflict("Approved revision differs from current validated draft");
        if(!CurrentQuestionBank.currentVersion().equals(d.bank))
            throw BusinessException.conflict("Bank is no longer the active verified bank");
        validate(d);
        String hash=fingerprint(d);
        if(!hash.equals(proof.fingerprint))
            throw BusinessException.conflict("Bank source or approved snapshot changed");
        // Serialize releases for a logical set, including first publication.
        jdbc.query("SELECT pg_advisory_xact_lock(hashtextextended(?::text,0))",rs->{},d.logical.toString());
        List<UUID> active=jdbc.query("SELECT active_version_id FROM admin_preset_active_versions "+
                "WHERE logical_set_id=? FOR UPDATE",(rs,row)->(UUID)rs.getObject(1),d.logical);
        if(d.source!=null) {
            if(active.isEmpty()||!d.source.equals(active.getFirst()))
                throw BusinessException.conflict("Source preset is not currently active; re-clone and review");
        } else if(!active.isEmpty()) {
            throw BusinessException.conflict("New preset logical identity already exists");
        }
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM preset_exam_sets WHERE logical_set_id=?",Integer.class,d.logical);
        if(count!=null && count>0 && d.source==null)
            throw BusinessException.conflict("Logical set already contains a version");
        // Insert as DRAFT to allow inserting questions through existing published-row guards.
        jdbc.update("INSERT INTO preset_exam_sets(id,logical_set_id,source_version_id,version_number,"+
            "code,title,license_class,bank_version,total_questions,duration_minutes,display_order,status) "+
            "VALUES(?,?,?,?,?,?,?,?,?,?,?,'DRAFT')",d.id,d.logical,d.source,d.number,
            d.code,d.title,d.license,d.bank,d.total,d.duration,d.display);
        jdbc.update("INSERT INTO preset_exam_set_questions(preset_exam_set_id,question_order,question_id) "+
            "SELECT ?,question_order,question_id FROM admin_preset_draft_questions "+
            "WHERE draft_id=? ORDER BY question_order",d.id,d.id);
        jdbc.update("UPDATE preset_exam_sets SET status='PUBLISHED' WHERE id=? AND status='DRAFT'",d.id);
        if(active.isEmpty()) {
            jdbc.update("INSERT INTO admin_preset_active_versions(logical_set_id,active_version_id,updated_by) "+
                "VALUES(?,?,?)",d.logical,d.id,actor);
        } else {
            jdbc.update("UPDATE admin_preset_active_versions SET active_version_id=?,revision=revision+1,"+
                "updated_by=?,updated_at=NOW() WHERE logical_set_id=?",d.id,actor,d.logical);
        }
        jdbc.update("UPDATE admin_preset_reviews SET state='PUBLISHED',published_version_id=? WHERE draft_id=?",d.id,d.id);
        audit(d,actor,"PUBLISH",hash,null);
        return new Receipt(d.logical,d.id,"ACTIVE",hash,Instant.now());
    }

    @Transactional
    public Receipt archive(UUID versionId,UUID actor) {
        List<UUID> logical=jdbc.query("SELECT logical_set_id FROM preset_exam_sets WHERE id=?",(r,i)->(UUID)r.getObject(1),versionId);
        if(logical.isEmpty())throw BusinessException.notFound("Published preset version not found");
        UUID stable=logical.getFirst();
        List<UUID> active=jdbc.query("SELECT active_version_id FROM admin_preset_active_versions "+
            "WHERE logical_set_id=? FOR UPDATE",(r,i)->(UUID)r.getObject(1),stable);
        if(active.isEmpty())throw BusinessException.notFound("Active logical preset not found");
        if(active.getFirst()==null) {
            Integer archived=jdbc.queryForObject("SELECT COUNT(*) FROM admin_preset_release_audit " +
                "WHERE version_id=? AND action='ARCHIVE'",Integer.class,versionId);
            if(archived!=null && archived>0)
                return new Receipt(stable,versionId,"ARCHIVED",null,Instant.now());
            throw BusinessException.conflict("This version is no longer active");
        }
        if(!versionId.equals(active.getFirst()))
            throw BusinessException.conflict("Only the active version can be archived");
        jdbc.update("UPDATE admin_preset_active_versions SET active_version_id=NULL,revision=revision+1,"+
            "updated_at=NOW(),updated_by=? WHERE logical_set_id=?",actor,stable);
        jdbc.update("INSERT INTO admin_preset_release_audit(version_id,logical_set_id,actor_id,action) "+
            "VALUES(?,?,?,'ARCHIVE')",versionId,stable,actor);
        return new Receipt(stable,versionId,"ARCHIVED",null,Instant.now());
    }

    @Transactional(readOnly=true)
    public Review review(UUID id) {
        List<Review> values=jdbc.query("SELECT draft_id,state,draft_revision,fingerprint,submitted_by,"+
            "approved_by,published_version_id,rejection_reason FROM admin_preset_reviews WHERE draft_id=?",
            (r,i)->new Review((UUID)r.getObject(1),r.getString(2),r.getLong(3),r.getString(4),
                (UUID)r.getObject(5),(UUID)r.getObject(6),(UUID)r.getObject(7),r.getString(8)),id);
        if(values.isEmpty())throw BusinessException.notFound("Preset has no review");
        return values.getFirst();
    }

    @Transactional(readOnly=true)
    public List<Version> versions(UUID logicalSetId) {
        return jdbc.query("SELECT p.id,p.logical_set_id,p.version_number,p.code,p.bank_version,p.status,"+
            "(p.id=a.active_version_id) AS active FROM preset_exam_sets p "+
            "LEFT JOIN admin_preset_active_versions a ON a.logical_set_id=p.logical_set_id "+
            "WHERE p.logical_set_id=? ORDER BY p.version_number DESC",
            (r,i)->new Version((UUID)r.getObject(1),(UUID)r.getObject(2),r.getInt(3),r.getString(4),
                r.getString(5),r.getBoolean(7)?"PUBLISHED": "ARCHIVED",r.getBoolean(7)),logicalSetId);
    }

    private Draft lock(UUID id) {
        List<Draft> found=jdbc.query(DRAFT_SQL,(rs,i)->new Draft((UUID)rs.getObject(1),
            (UUID)rs.getObject(2),(UUID)rs.getObject(3),rs.getString(4),rs.getString(5),
            rs.getString(6),rs.getString(7),rs.getInt(8),rs.getInt(9),rs.getInt(10),
            rs.getInt(11),rs.getLong(12),rs.getObject(13)==null?null:rs.getLong(13),
            (UUID)rs.getObject(14),(UUID)rs.getObject(15)),id);
        if(found.isEmpty())throw BusinessException.notFound("Preset draft not found");
        return found.getFirst();
    }

    private List<Approval> approval(UUID id,boolean lock) {
        return jdbc.query("SELECT state,draft_revision,fingerprint,submitted_by,approved_by FROM admin_preset_reviews "+
            "WHERE draft_id=?"+(lock?" FOR UPDATE":""),(r,i)->new Approval(r.getString(1),r.getLong(2),
                r.getString(3),(UUID)r.getObject(4),(UUID)r.getObject(5)),id);
    }
    private Approval requireReview(UUID id,String state) {
        List<Approval> found=approval(id,true);
        if(found.isEmpty()||!state.equals(found.getFirst().state))
            throw BusinessException.conflict("Preset is not "+state);
        return found.getFirst();
    }

    private void validate(Draft d) {
        PresetExamSet set=PresetExamSet.builder().id(d.id).code(d.code).licenseClass(d.license)
            .bankVersion(d.bank).totalQuestions(d.total).durationMinutes(d.duration)
            .status(PresetExamSetStatus.DRAFT).build();
        List<PresetExamSetQuestion> entries=jdbc.query("SELECT question_order,question_id FROM "+
            "admin_preset_draft_questions WHERE draft_id=? ORDER BY question_order",
            (r,i)->PresetExamSetQuestion.builder().presetExamSetId(d.id).questionOrder(r.getInt(1))
                .questionId(r.getInt(2)).build(),d.id);
        validator.validateDraft(set,entries);
    }

    private static void add(StringBuilder sb,Object v) {
        if(v==null) sb.append("-1:");
        else { String s=String.valueOf(v); sb.append(s.length()).append(':').append(s); }
        sb.append('\n');
    }
    /** The content digest includes all answer/media/critical/source fields, not just question IDs. */
    private String fingerprint(Draft d) {
        StringBuilder content=new StringBuilder("DTS-PRESET-VERSION-1\n");
        for(Object v:List.of(d.id,d.logical,d.code,d.title,d.license,d.bank,d.number,d.total,
                d.duration,d.display,d.revision))add(content,v);
        add(content,d.source);
        // FOR SHARE holds real source question rows until commit, preventing a publish race.
        jdbc.query("SELECT pq.question_order,pq.source_question_id,q.id,q.chapter,q.question_text,"+
            "q.options::text,q.correct_answer,q.is_critical,q.critical_licenses::text,"+
            "q.applicable_licenses::text,q.explanation,q.media_file_id,q.image_url,q.updated_at " +
            "FROM admin_preset_draft_questions pq JOIN questions q ON q.id=pq.question_id "+
            "WHERE pq.draft_id=? ORDER BY pq.question_order FOR SHARE OF q",
            (ResultSet r)->{
                for(int i=1;i<=14;i++)add(content,r.getObject(i));
            },d.id);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(content.toString().getBytes(StandardCharsets.UTF_8)));
        } catch(NoSuchAlgorithmException e) {throw new IllegalStateException(e);}
    }
    private void audit(Draft d,UUID actor,String action,String hash,String reason) {
        jdbc.update("INSERT INTO admin_preset_release_audit(draft_id,logical_set_id,actor_id,action,"+
            "snapshot_sha256,reason) VALUES(?,?,?,?,?,?)",d.id,d.logical,actor,action,hash,reason);
    }
}
