# Card05 — random simulation mock exams

Contract: [simulation-exams.openapi.json](simulation-exams.openapi.json). Candidate backend implementation; release acceptance requires the owner's Java21/Maven/PostgreSQL run, V32 migration, actual Practice→Media/Gateway runtime and frontend acceptance. Examples in this document are schematic, not runtime evidence.

All paths start `/api/v1/simulation-exams`. Use the existing authenticated Practice Gateway route; preserve the path and original bearer token. Native port/config and Gateway prefix come from the existing deployment. There is no frontend branch change in this patch.

## Rules and authorization

RANDOM chooses ten distinct eligible videos from the current published 120-scenario bank. There is no chapter quota, theory timer or critical-question rule. Each position scores 0–5; total is 0–50; the current policy passes at 35. `policyVersion`, threshold, scoring/content/media snapshots and order are pinned at creation. The backend returns `passed`/`PASSED|FAILED` only in a valid completed result. Frontend displays the returned outcome and never recomputes it. PRESET belongs to Card06 and is rejected here.

Every endpoint requires signed Identity JWT rights `simulations:read` and `simulations:exam`. Create and current/review playback additionally require `media:read`. Learning and theory permissions alone do not grant exam access. Admin has no ownership bypass. The original JWT subject owns all reads/writes; request bodies cannot specify another user, scenario order, score or outcome. Existing `practice:read` may also be required by the deployed Gateway. Identity V14 and Media Card05 kit2 are prerequisites.

## Calls

| Method | Suffix | Response data |
|---|---|---|
| GET | `/preparation?licenseClass=B` | Policy/current bank and optional active session |
| POST | `/sessions` | Session; requires one UUID `Idempotency-Key` header |
| GET | `/sessions/active` | `{session: Session|null}` |
| GET | `/sessions/{id}` | Session |
| GET | `/sessions/{id}/current/playback` | Scoped current video URL |
| POST | `/sessions/{id}/current/restart` | Session with a new playback nonce |
| POST | `/sessions/{id}/current/start` | Session |
| POST | `/sessions/{id}/current/heartbeat` | Session |
| PUT | `/sessions/{id}/reaction` | Session; first valid reaction only |
| POST | `/sessions/{id}/complete-current` | Session; verified end only |
| POST | `/sessions/{id}/submit` | Completed Session; read result separately |
| POST | `/sessions/{id}/cancel` | Cancelled Session |
| GET | `/sessions/{id}/result` | Immutable result with ten scores and outcome |
| GET | `/sessions/{id}/review` | Result and ten frozen review items |
| GET | `/sessions/{id}/review/{position}/playback` | Renewed selected review video URL |

Success is the existing `{success,message,data,timestamp,traceId}` envelope with `Cache-Control: no-store`. Optional query `licenseClass` on session calls changes supported-license presentation only; ownership and pinned bank remain the session's. Supported car/trailer licenses share this bank; motorcycle classes are denied. Use the same query on exact event retries.

Create body: `{ "mode":"RANDOM", "licenseClass":"B", "bankVersion":"dts-simulation-120-card01-v1" }`. `bankVersion` is optional; if supplied it must still be current. Same creation key/body recovers the existing session even after its state changes. Another key while IN_PROGRESS or READY_TO_SUBMIT returns HTTP409 `ACTIVE_SESSION_EXISTS` with `activeSessionId`. Read/resume or explicitly cancel that session before creating another.

Event body for start/heartbeat/reaction/complete: `{ "eventId":"<uuid>", "position":1, "playbackEventId":"<current nonce>", "mediaTimeMs":0 }`. Restart omits `mediaTimeMs`. Submit/cancel body contains only `eventId`. Use canonical UUIDs; integer positions1–10 and integer media time in the video duration. Unsupported fields are rejected. Retry the same logical write with the same eventId/body; reuse with different content is409. After timeout read session/result before issuing a new logical operation. Stored acknowledgements can describe an earlier state: never regress a newer local state after a delayed retry; refresh session when uncertain.

## Player integration required

The current frontend exam controller needs START, HEARTBEAT and RESTART commands in addition to its existing four commands. Merely wiring the existing source port cannot satisfy this playback contract.

1. Read the owned session. Resolve its current playback. Start the player at zero and send `current/start` with mediaTimeMs0 and its current nonce. Begin input only after successful start.
2. Send heartbeats every3seconds while media plays continuously. Backend validates server elapsed time, monotonic media time and gaps no greater than12seconds. Allow ordinary request latency; do not invent client progress when paused/buffering. There is no countdown/deadline.
3. Space/button sends REACTION once. Show pending until backend returns `responseRecorded:true`. Preserve the same eventId on retry. Keyboard and button cannot replace an already recorded response.
4. `ended` triggers COMPLETE_CURRENT with actual integer media position. The server requires verified continuous end (250ms tolerance). Network/buffering/seek failures never finish a video or create a zero score. Display a recoverable video error.
5. On reload, pause, seek or broken continuity, explicitly RESTART the current position, then resolve playback and START from zero with the **new** nonce. The saved first reaction stays recorded; disable input if already true. Stale requests for an old nonce cannot change the new run. No pause/resume-at-offset is supported in this exam flow.
6. A URL renewal during uninterrupted playback keeps nonce and progress. Preserve the live player's position. `restartAtMs:0` describes reload/restart policy; renewal alone is not permission to reset or skip verified progress.
7. After COMPLETE_CURRENT10 the session becomes READY_TO_SUBMIT. Submit, then GET result. IN_PROGRESS/READY_TO_SUBMIT/CANCELLED have no result outcome. Completed/cancelled sessions cannot resume; completed results cannot cancel.

Session/Playback deliberately omit original scenario IDs/numbers/titles/chapters/hints/timelines/points. Session exposes ten ordinal positions and playback policy. Review is available only after completion and uses frozen snapshots even when the current bank is unavailable. Each review item has `playbackPath`; lazily resolve only the selected position. Adapt the frontend review validator that currently requires ten eager playback objects. Review playback renewal writes no exam/learning rows.

## Failures and scope

Exam business errors use `{success:false,code,message,activeSessionId,timestamp}`. Existing shared JWT/security/validation errors keep their existing envelopes; use HTTP status and `code` where provided. 400 invalid body/UUID;401 missing/invalid JWT;403 missing rights/unsupported license;404 unknown or non-owned session;409 state/event/nonce/position conflict, incomplete playback or changed media;503 unavailable source/storage/transport. Shared continuity codes include `SIMULATION_NOT_PLAYING`, `SIMULATION_PLAYBACK_INTERRUPTED`, `SIMULATION_PLAYBACK_DISCONTINUITY`. Read session and explicitly restart after continuity conflicts. Never turn409/503 into automatic video completion.

Practice signs short-lived eligibility/current/review tickets accepted by Media kit2. They bind actor, pinned bank/manifest, session, ordinal, playback nonce, source checksum/durations and verified ETag. Frontend receives only scoped signed URLs, never service tickets or signing keys. Do not log tokens, passwords, signed URLs or URL query strings.

V32 adds only `simulation_exam_sessions`, `simulation_exam_positions`, `simulation_exam_events`, `simulation_exam_results`. This flow makes no Card01 learning/progress/bookmark/cursor or result-outbox writes. Existing theory/business services and migrations stay unchanged. Content Builder Card04 guide's `mockExam.available=false` remains unchanged until a separate coordinated release; it is not evidence that this candidate has passed runtime/UI acceptance.

## Acceptance still required

The candidate runs the earlier631 regression/security tests plus new API/protocol/access/PostgreSQL/migration tests. Real runtime must cover two users, native and Gateway paths, actual ten-video sequential playback, duplicate/lost-response submit, scoped review Range206, unsigned403, pinned results, and totals34/35/50/0. C05-T36 requires actual Gateway result evidence; MockMvc does not prove deployed Gateway routing. Browser SIM07–SIM10 acceptance belongs to the frontend team.


## Controlled video shortcuts — explicit opt-in

The legacy flow above remains the default. See [simulation-video-shortcuts.md](simulation-video-shortcuts.md) and its OpenAPI for V34-backed enable/skip-intro/advance-after-reaction. Only an acknowledged skip grants START at10000; allowSeek remains false. An acknowledged advance may complete one exam position before ended without inventing media duration. Existing scoring and history are unchanged.
