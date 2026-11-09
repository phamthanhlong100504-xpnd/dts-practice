package com.dts.practice.adminrelease;

import com.dts.practice.simulation.SimulationBank;
import com.dts.practice.simulation.AdminPreparedBankValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Idempotent PREPARE only. Does not mutate learner content, current bank, sessions or results. */
@Service
public class AdminReleaseConsumerService {
    public record Prepare(UUID releaseId,String bankVersion,String snapshotSha256,
                          String manifestSha256,String snapshotJson) {}
    public record Ack(UUID releaseId,String bankVersion,String snapshotSha256,
                      String manifestSha256,String status,boolean checksumVerified) {}
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    public AdminReleaseConsumerService(JdbcTemplate jdbc,ObjectMapper mapper) {
        this.jdbc=jdbc;this.mapper=mapper;
    }
    static boolean sha(String s){return s!=null && s.matches("[0-9a-f]{64}");}
    private static String digest(String raw) {
        try {return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(raw.getBytes(StandardCharsets.UTF_8)));}
        catch(Exception e){throw new IllegalStateException("Unable to digest release snapshot",e);}
    }
    public static void validate(Prepare p,ObjectMapper mapper) {
        if(p==null || p.releaseId()==null || p.bankVersion()==null || !sha(p.snapshotSha256()) ||
           !sha(p.manifestSha256()) || p.snapshotJson()==null ||
           p.snapshotJson().getBytes(StandardCharsets.UTF_8).length>16*1024*1024 ||
           p.snapshotJson().length()<100 || !p.snapshotSha256().equals(digest(p.snapshotJson())))
           throw new IllegalArgumentException("Invalid frozen release snapshot or SHA256");
        if(!p.bankVersion().matches("[A-Za-z0-9][A-Za-z0-9._-]{0,95}") || "current".equals(p.bankVersion()))
            throw new IllegalArgumentException("Invalid bank version");
        try {
            SimulationBank.Snapshot bank=mapper.readValue(p.snapshotJson(),SimulationBank.Snapshot.class);
            AdminPreparedBankValidator.validate(bank,p.bankVersion());
            if(!p.bankVersion().equals(bank.bankVersion()) ||
               !p.manifestSha256().equals(bank.manifestSha256()))
                throw new IllegalArgumentException("Simulation bank identity mismatch");
        } catch(com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalArgumentException("Malformed frozen release snapshot");
        }
    }
    private Ack row(java.sql.ResultSet r,int i)throws java.sql.SQLException {
        return new Ack(r.getObject("release_id",UUID.class),r.getString("bank_version"),
            r.getString("snapshot_sha256"),r.getString("manifest_sha256"),
            r.getString("state"),true);
    }
    @Transactional
    public Ack prepare(Prepare p) {
        validate(p,mapper);
        // Serialize competing requests for one bank version; reject same version with other hash.
        jdbc.query("SELECT pg_advisory_xact_lock(hashtext(?))", (org.springframework.jdbc.core.RowCallbackHandler) r->{},p.bankVersion());
        List<Ack> byVersion=jdbc.query("SELECT * FROM admin_simulation_release_preparations WHERE bank_version=?",this::row,p.bankVersion());
        if(!byVersion.isEmpty()) {
            Ack old=byVersion.get(0);
            if(!old.releaseId().equals(p.releaseId()) || !old.snapshotSha256().equals(p.snapshotSha256()) ||
               !old.manifestSha256().equals(p.manifestSha256()))
               throw new IllegalStateException("CONSUMER_VERSION_MISMATCH");
            return old;
        }
        if(!jdbc.query("SELECT release_id FROM admin_simulation_release_preparations WHERE release_id=?",
                (r,i)->r.getObject(1,UUID.class),p.releaseId()).isEmpty())
            throw new IllegalStateException("CONSUMER_RELEASE_CONFLICT");
        jdbc.update("INSERT INTO admin_simulation_release_preparations " +
            "(release_id,bank_version,snapshot_sha256,manifest_sha256,snapshot_json) VALUES(?,?,?,?,?)",
            p.releaseId(),p.bankVersion(),p.snapshotSha256(),p.manifestSha256(),p.snapshotJson());
        return new Ack(p.releaseId(),p.bankVersion(),p.snapshotSha256(),p.manifestSha256(),"PREPARED",true);
    }
    @Transactional(readOnly=true)
    public Ack status(UUID id) {
        return jdbc.query("SELECT * FROM admin_simulation_release_preparations WHERE release_id=?",
            this::row,id).stream().findFirst().orElseThrow(()->new IllegalArgumentException("Release not prepared"));
    }
}
