package com.dts.practice.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.dts.practice.simulation.SimulationBookmarkModel.*;
import static com.dts.practice.simulation.SimulationReviewModel.CompletedStats;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class SimulationBookmarkService {
    private final SimulationAccess access;
    private final SimulationBankClient banks;
    private final SimulationReviewRepository repo;
    private final SimulationThumbnailBatch thumbnails;

    public BookmarkOverview overview(String license, String version) {
        var actor = access.read(false);
        var state = state(actor, license, version);
        return new BookmarkOverview(state.scope(), state.bank().scoringVersion(),
                state.bookmarked().size(), state.chapters(), state.version());
    }

    public BookmarkPage list(String license, String version, Integer chapterId, String sort,
                             int page, int size, String expectedDatasetVersion) {
        var actor = access.read(true);
        if (!"SCENARIO_ASC".equals(sort) || page < 0 || page > 10000 || size < 1 || size > 12)
            throw SimulationFailure.invalid("Use sort=SCENARIO_ASC, page 0..10000 and size 1..12");
        if (expectedDatasetVersion != null && !expectedDatasetVersion.matches("[a-f0-9]{64}"))
            throw SimulationFailure.invalid("expectedDatasetVersion must be a SHA256 returned by this API");
        var state = state(actor, license, version);
        if (chapterId != null && state.chapters().stream().noneMatch(c -> c.chapterId() == chapterId))
            throw SimulationFailure.invalid("chapterId must belong to the published simulation bank");
        if (expectedDatasetVersion != null && !expectedDatasetVersion.equals(state.version()))
            throw SimulationLearningRules.conflict("SIMULATION_BOOKMARKS_CHANGED",
                    "Bookmark data changed; refresh the list before continuing pagination");
        var filtered = state.bookmarked().stream()
                .filter(s -> chapterId == null || s.chapterId() == chapterId).toList();
        int start = Math.min(filtered.size(), page * size);
        var selected = filtered.subList(start, Math.min(filtered.size(), start + size));
        var urls = thumbnails.resolve(actor, state.bank(), selected);
        var items = selected.stream().map(s -> item(state, s, urls.get(s.scenarioId()))).toList();
        return new BookmarkPage(state.scope(), state.bank().scoringVersion(), state.bookmarked().size(),
                state.chapters(), chapterId, sort, filtered.size(), items,
                new SimulationReviewModel.Pagination(page, size, filtered.size(), (filtered.size() + size - 1) / size),
                state.version());
    }

    private State state(SimulationAccess.Actor actor, String license, String version) {
        String code = SimulationLicensePolicy.require(license);
        var bank = banks.load(version);
        // The same Card02 reader queries the existing Card01 tables; there is no second bookmark store.
        var completed = repo.completed(actor.userId(), bank);
        var marks = repo.bookmarks(actor.userId(), bank.bankVersion());
        var selected = bank.scenarios().stream().filter(s -> marks.contains(s.scenarioId()))
                .sorted(Comparator.comparingInt(SimulationBank.Scenario::scenarioNumber)).toList();
        var chapters = bank.chapters().stream().map(c -> new BookmarkChapter(c.chapterId(), c.chapterId(), c.name(),
                (int) selected.stream().filter(s -> s.chapterId() == c.chapterId()).count())).toList();
        var hash = new StringBuilder().append(actor.userId()).append('|').append(bank.bankVersion())
                .append('|').append(bank.scoringVersion()).append('|').append(bank.manifestSha256());
        for (var s : selected) {
            var stats = completed.get(s.scenarioId());
            hash.append('|').append(s.scenarioId()).append(':')
                    .append(stats == null ? "null" : stats.completedAttemptCount() + "," + stats.bestScore());
        }
        return new State(bank, new SimulationLearningReads.Scope(actor.userId(), code, bank.bankVersion()),
                completed, selected, chapters, sha256(hash.toString()));
    }

    private static BookmarkItem item(State state, SimulationBank.Scenario s,
                                     SimulationThumbnailClient.Thumbnail thumbnail) {
        if (thumbnail == null) throw SimulationFailure.unavailable();
        var chapter = state.chapters().stream().filter(c -> c.chapterId() == s.chapterId())
                .findFirst().orElseThrow(SimulationFailure::notFound);
        var stats = state.completed().get(s.scenarioId());
        return new BookmarkItem(s.scenarioId(), s.scenarioNumber(), s.chapterId(), chapter.chapterNumber(),
                chapter.chapterName(), s.title(), s.durationMs(), thumbnail.url(), thumbnail.expiredAt(),
                stats != null && stats.completedAttemptCount() > 0, stats == null ? null : stats.bestScore(),
                5, true, stats == null ? 0 : stats.completedAttemptCount());
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA256 is unavailable", error);
        }
    }

    private record State(SimulationBank.Snapshot bank, SimulationLearningReads.Scope scope,
                         Map<UUID, CompletedStats> completed, List<SimulationBank.Scenario> bookmarked,
                         List<BookmarkChapter> chapters, String version) {}
}
