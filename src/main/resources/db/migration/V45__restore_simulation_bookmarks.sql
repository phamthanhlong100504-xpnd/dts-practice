-- ============================================================================
-- V45: Restore simulation bookmarks table erroneously dropped in V43
-- ============================================================================
CREATE TABLE IF NOT EXISTS simulation_bookmarks (
    user_id uuid NOT NULL,
    bank_version varchar(96) NOT NULL,
    scenario_id uuid NOT NULL,
    updated_at timestamptz NOT NULL,
    PRIMARY KEY(user_id,bank_version,scenario_id)
);
