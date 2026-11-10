// Reference wire types; not an installed frontend patch.
// Existing Session/Attempt wire shape stays stable; new capability/ACK is separate.

export type DtsVideoScope = {
 userId: string;
 licenseClass: string;
 bankVersion: string;
};

export type DtsVideoPolicy = {
 totalScenarios: 10;
 maxScenarioScore: 5;
 maxTotalScore: 50;
 passingScore: number;
 policyVersion: string;
};

export type DtsVideoPosition = {
 position: number;
 status: "NOT_STARTED" | "CURRENT" | "COMPLETED";
};

export type DtsVideoPlaybackPolicy = {
 allowPause: boolean;
 allowSeek: boolean;
 allowFullscreen: boolean;
 allowHintsWhilePlaying: boolean;
 heartbeatIntervalMs: number;
 maxHeartbeatGapMs: number;
};

export type DtsVideoSession = {
 scope: DtsVideoScope;
 sessionId: string;
 mode: "RANDOM" | "PRESET";
 status: "IN_PROGRESS" | "READY_TO_SUBMIT" | "COMPLETED" | "CANCELLED";
 bankVersion: string;
 scoringVersion: string;
 policy: DtsVideoPolicy;
 totalScenarios: number;
 completedScenarios: number;
 currentPosition: number;
 maxTotalScore: number;
 responseRecorded: boolean;
 playbackEventId: string;
 playbackState: "NOT_STARTED" | "READY" | "PLAYING" | "INTERRUPTED" | "FINISHED";
 requiresRestart: boolean;
 playbackPolicy: DtsVideoPlaybackPolicy;
 positions: Array<DtsVideoPosition>;
 startedAt: string;
 completedAt?: (string) | null;
 preset?: DtsVideoPresetRef;
 presetSetId?: string;
 presetVersionId?: string;
 presetTitle?: string;
};

export type DtsVideoPreparation = {
 scope: DtsVideoScope;
 bankVersion: string;
 policy: DtsVideoPolicy;
 activeSession?: (DtsVideoSession) | null;
};

export type DtsVideoActive = {
 session?: (DtsVideoSession) | null;
};

export type DtsVideoPlayback = {
 sessionId: string;
 position: number;
 playbackEventId: string;
 videoUrl: string;
 expiresAt: string;
 mimeType: "video/mp4";
 durationMs: number;
 responseRecorded: boolean;
 restartAtMs: number;
};

export type DtsVideoResultItem = {
 position: number;
 score: number;
 maxScore: number;
};

export type DtsVideoResult = {
 scope: DtsVideoScope;
 sessionId: string;
 mode: "RANDOM" | "PRESET";
 status: "COMPLETED";
 bankVersion: string;
 scoringVersion: string;
 policy: DtsVideoPolicy;
 totalScenarios: number;
 completedScenarios: number;
 totalScore: number;
 maxTotalScore: number;
 passingScore: number;
 passed: boolean;
 resultStatus: "PASSED" | "FAILED";
 startedAt: string;
 completedAt: string;
 items: Array<DtsVideoResultItem>;
 preset?: DtsVideoPresetRef;
 presetSetId?: string;
 presetVersionId?: string;
 presetTitle?: string;
};

export type DtsVideoSegment = {
 startMs: number;
 endMs: number;
 score: number;
 endInclusive: boolean;
};

export type DtsVideoReviewItem = {
 position: number;
 scenarioId: string;
 scenarioNumber: number;
 title: string;
 chapterName: string;
 score: number;
 maxScore: number;
 responseRecorded: boolean;
 firstResponseMs?: (number) | null;
 durationMs: number;
 scoringSegments: Array<DtsVideoSegment>;
 hazardSign: string;
 recognitionTip: string;
 scoringVersion: string;
 playbackPath: string;
};

export type DtsVideoReview = {
 result: DtsVideoResult;
 items: Array<DtsVideoReviewItem>;
};

export type DtsVideoCreate = ({
 mode: "RANDOM";
 licenseClass: string;
 bankVersion?: string;
}) | ({
 mode: "PRESET";
 licenseClass: string;
 bankVersion?: string;
 presetSetId: string;
 presetVersionId: string;
});

export type DtsVideoEvent = {
 eventId: string;
 position: number;
 playbackEventId: string;
 mediaTimeMs: number;
};

export type DtsVideoRestart = {
 eventId: string;
 position: number;
 playbackEventId: string;
};

export type DtsVideoCommand = {
 eventId: string;
};

export type DtsVideoExamError = {
 success: false;
 code: string;
 message: string;
 activeSessionId?: (string) | null;
 timestamp: string;
};

export type DtsVideoPreparationResponse = {
 success: true;
 message?: string;
 data: (DtsVideoPreparation) | null;
 timestamp?: string;
 traceId?: (string) | null;
};

export type DtsVideoActiveResponse = {
 success: true;
 message?: string;
 data: (DtsVideoActive) | null;
 timestamp?: string;
 traceId?: (string) | null;
};

export type DtsVideoSessionResponse = {
 success: true;
 message?: string;
 data: (DtsVideoSession) | null;
 timestamp?: string;
 traceId?: (string) | null;
};

export type DtsVideoPlaybackResponse = {
 success: true;
 message?: string;
 data: (DtsVideoPlayback) | null;
 timestamp?: string;
 traceId?: (string) | null;
};

export type DtsVideoResultResponse = {
 success: true;
 message?: string;
 data: (DtsVideoResult) | null;
 timestamp?: string;
 traceId?: (string) | null;
};

export type DtsVideoReviewResponse = {
 success: true;
 message?: string;
 data: (DtsVideoReview) | null;
 timestamp?: string;
 traceId?: (string) | null;
};

export type DtsVideoPresetRef = {
 presetSetId: string;
 presetVersionId: string;
 code: string;
 title: string;
 displayOrder: number;
 manifestSha256: string;
 sourceVersion: string;
 sourceSha256: string;
};

export type DtsVideoPresetItem = {
 presetSetId: string;
 presetVersionId: string;
 code: string;
 title: string;
 displayOrder: number;
 status: "PUBLISHED";
 totalScenarios: 10;
 maxTotalScore: 50;
 passingScore: 35;
 hasCompletedAttempt: boolean;
 latestCompletedSessionId: (string) | null;
 latestCompletedScore: (number) | null;
 latestCompletedPassed: (boolean) | null;
 latestCompletedAt: (string) | null;
 activeSessionId: (string) | null;
 attemptStatus: "NOT_STARTED" | "IN_PROGRESS" | "PASSED" | "FAILED";
};

export type DtsVideoPresetList = {
 scope: DtsVideoScope;
 bankVersion: string;
 totalPublishedSets: number;
 items: Array<DtsVideoPresetItem>;
 activeSession: ((DtsVideoSession)) | null;
};

export type DtsVideoPresetDetail = {
 scope: DtsVideoScope;
 preset: DtsVideoPresetItem;
 mode: "PRESET";
 maxScorePerScenario: 5;
 canStart: boolean;
 unavailableReason: (string) | null;
 activeSession: ((DtsVideoSession)) | null;
};

export type DtsVideoPresetListResponse = {
 success: true;
 message?: string;
 data: DtsVideoPresetList;
 timestamp?: string;
 traceId?: (string) | null;
};

export type DtsVideoPresetDetailResponse = {
 success: true;
 message?: string;
 data: DtsVideoPresetDetail;
 timestamp?: string;
 traceId?: (string) | null;
};

export type DtsVideoLearningResult = {
 attemptId: string;
 scenarioId: string;
 status: "COMPLETED";
 score: number;
 maxScore: 5;
 responseRecorded: boolean;
 firstResponseMs: (number) | null;
 videoDurationMs: number;
 bankVersion: string;
 scoringVersion: string;
 scoringSegments: Array<DtsVideoSegment>;
 hazardSign: string;
 recognitionTip: string;
};

export type DtsVideoLearningAttempt = {
 attemptId: string;
 scenarioId: string;
 bankVersion: string;
 scoringVersion: string;
 status: "CREATED" | "PLAYING" | "INTERRUPTED" | "COMPLETED" | "CANCELLED";
 playbackEventId: string;
 videoDurationMs: number;
 resumeAtMs: number;
 responseRecorded: boolean;
 firstResponseMs: (number) | null;
 requiresResume: boolean;
 createdAt: string;
 startedAt: (string) | null;
 completedAt: (string) | null;
 policy: DtsVideoPlaybackPolicy;
 result: ((DtsVideoLearningResult)) | null;
};

export type DtsVideoIntroCapability = {
 available: boolean;
 targetMs: 10000;
 maxSafeTargetMs: number;
 reason: (string) | null;
};

export type DtsVideoAdvanceCapability = {
 available: boolean;
 reason: (string) | null;
};

export type DtsVideoShortcutState = {
 targetKind: "LEARNING" | "EXAM";
 targetId: string;
 licenseClass: string;
 supported: boolean;
 enabled: boolean;
 canEnable: boolean;
 protocolVersion: "legacy-v1" | "video-shortcuts-v1";
 reason: (string) | null;
 shortcutRevision: number;
 stateFingerprint: string;
 position: number;
 playbackEventId: string;
 startAtMs: number;
 skipIntro: DtsVideoIntroCapability;
 advanceAfterReaction: DtsVideoAdvanceCapability;
 reactionAcknowledgement: (string) | null;
 completionReason: ("ENDED" | "ADVANCE_AFTER_REACTION") | null;
 completionReasons: Record<string, "ENDED" | "ADVANCE_AFTER_REACTION">;
 attempt: ((DtsVideoLearningAttempt)) | null;
 session: ((DtsVideoSession)) | null;
};

export type DtsVideoShortcutReceipt = {
 eventId: string;
 operation: "ENABLE" | "SKIP_INTRO" | "ADVANCE_AFTER_REACTION";
 targetKind: "LEARNING" | "EXAM";
 targetId: string;
 position: number;
 playbackEventId: string;
 resultingPlaybackEventId: string;
 checkpointMs: 0 | 10000;
 appliedRevision: number;
 appliedAt: string;
};

export type DtsVideoShortcutAck = {
 receipt: DtsVideoShortcutReceipt;
 state: DtsVideoShortcutState;
};

export type DtsVideoShortcutEnable = {
 eventId: string;
 position: number;
 playbackEventId: string;
};

export type DtsVideoShortcutSkip = {
 eventId: string;
 position: number;
 playbackEventId: string;
 mediaTimeMs: number;
};

export type DtsVideoShortcutAdvance = {
 eventId: string;
 position: number;
 playbackEventId: string;
 reactionAcknowledgement: string;
};

export type DtsVideoShortcutError = {
 success: false;
 code: string;
 message: string;
 timestamp: string;
};

export type DtsVideoShortcutStateResponse = {
 success: true;
 message?: string;
 data: DtsVideoShortcutState;
 timestamp?: string;
 traceId?: (string) | null;
};

export type DtsVideoShortcutAckResponse = {
 success: true;
 message?: string;
 data: DtsVideoShortcutAck;
 timestamp?: string;
 traceId?: (string) | null;
};
