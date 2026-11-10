# Follow-up 2026-11-04

This source delta extends the owner-accepted V34 protocol; the new delta requires its own candidate/runtime checks. No migration or scoring change.

# Controlled video shortcuts v1 — candidate contract

This source implements a new bounded protocol, **not yet owner Maven/HTTP/browser accepted**. Requires V34. Old learning/exam DTOs, scoring windows/formula, source snapshots,35/50 policy, first reaction, PRESET order/ref/history and global active slot remain. Media, Identity, Content and frontend source are not changed.

## Opt in and capabilities

New target-scoped routes (native Practice; Gateway adds `/api/practice` in place of `/api`):

| Kind | Prefix | Routes |
| --- | --- | --- |
| LEARNING | `/api/v1/simulations/attempts/{attemptId}/shortcuts` |GET state, POST /enable, POST /skip-intro |
| EXAM RANDOM/PRESET | `/api/v1/simulation-exams/sessions/{sessionId}/shortcuts` |GET state, POST /enable, POST /skip-intro, POST /advance-after-reaction |

All require query `licenseClass` with a supported project license. Backend owner checks are unchanged. Learning needs simulations:read+learn; exams need simulations:read+exam. Gateway also practice:read. Existing playback still requires media:read. No new Identity grant or service credential is introduced.

Old creation payloads and request hashes stay unchanged. A new client creates the attempt/session, then explicitly ENABLEs **before the first START**. A resolved but unstarted media grant is allowed. Existing PLAYING/started/partly completed legacy targets cannot be silently upgraded; keep their old flow or let the user explicitly cancel and create a new target. ENABLE never changes scoring policy. Retrying the same enable event recovers it; a new enable event on an already enabled active target is a no-op and does not reset usage/history.

GET returns supported/enabled/canEnable/protocolVersion/reason, shortcutRevision, stateFingerprint, position/nonce/startAtMs, skipIntro and advanceAfterReaction capabilities, nullable reactionAcknowledgement, completion reasons and the normal current attempt OR session. GET is read-only repeatable-read, never enables or issues media URLs. `supported:false` with MIGRATION_REQUIRED/FEATURE_DISABLED is distinct from an enabled target. `simulation.video-shortcuts-enabled` defaults true but no target is auto-enabled. Existing learning/exam feature flags still apply.

Old clients and old target DTOs remain the default; no shortcut fields are added to Session/Attempt. Clients opt in by endpoint, not by setting allowSeek=true. Only the explicitly enabled target can use new actions. Source DTO/OpenAPI and request/response excerpts are in the adjacent files. `Playback.restartAtMs` for exams remains0 on legacy/full restart; it may be10000 only for a new approved checkpoint.

## Request shapes

ENABLE: `{eventId,position,playbackEventId}`. Learning position is1; exam is current1..10.

SKIP: same fields plus `mediaTimeMs` — the actual integer original-video currentTime at click, never a fabricated target/duration. Target is fixed server-side10000; clients cannot send targetMs/duration/score/actor/order.

ADVANCE: same fields plus `reactionAcknowledgement` — opaque64-hex confirmation returned in GET shortcuts state **after the first reaction has committed**. The original reaction eventId or an optimistic responseRecorded flag is not this confirmation. A lost reaction HTTP ACK is reconciled with GET state; that response itself confirms storage. The confirmation is target/nonce/first-reaction bound and derived with a private per-target key never exported. Do not log/persist the confirmation.

Each intent gets a canonical event UUID. Exact retry keeps original body/query/event. Changed payload, position, nonce, proof or license under the same event is409 EVENT_CONFLICT. New commands reserve the existing event namespace, so their event cannot be reused for an old START/HB/reaction/complete/submit/cancel command.

## Skip transition — real clock and absolute milliseconds

1. ENABLE before START. Resolve existing playback, START at0, wait ACK and play normally. Skip is available only while actual verified playback is live, before any first reaction, before the intro has passed, and once per continuous playback generation. For EXAM only, an acknowledged full RESTART to checkpoint0 re-arms Skip when the stored first reaction is null; it becomes available after START on the fresh nonce. A stored reaction at0ms does not re-arm it.
2. Capability uses the pinned snapshot duration and earliest scoring boundary t5StartMs. Duration must exceed10000 and earliest scoring must be at/after10000. Otherwise available=false with VIDEO_TOO_SHORT or SCORING_BEFORE_INTRO_END; maxSafeTargetMs is bounded by duration/scoring and10000. Do not silently jump to a smaller number: the button requests exactly00:10. The source represents danger/scoring timing through t5..t0; no video-image analysis or alteration of those windows is performed.
3. On click, pause/quarantine the current media and stop producing old heartbeat/reaction/ended intents. Send SKIP with actual last media time. Backend validates that position against the existing elapsed/monotonic/gap checks. It rotates nonce, clears the media grant/segment anchor, saves checkpoint/lastMs10000 and marks skip used. It does **not** mark watched/complete or add a synthetic wall-clock interval.
4. ACK contains a stable operation receipt and authoritative current state with new nonce/startAtMs10000. Learning becomes INTERRUPTED with SHORTCUT_CHECKPOINT; exam current becomes NOT_STARTED. Do not accept reaction during this transition.
5. Resolve playback for the new nonce. Seek only the authorized transition to10000, wait for the matching seeked/load state, and START with mediaTimeMs10000. Wait for START ACK and actual playing before enabling reaction/HB. New segment_started_at is actual server time; expected original media position is **checkpoint + elapsed**, not a timestamp moved into the past.
6. Subsequent media times are original-video times. At video17.432s send17432. Backend stores17432 and applies the exact original scoring function; never send7432 or27432. COMPLETE can still be used at real ended, using actual remaining elapsed plus checkpoint credit. No client fabricates duration/ended/HB.

allowSeek remains false. Only that acknowledged seek with its new nonce/checkpoint is authorized. A manual scrub, stale nonce or further skip is rejected. Learning keeps once-per-attempt usage. Exam restart re-arms only the current unanswered position on its fresh START0 generation; a second skip in that same generation stays denied. A stored reaction, including0, never re-arms. This remains a fixed jump to00:10, not a repeated+10s button. The backend does not expose full scoring windows in exam capabilities.

## Next after confirmed reaction

After the existing REACTION/response ACK, GET shortcuts state. Only if advanceAfterReaction.available=true and an opaque reactionAcknowledgement exists may the client enable/send Next. Pending/failed/lost ACK without successful reconciliation does not permit Next. `firstResponseMs:0` is a real stored reaction; null is not. Next is not exposed for learning targets.

Under the same exam advisory/row lock as heartbeat/reaction/restart/complete/submit, backend verifies current position/nonce, ownership/license, enabled protocol and confirmation. It freezes the stored reaction's score with the pinned timeline, completes exactly this frame with **ADVANCE_AFTER_REACTION**, leaves last_media_time at the actual observed value, increments count once and exposes the next precreated nonce. It does not record a fake ended or watched-to-duration. At position10 returns READY_TO_SUBMIT; SUBMIT remains a separate existing command. No result before COMPLETED.

Double click with a new event on the old position is rejected; exact retry returns the immutable original receipt plus **fresh current state**, including after a later position or submit. Receipt equality, not byte equality of the whole response, is the idempotency promise. Stale ended for an already completed frame is an existing idempotent no-op; old HB/reaction cannot affect the next frame. First reaction cannot be replaced by Next or later requests. Completion reasons per ordinal are in state and persisted; natural completion is ENDED.

## Recovery, ordering, reload and URL renewal

- `receipt.appliedRevision` and `state.shortcutRevision` order **new shortcut actions only**, not every legacy heartbeat/reaction. `stateFingerprint` is an equality stamp, not a sortable clock. Do not infer unchanged session just because shortcutRevision is unchanged.
- Keep one coordinator/write queue for the current target and media generation. Serialize HB/reaction/skip/restart/complete/Next/submit. Response for an older position/nonce or abandoned request generation must not overwrite current UI. Within one position, never clear a confirmed reaction because an older HB ACK has responseRecorded=false. After uncertainty, fetch authoritative state instead of merging stale snapshots.
- Lost SKIP/Next ACK: keep exact event/body, GET shortcuts state and retry original intent. Receipt is constant; state is refreshed atomically. Do not create a new intent or seek/advance optimistically. A lost reaction ACK is reconciled by GET state, which supplies confirmation only after committed storage.
- Learning interrupt/resume keeps its last verified absolute position and rotates nonce as before. After skipped playback at13s, interrupt/resume STARTs at verified13000, not3000. URL renewal itself does not change anchor or authorize seek.
- Exam full RESTART retains first reaction, resets checkpoint/lastMs to0 and rotates nonce. Fetch state again; confirmation is nonce-bound. If first reaction is null, START0 on the fresh restart nonce can offer Skip again. If reaction is stored (including0), Skip remains denied. The old skip receipt is retained; exact retry returns that receipt plus fresh state and cannot reapply a jump. An old RESTART retry is also a no-op, even after a later Skip. Legacy reload flow remains full restart0. If SKIP was committed but checkpoint START has never occurred, the acknowledged pending transition may be completed at10000; after active playback is lost, use the normal restart flow, not saved local currentTime.
- New URL for the same live segment does not reset its clock. If swapping src/remount/buffering breaks actual continuity, interrupt/resume (learning) or restart (exam), rather than queueing fake or stale media samples. Seeking outside the one acknowledged transition keeps the previous interruption behavior.
- Neither unload nor route change auto-cancels. Review stays existing GET-only result/review/review-playback; no new learning, bookmark or cursor writes from exam/review.

## Error handling

New failures are no-store `{success:false,code,message,timestamp}`. Status first: inherited Identity/owner/license/position failures may use existing envelopes/codes.

| Status/code suffix (SIMULATION_SHORTCUT_) | Meaning/recovery |
| --- | --- |
|400 INVALID_REQUEST |Strict fields/UUID/int/proof/query; fix shape, not payload under a reused event |
|409 NOT_ENABLED / POLICY_LOCKED |No opt-in, or legacy target already started; preserve old flow |
|409 NOT_PLAYING / INTRO_ALREADY_PASSED / ALREADY_USED / REACTION_ALREADY_RECORDED |Skip not available; refetch state, do not seek |
|409 VIDEO_TOO_SHORT / SCORING_BEFORE_INTRO_END |Would lose a scoring opportunity; display capability reason |
|409 REACTION_ACK_REQUIRED |No committed reaction or no matching server confirmation; reconcile before Next |
|409 PLAYBACK_CHANGED / POSITION_CHANGED / TERMINAL |Stale generation/position/terminal target; read current state |
|409 EVENT_CONFLICT |Event was used for another command/payload; no mutation |
|503 UNAVAILABLE |Feature/schema not ready or dependency issue; do not fake fallback state after an uncertain write |

GET capability reasons MIGRATION_REQUIRED, FEATURE_DISABLED, NOT_ENABLED and LEGACY_ALREADY_STARTED are not instructions to modify DB/roles in frontend. New version depends on V34 and a server restart with installed source; old source/core still works with V33 for legacy sessions, but cannot advertise/use shortcuts.

## Prior publication-stage validation status

Revision 2 requires 996 JUnit/PostgreSQL cases in 47 suites: the 900-case regression pack plus 96 new cases. Three archived regression source files are hash-verified from the owner-provided accepted Card06 audit and staged only in the isolated candidate, never installed into the working tree. The owner passed all 93 shortcut cases in revision 1, but the failed overall run is not acceptance of this revision. All old tests and migrations V1–V33 are unchanged. Source SQL adds V34 only. That unapplied migration also serializes the existing practice-result enqueue function by locking its source exam before the outbox existence check; this fixes the duplicate event primary-key race observed in the unchanged Screen13 concurrency test. Existing V28 is untouched, function-body drift is rejected, and historical events/rows are not rewritten. Local compiled production pure helper checks and syntax parsing are recorded separately; full owner Java21/Maven/PostgreSQL and HTTP acceptance must be returned before declaring backend accepted. Browser/real video two-way acceptance remains frontend-owned. Preserve the agreed0.6cm spacing and transparent white-text controls1.5cm from right/bottom.
