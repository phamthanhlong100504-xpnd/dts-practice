-- ADM02-05: private consumer preparation ledger. No active bank switches and no learner writes.
-- A release can be prepared once for a bank version and exact frozen byte digest.
CREATE TABLE admin_simulation_release_preparations (
    release_id UUID PRIMARY KEY,
    bank_version VARCHAR(96) NOT NULL UNIQUE,
    snapshot_sha256 CHAR(64) NOT NULL CHECK(snapshot_sha256 ~ '^[a-f0-9]{64}$'),
    manifest_sha256 CHAR(64) NOT NULL CHECK(manifest_sha256 ~ '^[a-f0-9]{64}$'),
    snapshot_json TEXT NOT NULL CHECK(octet_length(snapshot_json)>100 AND octet_length(snapshot_json)<=16777216),
    state VARCHAR(16) NOT NULL DEFAULT 'PREPARED' CHECK(state='PREPARED'),
    prepared_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CHECK (bank_version ~ '^[A-Za-z0-9][A-Za-z0-9._-]{0,95}$')
);
CREATE FUNCTION protect_admin_simulation_preparation() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN RAISE EXCEPTION 'Prepared simulation release is immutable'; END $$;
CREATE TRIGGER trg_admin_simulation_preparation_immutable
 BEFORE UPDATE OR DELETE ON admin_simulation_release_preparations
 FOR EACH ROW EXECUTE FUNCTION protect_admin_simulation_preparation();
-- No rows are inserted into active content pointers; legacy Practice APIs stay unchanged.
