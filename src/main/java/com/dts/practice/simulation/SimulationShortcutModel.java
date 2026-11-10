package com.dts.practice.simulation;

import java.time.Instant;
import java.util.*;

public final class SimulationShortcutModel {
    private SimulationShortcutModel() {}
    public record Request(UUID eventId,int position,UUID playbackEventId,Integer mediaTimeMs,String reactionAcknowledgement) {}
    public record Advance(boolean available,String reason) {}
    public record State(String targetKind,UUID targetId,String licenseClass,boolean supported,boolean enabled,boolean canEnable,String protocolVersion,
                        String reason,long shortcutRevision,String stateFingerprint,int position,UUID playbackEventId,int startAtMs,
                        SimulationShortcutRules.Intro skipIntro,Advance advanceAfterReaction,String reactionAcknowledgement,String completionReason,Map<String,String> completionReasons,
                        SimulationLearningModel.Attempt attempt,SimulationExamModel.Session session) {}
    public record Receipt(UUID eventId,String operation,String targetKind,UUID targetId,int position,UUID playbackEventId,
                          UUID resultingPlaybackEventId,int checkpointMs,long appliedRevision,Instant appliedAt) {}
    public record Ack(Receipt receipt,State state) {}
    static final class Protocol {
        String kind;UUID target,user,secret;long revision;Set<Integer> skipped=new HashSet<>();Map<String,String> reasons=new HashMap<>();
    }
}
