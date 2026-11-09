package com.dts.practice.simulation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;
class SimulationShortcutRulesTest {
    @ParameterizedTest @CsvSource({"10000,10000,VIDEO_TOO_SHORT","9999,20000,VIDEO_TOO_SHORT","60000,9999,SCORING_BEFORE_INTRO_END","60000,0,SCORING_BEFORE_INTRO_END"})
    void cannotLoseScoringOpportunity(int duration,int first,String reason){var c=SimulationShortcutRules.intro(duration,first,true,true,false,null,0);assertFalse(c.available());assertEquals(reason,c.reason());assertTrue(c.maxSafeTargetMs()<10000);}
    @ParameterizedTest @CsvSource({"0,17432,17432","10000,7432,17432","10000,0,10000","0,0,0"})
    void absoluteClockIsNotShiftedForScoring(int checkpoint,long elapsed,long expected){assertEquals(expected,SimulationShortcutRules.expectedAbsoluteMs(checkpoint,elapsed));}
    @Test void exactTenIsSafeWhenScoringBeginsAtTen(){assertTrue(SimulationShortcutRules.intro(60000,10000,true,true,false,null,0).available());}
    @Test void afterFirstResponseZeroIsDifferentFromNull(){assertFalse(SimulationShortcutRules.intro(60000,17000,true,true,false,0,0).available());assertTrue(SimulationShortcutRules.intro(60000,17000,true,true,false,null,0).available());}
    @Test void disabledProtocolHasNoImplicitUpgrade(){assertEquals("NOT_ENABLED",SimulationShortcutRules.intro(60000,17000,false,true,false,null,0).reason());}
    @Test void noSkipAfterOneUse(){assertEquals("ALREADY_USED",SimulationShortcutRules.intro(60000,17000,true,true,true,null,0).reason());}
    @Test void requestRequiresPlaying(){assertEquals("NOT_PLAYING",SimulationShortcutRules.intro(60000,17000,true,false,false,null,0).reason());}
    @Test void doesNotJumpBackwardAfterTen(){assertEquals("INTRO_ALREADY_PASSED",SimulationShortcutRules.intro(60000,17000,true,true,false,null,10000).reason());}
    @Test void policyDoesNotGrantUnlimitedSeeking(){var c=SimulationShortcutRules.intro(60000,25000,true,true,false,null,1000);assertEquals(10000,c.targetMs());assertEquals(10000,c.maxSafeTargetMs());}
    @Test void firstResponse17432KeepsSamePositiveScore(){var t=new SimulationBank.Timeline(17000,18000,19000,20000,21000,22000);assertEquals(5,SimulationLearningRules.score(t,17432));assertEquals(5,SimulationLearningRules.score(t,(int)SimulationShortcutRules.expectedAbsoluteMs(10000,7432)));assertEquals(0,SimulationLearningRules.score(t,7432));}
    @Test void nullStillScoresZero(){assertEquals(0,SimulationLearningRules.score(new SimulationBank.Timeline(0,1000,2000,3000,4000,5000),null));}
    @Test void zeroStillScoresFirstWindow(){assertEquals(5,SimulationLearningRules.score(new SimulationBank.Timeline(0,1000,2000,3000,4000,5000),0));}
}
