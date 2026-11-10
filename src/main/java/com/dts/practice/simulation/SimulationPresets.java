package com.dts.practice.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.*;

/** Internal DTOs are validated against the current bank and never returned to a learner. */
public final class SimulationPresets {
    private SimulationPresets() {}
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Snapshot(String sourceVersion,String sourceSha256,int sourceSetId,String code,String title,int displayOrder,
                           String bankVersion,String bankManifestSha256,String scoringVersion,int totalScenarios,int maxTotalScore,
                           int passingScore,List<SimulationBank.Scenario> scenarios) {}
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Version(UUID presetSetId,UUID presetVersionId,Snapshot snapshot,String manifestSha256,String status,boolean current,Instant publishedAt) {}
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Catalog(String bankVersion,String bankManifestSha256,List<Version> items) {}
    public record Ref(UUID presetSetId,UUID presetVersionId,String code,String title,int displayOrder,String manifestSha256,
                      String sourceVersion,String sourceSha256) {}
    public record Item(UUID presetSetId,UUID presetVersionId,String code,String title,int displayOrder,String status,
                       int totalScenarios,int maxTotalScore,int passingScore,boolean hasCompletedAttempt,
                       UUID latestCompletedSessionId,Integer latestCompletedScore,Boolean latestCompletedPassed,Instant latestCompletedAt,
                       UUID activeSessionId,String attemptStatus) {}
    public record ListView(SimulationExamModel.Scope scope,String bankVersion,int totalPublishedSets,List<Item> items,
                           SimulationExamModel.Session activeSession) {}
    public record Detail(SimulationExamModel.Scope scope,Item preset,String mode,int maxScorePerScenario,boolean canStart,
                         String unavailableReason,SimulationExamModel.Session activeSession) {}

    static Ref ref(Version v){var s=v.snapshot();return new Ref(v.presetSetId(),v.presetVersionId(),s.code(),s.title(),s.displayOrder(),v.manifestSha256(),s.sourceVersion(),s.sourceSha256());}
    static Catalog validate(Catalog value,SimulationBank.Snapshot bank) {
        try {
            if(value==null||!bank.bankVersion().equals(value.bankVersion())||!bank.manifestSha256().equals(value.bankManifestSha256())||value.items()==null||value.items().size()>15)throw new IllegalArgumentException();
            Set<UUID> sets=new HashSet<>(),versions=new HashSet<>();Set<Integer> order=new HashSet<>();int prior=0;
            for(var v:value.items()) {validate(v,bank);int current=v.snapshot().displayOrder();if(!sets.add(v.presetSetId())||!versions.add(v.presetVersionId())||!order.add(current)||current<=prior)throw new IllegalArgumentException();prior=current;}
            return value;
        }catch(Exception e){throw SimulationExamFailure.unavailable();}
    }
    static Version validate(Version v,SimulationBank.Snapshot bank) {
        try {
            if(v==null||v.presetSetId()==null||v.presetVersionId()==null||!"PUBLISHED".equals(v.status())||!v.current()||v.publishedAt()==null||!sha(v.manifestSha256()))throw new IllegalArgumentException();
            var s=Objects.requireNonNull(v.snapshot());
            if(s.sourceVersion()==null||!s.sourceVersion().matches("[A-Za-z0-9][A-Za-z0-9._-]{0,95}")||!sha(s.sourceSha256())||s.sourceSetId()<1||s.sourceSetId()>15||s.displayOrder()!=s.sourceSetId()
                    ||!String.format(Locale.ROOT,"SIM-SET-%02d",s.sourceSetId()).equals(s.code())||s.title()==null||s.title().isBlank()||s.title().length()>200||s.title().chars().anyMatch(c->c<32)
                    ||!bank.bankVersion().equals(s.bankVersion())||!bank.manifestSha256().equals(s.bankManifestSha256())||!bank.scoringVersion().equals(s.scoringVersion())
                    ||s.totalScenarios()!=10||s.maxTotalScore()!=50||s.passingScore()!=35||s.scenarios()==null||s.scenarios().size()!=10)throw new IllegalArgumentException();
            Set<UUID> ids=new HashSet<>();int[] quota=new int[6];
            for(var selected:s.scenarios()) {
                if(selected==null||selected.scenarioId()==null||!ids.add(selected.scenarioId())||!SimulationBank.scenario(bank,selected.scenarioId()).equals(selected))throw new IllegalArgumentException();
                quota[selected.chapterId()-1]++;
            }
            if(!Arrays.equals(quota,new int[]{2,1,2,1,2,2}))throw new IllegalArgumentException();
            return v;
        }catch(Exception e){throw SimulationExamFailure.unavailable();}
    }
    private static boolean sha(String value){return value!=null&&value.matches("[a-f0-9]{64}");}
}
