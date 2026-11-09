package com.dts.practice.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.dts.practice.simulation.SimulationReviewModel.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class SimulationReviewService {
    private final SimulationAccess access;
    private final SimulationBankClient banks;
    private final SimulationReviewRepository repo;
    private final SimulationThumbnailBatch thumbnails;

    public ReviewOverview overview(String license, String version) {
        var actor = access.read(false);
        var state = state(actor, license, version);
        return new ReviewOverview(state.scope(), state.bank().scoringVersion(), state.practiced(),
                state.review().size(), state.chapters(), state.version());
    }

    public ReviewPage list(String license, String version, Integer chapterId, String sort,
                           int page, int size, String expectedDatasetVersion) {
        var actor = access.read(true);
        if (!"BEST_SCORE_ASC".equals(sort) || page < 0 || page > 10000 || size < 1 || size > 12)
            throw SimulationFailure.invalid("Use sort=BEST_SCORE_ASC, page 0..10000 and size 1..12");
        if (expectedDatasetVersion != null && !expectedDatasetVersion.matches("[a-f0-9]{64}"))
            throw SimulationFailure.invalid("expectedDatasetVersion must be a SHA256 returned by this API");
        var state = state(actor, license, version);
        if (chapterId != null && state.chapters().stream().noneMatch(c -> c.chapterId() == chapterId))
            throw SimulationFailure.invalid("chapterId must belong to the published simulation bank");
        if (expectedDatasetVersion != null && !expectedDatasetVersion.equals(state.version()))
            throw SimulationLearningRules.conflict("SIMULATION_REVIEW_CHANGED",
                    "Review data changed; refresh the list before continuing pagination");
        var filtered = state.review().stream()
                .filter(s -> chapterId == null || s.chapterId() == chapterId).toList();
        int start = Math.min(filtered.size(), page * size);
        var selected = filtered.subList(start, Math.min(filtered.size(), start + size));
        var urls = thumbnails.resolve(actor, state.bank(), selected);
        var items = selected.stream().map(s -> item(state, s, urls.get(s.scenarioId()))).toList();
        return new ReviewPage(state.scope(), state.bank().scoringVersion(), state.practiced(),
                state.review().size(), state.chapters(), chapterId, sort, filtered.size(), items,
                new Pagination(page, size, filtered.size(), (filtered.size() + size - 1) / size),
                state.version());
    }

    private State state(SimulationAccess.Actor actor, String license, String version) {
        var code = SimulationLicensePolicy.require(license);
        var bank = banks.load(version);
        var completed = repo.completed(actor.userId(), bank);
        var bookmarks = repo.bookmarks(actor.userId(), bank.bankVersion());
        var published = bank.scenarios();
        int practiced = (int) published.stream().filter(s -> completed.containsKey(s.scenarioId())).count();
        var review = published.stream().filter(s -> {
            var stats = completed.get(s.scenarioId());
            return stats != null && stats.completedAttemptCount() > 0 && stats.bestScore() < 5;
        }).sorted(Comparator.<SimulationBank.Scenario>comparingInt(s -> completed.get(s.scenarioId()).bestScore())
                .thenComparingInt(SimulationBank.Scenario::scenarioNumber)).toList();
        var chapters = bank.chapters().stream().map(c -> new ReviewChapter(c.chapterId(), c.chapterId(),
                c.name(), (int) review.stream().filter(s -> s.chapterId() == c.chapterId()).count())).toList();
        var hash = new StringBuilder().append(actor.userId()).append('|').append(bank.bankVersion())
                .append('|').append(bank.scoringVersion()).append('|').append(bank.manifestSha256());
        for (var s : published) {
            var stats = completed.get(s.scenarioId());
            hash.append('|').append(s.scenarioId()).append(':')
                    .append(stats == null ? "null" : stats.completedAttemptCount() + "," + stats.bestScore())
                    .append(':').append(bookmarks.contains(s.scenarioId()));
        }
        return new State(bank, new SimulationLearningReads.Scope(actor.userId(), code, bank.bankVersion()),
                completed, bookmarks, practiced, review, chapters, sha256(hash.toString()));
    }

    private static ReviewItem item(State state, SimulationBank.Scenario scenario,
                                    SimulationThumbnailClient.Thumbnail thumbnail) {
        if (thumbnail == null) throw SimulationFailure.unavailable();
        var chapter = state.chapters().stream().filter(c -> c.chapterId() == scenario.chapterId())
                .findFirst().orElseThrow(SimulationFailure::notFound);
        var stats = state.completed().get(scenario.scenarioId());
        return new ReviewItem(scenario.scenarioId(), scenario.scenarioNumber(), scenario.chapterId(),
                chapter.chapterNumber(), chapter.chapterName(), scenario.title(), scenario.durationMs(),
                thumbnail.url(), thumbnail.expiredAt(), stats.bestScore(), 5, true,
                state.bookmarks().contains(scenario.scenarioId()), stats.completedAttemptCount());
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA256 is unavailable", ex);
        }
    }

    private record State(SimulationBank.Snapshot bank, SimulationLearningReads.Scope scope,
                         Map<UUID, CompletedStats> completed, Set<UUID> bookmarks, int practiced,
                         List<SimulationBank.Scenario> review, List<ReviewChapter> chapters, String version) {}
}
