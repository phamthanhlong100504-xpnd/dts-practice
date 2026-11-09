package com.dts.practice.simulation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SimulationExamModel {
    private SimulationExamModel() {}
    public record Policy(int totalScenarios,int maxScenarioScore,int maxTotalScore,int passingScore,String policyVersion) {}
    public record Scope(UUID userId,String licenseClass,String bankVersion) {}
    public record Position(int position,String status) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(value={"presetSetId","presetVersionId","presetTitle"},allowGetters=true)
    public record Session(Scope scope,UUID sessionId,String mode,String status,String bankVersion,String scoringVersion,Policy policy,
                          int totalScenarios,int completedScenarios,int currentPosition,int maxTotalScore,boolean responseRecorded,
                          UUID playbackEventId,String playbackState,boolean requiresRestart,SimulationLearningModel.Policy playbackPolicy,
                          List<Position> positions,Instant startedAt,Instant completedAt,@JsonInclude(JsonInclude.Include.NON_NULL) SimulationPresets.Ref preset) {
        public Session(Scope scope,UUID sessionId,String mode,String status,String bankVersion,String scoringVersion,Policy policy,int totalScenarios,int completedScenarios,int currentPosition,int maxTotalScore,boolean responseRecorded,UUID playbackEventId,String playbackState,boolean requiresRestart,SimulationLearningModel.Policy playbackPolicy,List<Position> positions,Instant startedAt,Instant completedAt) {
            this(scope,sessionId,mode,status,bankVersion,scoringVersion,policy,totalScenarios,completedScenarios,currentPosition,maxTotalScore,responseRecorded,playbackEventId,playbackState,requiresRestart,playbackPolicy,positions,startedAt,completedAt,null);
        }
        @JsonProperty("presetSetId") @JsonInclude(JsonInclude.Include.NON_NULL) public UUID presetSetId(){return preset==null?null:preset.presetSetId();}
        @JsonProperty("presetVersionId") @JsonInclude(JsonInclude.Include.NON_NULL) public UUID presetVersionId(){return preset==null?null:preset.presetVersionId();}
        @JsonProperty("presetTitle") @JsonInclude(JsonInclude.Include.NON_NULL) public String presetTitle(){return preset==null?null:preset.title();}
    }
    public record Preparation(Scope scope,String bankVersion,Policy policy,Session activeSession) {}
    public record Active(Session session) {}
    public record Playback(UUID sessionId,int position,UUID playbackEventId,String videoUrl,OffsetDateTime expiresAt,String mimeType,
                           int durationMs,boolean responseRecorded,int restartAtMs) {}
    public record ResultItem(int position,int score,int maxScore) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(value={"presetSetId","presetVersionId","presetTitle"},allowGetters=true)
    public record Result(Scope scope,UUID sessionId,String mode,String status,String bankVersion,String scoringVersion,Policy policy,
                         int totalScenarios,int completedScenarios,int totalScore,int maxTotalScore,int passingScore,boolean passed,
                         String resultStatus,Instant startedAt,Instant completedAt,List<ResultItem> items,@JsonInclude(JsonInclude.Include.NON_NULL) SimulationPresets.Ref preset) {
        public Result(Scope scope,UUID sessionId,String mode,String status,String bankVersion,String scoringVersion,Policy policy,int totalScenarios,int completedScenarios,int totalScore,int maxTotalScore,int passingScore,boolean passed,String resultStatus,Instant startedAt,Instant completedAt,List<ResultItem> items) {
            this(scope,sessionId,mode,status,bankVersion,scoringVersion,policy,totalScenarios,completedScenarios,totalScore,maxTotalScore,passingScore,passed,resultStatus,startedAt,completedAt,items,null);
        }
        @JsonProperty("presetSetId") @JsonInclude(JsonInclude.Include.NON_NULL) public UUID presetSetId(){return preset==null?null:preset.presetSetId();}
        @JsonProperty("presetVersionId") @JsonInclude(JsonInclude.Include.NON_NULL) public UUID presetVersionId(){return preset==null?null:preset.presetVersionId();}
        @JsonProperty("presetTitle") @JsonInclude(JsonInclude.Include.NON_NULL) public String presetTitle(){return preset==null?null:preset.title();}
    }
    public record ReviewItem(int position,UUID scenarioId,int scenarioNumber,String title,String chapterName,int score,int maxScore,
                             boolean responseRecorded,Integer firstResponseMs,int durationMs,List<SimulationLearningModel.Segment> scoringSegments,
                             String hazardSign,String recognitionTip,String scoringVersion,String playbackPath) {}
    public record Review(Result result,List<ReviewItem> items) {}
    public record Create(String mode,String licenseClass,String bankVersion,UUID presetSetId,UUID presetVersionId) {
        public Create(String mode,String licenseClass,String bankVersion){this(mode,licenseClass,bankVersion,null,null);}
    }
    public record Event(UUID eventId,int position,UUID playbackEventId,Integer mediaTimeMs) {}
    public record Restart(UUID eventId,int position,UUID playbackEventId) {}
    public record Command(UUID eventId) {}
    static final class Row {
        UUID id,user,createKey;String createHash,license,bank,scoring,manifest,policyVersion,status;
        String mode="RANDOM";SimulationPresets.Ref preset;
        int passingScore,completedCount,currentPosition;Instant created,started,updated,completed,cancelled;
    }
    static final class Frame {
        UUID session,scenarioId,playbackId,responseEvent;int position,duration,lastMs,segmentStart;boolean checkpointColumn;Integer responseMs,score;
        String chapterName,etag,state;boolean complete;Instant granted,segmentStarted,lastObserved,completed;
        SimulationBank.Scenario snapshot;
    }
}
