package com.dts.practice.simulation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import static com.dts.practice.simulation.SimulationLearningModel.*;

class SimulationLearningRulesTest {
    final SimulationBank.Timeline timeline=new SimulationBank.Timeline(10000,11000,12000,13000,14000,15000);
    @ParameterizedTest @CsvSource({"0,0","9999,0","10000,5","10999,5","11000,4","11999,4","12000,3","12999,3","13000,2","13999,2","14000,1","15000,1","15001,0","60000,0"})
    void scoresAllBoundariesWithoutReplacingMediaPositionWithWallTime(int position,int score) {assertEquals(score,SimulationLearningRules.score(timeline,position));}
    @Test void noResponseIsZeroAndLastScoringEndpointIsExplicitlyInclusive() {
        assertEquals(0,SimulationLearningRules.score(timeline,null));var segments=SimulationLearningRules.segments(timeline);
        assertEquals(5,segments.size());assertFalse(segments.getFirst().endInclusive());assertTrue(segments.getLast().endInclusive());
    }
    @Test void playbackRequiresContinuousServerTimeAndMonotonicVideoPosition() {
        var r=row();var now=r.segmentStarted.plusMillis(10000);
        assertDoesNotThrow(()->SimulationLearningRules.position(r,10000,now,false));
        assertThrows(Exception.class,()->SimulationLearningRules.position(r,20000,now,false));
        assertThrows(Exception.class,()->SimulationLearningRules.position(r,1000,now,false));
        assertThrows(Exception.class,()->SimulationLearningRules.position(r,60000,r.segmentStarted.plusSeconds(60),false));
        r.lastMs=10000;r.lastObserved=now;
        assertDoesNotThrow(()->SimulationLearningRules.position(r,9500,now,true));
        assertThrows(Exception.class,()->SimulationLearningRules.position(r,9500,now,false));
    }
    private Row row() {
        var r=new Row();r.status="PLAYING";r.duration=60000;r.segmentStarted=Instant.parse("2026-11-02T00:00:00Z");r.lastObserved=r.segmentStarted;return r;
    }
}
