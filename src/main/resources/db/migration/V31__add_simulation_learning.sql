-- Card 01: independent simulation learning; no changes to theory tables or migrations.
CREATE TABLE simulation_attempts (
    id uuid PRIMARY KEY,
    attempt_sequence bigint GENERATED ALWAYS AS IDENTITY UNIQUE,
    user_id uuid NOT NULL,
    create_event_id uuid NOT NULL,
    bank_version varchar(96) NOT NULL,
    scoring_version varchar(128) NOT NULL,
    manifest_sha256 char(64) NOT NULL,
    scenario_id uuid NOT NULL,
    scenario_number integer NOT NULL CHECK (scenario_number BETWEEN 1 AND 120),
    chapter_id integer NOT NULL CHECK (chapter_id BETWEEN 1 AND 6),
    license_class varchar(8) NOT NULL,
    duration_ms integer NOT NULL CHECK (duration_ms > 0),
    scenario_snapshot jsonb NOT NULL CHECK (jsonb_typeof(scenario_snapshot) = 'object'),
    next_scenario_id uuid,
    status varchar(16) NOT NULL CHECK (status IN ('CREATED','PLAYING','INTERRUPTED','COMPLETED','CANCELLED')),
    playback_event_id uuid NOT NULL,
    granted_at timestamptz,
    started_at timestamptz,
    segment_started_at timestamptz,
    segment_start_ms integer NOT NULL DEFAULT 0,
    last_media_time_ms integer NOT NULL DEFAULT 0,
    last_observed_at timestamptz,
    first_response_ms integer,
    first_response_event_id uuid,
    score integer CHECK (score BETWEEN 0 AND 5),
    completed_at timestamptz,
    interrupted_reason varchar(32),
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    UNIQUE(user_id,create_event_id),
    UNIQUE(user_id,id),
    CHECK (segment_start_ms BETWEEN 0 AND duration_ms AND last_media_time_ms BETWEEN segment_start_ms AND duration_ms),
    CHECK (first_response_ms IS NULL OR first_response_ms BETWEEN 0 AND duration_ms),
    CHECK ((first_response_ms IS NULL) = (first_response_event_id IS NULL)),
    CHECK ((status = 'COMPLETED') = (completed_at IS NOT NULL AND score IS NOT NULL)),
    CHECK (status = 'COMPLETED' OR (completed_at IS NULL AND score IS NULL)),
    CHECK (status <> 'PLAYING' OR (started_at IS NOT NULL AND segment_started_at IS NOT NULL AND last_observed_at IS NOT NULL AND granted_at IS NOT NULL))
);
CREATE INDEX simulation_attempts_progress_idx ON simulation_attempts(user_id,bank_version,scenario_id) WHERE status='COMPLETED';
CREATE INDEX simulation_attempts_recent_idx ON simulation_attempts(user_id,bank_version,scenario_id,attempt_sequence DESC);

CREATE TABLE simulation_attempt_events (
    user_id uuid NOT NULL,
    event_id uuid NOT NULL,
    attempt_id uuid,
    kind varchar(24) NOT NULL,
    request_sha256 char(64) NOT NULL,
    created_at timestamptz NOT NULL,
    PRIMARY KEY(user_id,event_id),
    FOREIGN KEY(user_id,attempt_id) REFERENCES simulation_attempts(user_id,id)
);
CREATE TABLE simulation_bookmarks (
    user_id uuid NOT NULL,
    bank_version varchar(96) NOT NULL,
    scenario_id uuid NOT NULL,
    updated_at timestamptz NOT NULL,
    PRIMARY KEY(user_id,bank_version,scenario_id)
);
CREATE TABLE simulation_cursors (
    user_id uuid NOT NULL,
    bank_version varchar(96) NOT NULL,
    chapter_id integer NOT NULL CHECK (chapter_id BETWEEN 1 AND 6),
    scenario_id uuid NOT NULL,
    source_attempt_id uuid,
    revision bigint NOT NULL CHECK (revision > 0),
    updated_at timestamptz NOT NULL,
    PRIMARY KEY(user_id,bank_version,chapter_id),
    FOREIGN KEY(user_id,source_attempt_id) REFERENCES simulation_attempts(user_id,id)
);

CREATE FUNCTION protect_simulation_attempt() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP='DELETE' THEN RAISE EXCEPTION 'Simulation attempt history is retained'; END IF;
    IF ROW(NEW.id,NEW.attempt_sequence,NEW.user_id,NEW.create_event_id,NEW.bank_version,NEW.scoring_version,NEW.manifest_sha256,
           NEW.scenario_id,NEW.scenario_number,NEW.chapter_id,NEW.license_class,NEW.duration_ms,NEW.scenario_snapshot,NEW.next_scenario_id,NEW.created_at)
       IS DISTINCT FROM
       ROW(OLD.id,OLD.attempt_sequence,OLD.user_id,OLD.create_event_id,OLD.bank_version,OLD.scoring_version,OLD.manifest_sha256,
           OLD.scenario_id,OLD.scenario_number,OLD.chapter_id,OLD.license_class,OLD.duration_ms,OLD.scenario_snapshot,OLD.next_scenario_id,OLD.created_at)
    THEN RAISE EXCEPTION 'Simulation snapshot is immutable'; END IF;
    IF OLD.first_response_ms IS NOT NULL AND ROW(NEW.first_response_ms,NEW.first_response_event_id) IS DISTINCT FROM ROW(OLD.first_response_ms,OLD.first_response_event_id)
    THEN RAISE EXCEPTION 'First response is immutable'; END IF;
    IF OLD.status IN ('COMPLETED','CANCELLED') AND NEW IS DISTINCT FROM OLD THEN RAISE EXCEPTION 'Terminal attempt is immutable'; END IF;
    IF (OLD.status='CREATED' AND NEW.status NOT IN ('CREATED','PLAYING','CANCELLED'))
       OR (OLD.status='PLAYING' AND NEW.status NOT IN ('PLAYING','INTERRUPTED','COMPLETED','CANCELLED'))
       OR (OLD.status='INTERRUPTED' AND NEW.status NOT IN ('INTERRUPTED','PLAYING','CANCELLED'))
    THEN RAISE EXCEPTION 'Invalid simulation transition'; END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER simulation_attempt_guard BEFORE UPDATE OR DELETE ON simulation_attempts FOR EACH ROW EXECUTE FUNCTION protect_simulation_attempt();
CREATE FUNCTION protect_simulation_event() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN RAISE EXCEPTION 'Simulation events are append only'; END $$;
CREATE TRIGGER simulation_event_guard BEFORE UPDATE OR DELETE ON simulation_attempt_events FOR EACH ROW EXECUTE FUNCTION protect_simulation_event();
