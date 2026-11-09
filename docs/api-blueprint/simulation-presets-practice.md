# Card06 — Practice PRESET contract

Candidate extends Card05. Content Builder15-set publication/runtime has been accepted separately; this Practice candidate still needs owner Maven900/43, Apply/V33, HTTP runtime and frontend acceptance. Existing theory routes/data and Card01–03 are unchanged. No new Identity/Media/Gateway patch is required by this source change.

Native prefix: `/api/v1/simulation-exams`; Gateway prefix: `/api/practice/v1/simulation-exams`. Gateway must be verified by the subsequent runtime. Authentication, no-store envelope, Idempotency-Key and playback/event protocol follow Card05. Read requires `simulations:read` + `simulations:exam`; create/current/review playback additionally require `media:read`. Account permission context and supported-license validation still apply.

## SIM-11 and SIM-12

`GET /presets?licenseClass=B` returns `ApiResponse<ListView>` with `scope`, `bankVersion`, `totalPublishedSets`, ordered `items`, and nullable `activeSession` (any globally active mode/edition). Empty eligible publication returns `items:[]`, `totalPublishedSets:0`; missing/unavailable dependency is an error rather than a fabricated empty catalogue.

Each item contains:

| Field | Meaning |
|---|---|
| presetSetId / presetVersionId | Stable set UUID and exact currently eligible immutable version UUID |
| code / title / displayOrder / status | Published identity/order; status is `PUBLISHED` |
| totalScenarios / maxTotalScore / passingScore | 10 / 50 / 35 |
| hasCompletedAttempt | Completion exists for this actor and this exact version |
| latestCompletedSessionId / latestCompletedScore / latestCompletedPassed / latestCompletedAt | One paired latest completed attempt, ordered completedAt then session UUID; all null when absent |
| activeSessionId | Active session of this exact version; nullable |
| attemptStatus | `IN_PROGRESS` if exact-version active; otherwise `NOT_STARTED`, `PASSED`, or `FAILED` from latest completed |

`GET /presets/{presetSetId}?licenseClass=B` returns `ApiResponse<Detail>`: `scope`, `preset` (same Item), `mode:PRESET`, `maxScorePerScenario:5`, `canStart`, nullable `unavailableReason`, nullable `activeSession`. Unknown/unpublished set returns404. No GET creates sessions or issues media URLs. Both reads execute in a PostgreSQL read-only repeatable-read transaction for consistent active/latest/frame views.

`canStart` means the selected version is published/eligible and no globally active exam occupies the slot. If blocked, `unavailableReason:ACTIVE_SESSION_EXISTS` and `activeSession` allow the learner to resume/cancel the exact existing session. Live READY/object integrity is verified during creation; GET does not call Media with learner credentials or reserve a session. A changed/unavailable video can therefore reject Create and leave no exam.

During retake, `activeSessionId` and the prior latest-completed fields coexist. Score45 followed by a completed score30 shows30 and the second session ID. Cancelled, in-progress and ready-to-submit sessions never replace latest completion. New version has no inherited grade. An old-version active session remains in root `activeSession` with its pinned old metadata; its ID is not relabelled as the new edition's `activeSessionId`. State is shared across supported car/trailer license views, as in Card05.

Before submit, no list/detail/active session exposes scenario IDs/numbers/order, scoring timeline, hints, per-position scores, media UUIDs or service tickets.

## PRESET creation

```json
{"mode":"PRESET","licenseClass":"B","presetSetId":"<canonical-uuid>","presetVersionId":"<canonical-uuid>"}
```

Use `POST /sessions` with exactly one canonical UUID `Idempotency-Key`. Optional `bankVersion` must match current bank. `licenseClass` remains required from the shared Card05 request scope in addition to the PDF's minimal preset selection; no implicit default B is applied. RANDOM retains its old fields/hash and cannot attach preset IDs. Neither mode accepts userId, scoring, scenario selection or order.

Creation holds the existing per-user lock, checks previous create receipt, blocks any global active session, validates current bank/source version and verifies all10 fixed videos. Any unready position aborts; no replacement/shuffle. The current preset publication is read again after Media verification before inserting local session/positions. A publication change rejects the request atomically.

The session pins mode, version/ref/title, bank manifest, scoring, policy35 and all10 frame snapshots in source order. Repeat of the same original create key/payload returns its original session even after the source is archived; a new key must select a currently eligible version. Same key with changed mode/license/bank/set/version returns409 `SIMULATION_EXAM_EVENT_CONFLICT`.

PRESET adds `preset` metadata and top-level aliases `presetSetId`, `presetVersionId`, `presetTitle` to Session/Result. `preset` contains set/version/code/title/displayOrder/manifestSha256/sourceVersion/sourceSha256. These are content identity fields, not access credentials. RANDOM omits these new nullable properties, preserving old wire shape. Stored pre-Card06 result/event JSON remains readable; derived aliases in stored PRESET JSON are ignored on decode in favor of canonical `preset`.

## Shared exam behavior and history

Every START/heartbeat/restart/reaction/complete-current/submit/cancel/current-playback/result/review/review-playback path is the existing Card05 path. The protocol still requires continuous elapsed-time playback and nonce, keeps the first valid reaction, rejects stale event IDs/nonces and forbids skip/early submit. PRESET does not add a second scoring or playback engine.

Submit returns modePRESET and pinned metadata; 10 completed valid frames are required. Score34→FAILED,35→PASSED,50→PASSED, no reaction→0. Completed/ cancelled audit is retained. A retake uses a new create key/session ID and preserves previous result/review. History reads use session snapshots regardless of current source/version/policy. A physically changed historical video still fails closed during shared Media playback; no silent replacement. Result and review are strictly owned by current authenticated actor.

## Errors and storage

| Condition | HTTP / code |
|---|---|
| Missing/forged JWT or insufficient rights | Existing401/403 authentication/authorization contract |
| Unsupported motorcycle license | 403 / existing simulation forbidden code |
| Invalid/missing/spoofed request fields or IDs | 400 / SIMULATION_EXAM_INVALID_REQUEST |
| Unknown eligible set/version | 404 / exam not-found or internal simulation-source not-found |
| Another global active session | 409 / ACTIVE_SESSION_EXISTS with recoverable activeSessionId |
| Selected set/version mismatch or publication changed | 409 / SIMULATION_PRESET_VERSION_CHANGED, or404 when now ineligible |
| Any fixed video not READY | 409 / SIMULATION_PRESET_MEDIA_UNAVAILABLE |
| Dependency failure/malformed source | 503 / existing simulation or exam unavailable code |
| Pinned historical media changed | Existing409 / SIMULATION_EXAM_MEDIA_CHANGED |

V33 only extends `simulation_exam_sessions`: mode constraint, nullable set/version UUIDs, immutable safe metadata JSON, latest-completed index and reference guard. Existing shared positions/events/results, unique active index and old SQL remain unchanged. There is no new theory table, content import into Practice, learning-progress/bookmark write, package install, auto-migration or UI-release flag change.

## Candidate verification map

| Area | New tests |
|---|---|
|15 definitions, exact order/quota/coverage, invalid metadata/source/scoring, empty/duplicate catalogue|SimulationPresetContractTest24|
|JWT/grants/licenses, no-store GETs, strict PRESET request, no actor/order spoof, shared service binding|SimulationPresetApiTest20|
|Physical PostgreSQL create/order/idempotency/rollback/concurrency, one global active, READY fail, publication race, boundaries, latest45→30, retake/cancel, version history, ownership, old/new JSON compatibility, read-only GET snapshots|SimulationPresetPostgresTest28|
|Dedicated signed source token domain/type/audience/purpose, exact RPC paths,404/503/redirect/body bounds|SimulationPresetSourceClientTest6|
|Readonly V32 validation, onlyV33 upgrade, protected old random session/result/receipt rows, old history checksums, partial-schema refusal and idempotency|SimulationPresetMigrationToolPostgresTest8|

These86 cases run with814 existing cases in43 canonical suites. They are source-level verification targets until actual owner results are returned. Native/Gateway real JWT, Practice→Content service-token integration, Media/MinIO Range and real elapsed PRESET scoring require the subsequent HTTP runner. OpenAPI `simulation-presets-practice.openapi.json` includes the existing shared17 operations with PRESET overlays; browser/UI acceptance belongs to the frontend team on their current branch.


## Controlled video shortcuts — explicit opt-in

The legacy flow above remains the default. See [simulation-video-shortcuts.md](simulation-video-shortcuts.md) and its OpenAPI for V34-backed enable/skip-intro/advance-after-reaction. Only an acknowledged skip grants START at10000; allowSeek remains false. An acknowledged advance may complete one exam position before ended without inventing media duration. Existing scoring and history are unchanged.
