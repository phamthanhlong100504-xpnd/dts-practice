package com.dts.practice.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.dts.practice.simulation.SimulationReviewModel.*;

@Service
@RequiredArgsConstructor
public class SimulationReplayService {
    private final SimulationAccess access;
    private final SimulationLearningRepository repo;
    private final SimulationBankClient banks;
    private final SimulationPlaybackClient media;

    @Transactional(readOnly = true)
    public Replay replay(UUID attemptId) {
        var actor = access.read(true);
        var row = repo.get(actor.userId(), attemptId, false);
        if (!"COMPLETED".equals(row.status))
            throw SimulationLearningRules.conflict("SIMULATION_REPLAY_NOT_READY",
                    "Read-only replay is available only for a completed owned attempt");
        var bank = banks.load(row.bank);
        var scenario = SimulationBank.scenario(bank, row.scenarioId);
        if (!row.manifest.equals(bank.manifestSha256()) || !row.scoring.equals(bank.scoringVersion())
                || !row.snapshot.equals(scenario))
            throw SimulationLearningRules.conflict("SIMULATION_BANK_CHANGED", "Pinned replay binding differs");
        // This never calls learning.playback(), saves a row, or refreshes a learning lease.
        var url = media.resolve(actor, row);
        return new Replay(row.id, row.scenarioId, row.bank, row.scoring, url.url(), url.expiredAt(),
                "video/mp4", row.duration, true, new ReplayPolicy(true, true, true));
    }
}
