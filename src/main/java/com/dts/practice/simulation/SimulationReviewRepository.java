package com.dts.practice.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.dts.practice.simulation.SimulationReviewModel.CompletedStats;

@Repository
@RequiredArgsConstructor
public class SimulationReviewRepository {
    private final JdbcTemplate jdbc;

    Map<UUID, CompletedStats> completed(UUID user, SimulationBank.Snapshot bank) {
        var result = new HashMap<UUID, CompletedStats>();
        jdbc.query("""
                SELECT scenario_id, count(*) AS total, max(score) AS best
                FROM simulation_attempts
                WHERE user_id=? AND bank_version=? AND scoring_version=? AND manifest_sha256=?
                  AND status='COMPLETED'
                GROUP BY scenario_id
                """, (org.springframework.jdbc.core.RowCallbackHandler) row ->
                result.put(row.getObject(1, UUID.class), new CompletedStats(row.getInt(2), row.getInt(3))),
                user, bank.bankVersion(), bank.scoringVersion(), bank.manifestSha256());
        return result;
    }

    Set<UUID> bookmarks(UUID user, String bank) {
        return new HashSet<>(jdbc.queryForList(
                "SELECT scenario_id FROM simulation_bookmarks WHERE user_id=? AND bank_version=?",
                UUID.class, user, bank));
    }
}
