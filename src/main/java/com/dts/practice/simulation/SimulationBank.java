package com.dts.practice.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.*;

/** Internal Content Builder DTOs. Never serialize Snapshot directly to a learner. */
public final class SimulationBank {
    private SimulationBank() {}
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Asset(String fileName,String mimeType,String sha256,Integer durationMs,Integer videoTrackDurationMs) {}
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Chapter(int chapterId,String name,int totalScenarios) {}
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Timeline(int t5StartMs,int t4StartMs,int t3StartMs,int t2StartMs,int t1StartMs,int t0EndMs) {}
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Scenario(UUID scenarioId,int scenarioNumber,int chapterId,String title,int durationMs,UUID videoMediaId,UUID thumbnailMediaId,
                           String hazardSign,String recognitionTip,Timeline scoringTimeline,Asset video,Asset thumbnail) {}
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Snapshot(String bankVersion,String contentVersion,String assetVersion,String scoringVersion,String sourceSha256,
                           String assetManifestSha256,String manifestSha256,List<Chapter> chapters,List<Scenario> scenarios) {}
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Published(Snapshot snapshot,String status,boolean current,Instant publishedAt) {}

    static void version(String value) {
        if(value==null || !value.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,95}") || Set.of("current","source","imports","resolve-media").contains(value))
            throw SimulationFailure.invalid("Invalid bankVersion");
    }
    static Snapshot validate(Published published, String requestedVersion) {
        try {
            if(published==null || !"PUBLISHED".equals(published.status()) || published.publishedAt()==null
                    || (requestedVersion==null && !published.current())) throw new IllegalArgumentException();
            Snapshot bank=Objects.requireNonNull(published.snapshot());version(bank.bankVersion());
            if(requestedVersion!=null && !requestedVersion.equals(bank.bankVersion())) throw new IllegalArgumentException();
            if(blank(bank.contentVersion()) || blank(bank.assetVersion()) || blank(bank.scoringVersion())
                    || !sha(bank.sourceSha256()) || !sha(bank.assetManifestSha256()) || !sha(bank.manifestSha256())
                    || bank.chapters()==null || bank.chapters().size()!=6 || bank.scenarios()==null || bank.scenarios().size()!=120)
                throw new IllegalArgumentException();
            int[] counts={29,14,20,10,17,30};
            for(int i=0;i<6;i++) {
                var c=bank.chapters().get(i);
                if(c.chapterId()!=i+1 || blank(c.name()) || c.totalScenarios()!=counts[i]) throw new IllegalArgumentException();
            }
            Set<UUID> ids=new HashSet<>(), mediaIds=new HashSet<>();int[] actual=new int[6];int lastChapter=1;
            for(int i=0;i<120;i++) {
                var s=bank.scenarios().get(i);var t=Objects.requireNonNull(s.scoringTimeline());
                if(s.scenarioNumber()!=i+1 || s.scenarioId()==null || !ids.add(s.scenarioId()) || s.chapterId()<1 || s.chapterId()>6
                        || s.chapterId()<lastChapter || blank(s.title()) || s.durationMs()<=0
                        || s.videoMediaId()==null || s.thumbnailMediaId()==null || !mediaIds.add(s.videoMediaId()) || !mediaIds.add(s.thumbnailMediaId())
                        || blank(s.hazardSign()) || blank(s.recognitionTip())
                        || t.t5StartMs()<0 || t.t4StartMs()<=t.t5StartMs() || t.t3StartMs()<=t.t4StartMs()
                        || t.t2StartMs()<=t.t3StartMs() || t.t1StartMs()<=t.t2StartMs() || t.t0EndMs()<=t.t1StartMs() || t.t0EndMs()>s.durationMs())
                    throw new IllegalArgumentException();
                var v=Objects.requireNonNull(s.video());var image=Objects.requireNonNull(s.thumbnail());
                if(!"video/mp4".equals(v.mimeType()) || v.fileName()==null || !v.fileName().toLowerCase(Locale.ROOT).endsWith(".mp4")
                        || !sha(v.sha256()) || v.durationMs()==null || v.videoTrackDurationMs()==null || v.durationMs()!=s.durationMs()
                        || v.videoTrackDurationMs()<t.t0EndMs() || !"image/webp".equals(image.mimeType()) || !sha(image.sha256())
                        || image.fileName()==null || !image.fileName().toLowerCase(Locale.ROOT).endsWith(".webp")) throw new IllegalArgumentException();
                actual[s.chapterId()-1]++;lastChapter=s.chapterId();
            }
            if(!Arrays.equals(counts,actual)) throw new IllegalArgumentException();
            return bank;
        } catch(Exception failure) {throw SimulationFailure.unavailable();}
    }
    static Scenario scenario(Snapshot bank,UUID id) {
        return bank.scenarios().stream().filter(s->s.scenarioId().equals(id)).findFirst().orElseThrow(SimulationFailure::notFound);
    }
    private static boolean blank(String s) {return s==null || s.isBlank();}
    private static boolean sha(String s) {return s!=null && s.matches("[a-f0-9]{64}");}
}
