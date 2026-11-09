package com.dts.practice.simulation;

/** Pure protocol rules. No wall-clock credit is invented and scoring is unchanged. */
public final class SimulationShortcutRules {
    public static final String VERSION="video-shortcuts-v1";
    public static final int INTRO_MS=10000;
    private SimulationShortcutRules() {}
    public record Intro(boolean available,int targetMs,int maxSafeTargetMs,String reason) {}
    public static Intro intro(int duration,int firstScoringMs,boolean enabled,boolean playing,boolean used,Integer response,int lastMs) {
        int limit=Math.max(0,Math.min(INTRO_MS,Math.min(duration-1,firstScoringMs)));
        String reason=!enabled?"NOT_ENABLED":response!=null?"REACTION_ALREADY_RECORDED":used?"ALREADY_USED":duration<=INTRO_MS?"VIDEO_TOO_SHORT":firstScoringMs<INTRO_MS?"SCORING_BEFORE_INTRO_END":lastMs>=INTRO_MS?"INTRO_ALREADY_PASSED":!playing?"NOT_PLAYING":null;
        return new Intro(reason==null,INTRO_MS,limit,reason);
    }
    /** A server RESTART re-arms only an unanswered exam at checkpoint zero.
     * SKIP itself keeps checkpoint10000, so its rotated nonce is not another allowance.
     * Learning retains its original once-per-attempt policy; zero is a stored response. */
    public static boolean usedInPlayback(boolean exam,boolean used,Integer response,int checkpoint) {
        return used && (!exam || response!=null || checkpoint!=0);
    }
    public static long expectedAbsoluteMs(int checkpoint,long elapsedMs){return checkpoint+elapsedMs;}
}
