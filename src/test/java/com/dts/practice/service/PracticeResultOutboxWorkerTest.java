package com.dts.practice.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.kafka.core.KafkaTemplate;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class PracticeResultOutboxWorkerTest {
    @SuppressWarnings({"unchecked","rawtypes"})
    void run(boolean fail) throws Exception {
        var jdbc=mock(JdbcTemplate.class);KafkaTemplate<String,String> kafka=mock(KafkaTemplate.class);
        UUID event=UUID.randomUUID(),user=UUID.randomUUID();String json="{\"eventVersion\":2,\"sourceId\":\""+event+"\"}";
        var rs=mock(ResultSet.class);when(rs.getObject("event_id",UUID.class)).thenReturn(event);
        when(rs.getObject("user_id",UUID.class)).thenReturn(user);when(rs.getString("payload")).thenReturn(json);when(rs.getInt("attempts")).thenReturn(0);
        when(jdbc.query(anyString(),any(RowMapper.class))).thenAnswer(inv -> {
            RowMapper<?> mapper=inv.getArgument(1);return List.of(mapper.mapRow(rs,0));
        });
        when(kafka.send("learning-results",user.toString(),json)).thenReturn(fail
            ? CompletableFuture.failedFuture(new IllegalStateException("offline")) : CompletableFuture.completedFuture(null));
        new PracticeResultOutboxWorker(jdbc,kafka,"learning-results").publishPending();
        verify(kafka).send("learning-results",user.toString(),json);
        if(fail) {
            verify(jdbc).update(contains("next_attempt_at=NOW()"),eq(1),anyString(),eq(2L),eq(event));
            verify(jdbc,never()).update(contains("published_at=NOW()"),eq(event));
        } else verify(jdbc).update(contains("published_at=NOW()"),eq(event));
    }
    @Test void acknowledgedJsonIsMarkedDelivered() throws Exception {run(false);}
    @Test void kafkaFailureRetainsSnapshotAndSchedulesRetry() throws Exception {run(true);}
}
