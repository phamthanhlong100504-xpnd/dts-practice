package com.dts.practice.integration;

import com.dts.practice.simulation.SimulationBankClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.transaction.PlatformTransactionManager;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SimulationPerformanceWorkerTest {
    JdbcTemplate jdbc;KafkaTemplate<String,String> kafka;SimulationPerformanceWorker worker;
    @BeforeEach @SuppressWarnings("unchecked") void setup(){
        jdbc=mock(JdbcTemplate.class);kafka=mock(KafkaTemplate.class);
        worker=new SimulationPerformanceWorker(jdbc,kafka,new ObjectMapper(),mock(SimulationBankClient.class),mock(PlatformTransactionManager.class),"simulation-performance-v1",true);
        when(jdbc.queryForObject("SELECT pg_try_advisory_xact_lock(773504,2)",Boolean.class)).thenReturn(true);
        when(jdbc.queryForObject(startsWith("SELECT next_attempt_at"),eq(Boolean.class),any())).thenReturn(true);
    }
    @SuppressWarnings({"unchecked","rawtypes"}) void rows(int count)throws Exception{
        when(jdbc.query(startsWith("SELECT event_sequence,payload::text"),any(RowMapper.class))).thenAnswer(inv->{
            RowMapper mapper=inv.getArgument(1);var values=new ArrayList<>();
            for(int i=1;i<=count;i++){ResultSet r=mock(ResultSet.class);when(r.getLong(1)).thenReturn((long)i);when(r.getString(2)).thenReturn("event-"+i);values.add(mapper.mapRow(r,i-1));}return values;
        });
    }
    @Test void successfulPublicationUsesOneStableKeyAndMarksTheSameSequence()throws Exception{
        rows(2);when(kafka.send(anyString(),anyString(),anyString())).thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        worker.publish();var ordered=inOrder(kafka,jdbc);
        ordered.verify(kafka).send("simulation-performance-v1","dts-simulation-performance-v1","event-1");
        ordered.verify(jdbc).update(startsWith("UPDATE simulation_performance_outbox SET published_at"),eq(1L));
        ordered.verify(kafka).send("simulation-performance-v1","dts-simulation-performance-v1","event-2");
    }
    @Test void brokerFailureRetainsTheEventAndNeverPublishesAroundTheGap()throws Exception{
        rows(2);when(kafka.send(anyString(),anyString(),anyString())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("isolated broker fixture")));
        worker.publish();verify(kafka,times(1)).send(anyString(),anyString(),eq("event-1"));
        verify(jdbc).update(startsWith("UPDATE simulation_performance_outbox SET attempts"),anyString(),eq(1L));
        verify(jdbc,never()).update(startsWith("UPDATE simulation_performance_outbox SET published_at"),anyLong());
    }
    @Test void aDeferredLowerSequenceBlocksHigherOnes()throws Exception{
        rows(2);when(jdbc.queryForObject(startsWith("SELECT next_attempt_at"),eq(Boolean.class),eq(1L))).thenReturn(false);
        worker.publish();verifyNoInteractions(kafka);
    }
    @Test void anotherPublisherHoldingTheCoordinatorCausesNoSend(){
        when(jdbc.queryForObject("SELECT pg_try_advisory_xact_lock(773504,2)",Boolean.class)).thenReturn(false);
        worker.publish();verifyNoInteractions(kafka);
    }
}
