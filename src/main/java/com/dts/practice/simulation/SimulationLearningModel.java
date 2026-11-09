package com.dts.practice.simulation;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class SimulationLearningModel {
    private SimulationLearningModel() {}
    public record Create(@NotNull UUID eventId,@NotNull UUID scenarioId,@NotBlank String licenseClass,@NotBlank String bankVersion) {}
    public record Event(@NotNull UUID eventId,@NotNull UUID playbackEventId,@NotNull @Min(0) Integer mediaTimeMs) {}
    public record Interrupt(@NotNull UUID eventId,@NotNull UUID playbackEventId,@NotBlank @Pattern(regexp="VIDEO_ERROR|NETWORK|PAGE_EXIT|PAUSED|SEEK") String reason) {}
    public record Cancel(@NotNull UUID eventId) {}
    public record Bookmark(@NotBlank String licenseClass,@NotBlank String bankVersion,@NotNull Boolean bookmarked) {}
    public record CursorCommand(@NotNull UUID eventId,@NotBlank String licenseClass,@NotBlank String bankVersion,@NotNull UUID scenarioId,@Min(0) long expectedRevision) {}
    public record Segment(int startMs,int endMs,int score,boolean endInclusive) {}
    public record Policy(boolean allowPause,boolean allowSeek,boolean allowFullscreen,boolean allowHintsWhilePlaying,int heartbeatIntervalMs,int maxHeartbeatGapMs) {}
    public record Result(UUID attemptId,UUID scenarioId,String status,int score,int maxScore,boolean responseRecorded,Integer firstResponseMs,
                         int videoDurationMs,String bankVersion,String scoringVersion,List<Segment> scoringSegments,String hazardSign,String recognitionTip) {}
    public record Attempt(UUID attemptId,UUID scenarioId,String bankVersion,String scoringVersion,String status,UUID playbackEventId,
                          int videoDurationMs,int resumeAtMs,boolean responseRecorded,Integer firstResponseMs,boolean requiresResume,
                          Instant createdAt,Instant startedAt,Instant completedAt,Policy policy,Result result) {}
    public record Playback(UUID attemptId,UUID playbackEventId,String bankVersion,String videoUrl,java.time.OffsetDateTime expiresAt,String mimeType,int durationMs,int resumeAtMs) {}
    public record Cursor(UUID scenarioId,UUID sourceAttemptId,long revision) {}
    public record Stats(int completedAttemptCount,Integer bestScore,boolean bookmarked) {public boolean practiced() {return completedAttemptCount>0;}}
    static final class Row {
        UUID id,user,createEvent,scenarioId,nextScenario,playbackId,responseEvent;
        String bank,scoring,manifest,license,status,reason;
        int number,chapter,duration,segmentStart,lastMs;
        Integer responseMs,score;
        Instant granted,started,segmentStarted,lastObserved,created,updated,completed;
        SimulationBank.Scenario snapshot;
    }
}
