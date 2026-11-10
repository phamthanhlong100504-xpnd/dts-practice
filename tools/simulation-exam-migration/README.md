# Card05 V32 migration

Use the Card05 kit `DbCheck`, then `DbMigrate` after its matching candidate tests and source Apply/Verify pass. These modes run this repository tool from the tested external candidate directory. They do not start Spring.

Requires the full successful SQL history V1–V31 plus V27.1 in `dts_practice.public`. The runner validates all earlier checksums, checks V32 table/history consistency and targets only V32. It refuses baseline, repair, clean, an incomplete/unknown history, checksum drift and partial V32 state. Check uses read-only connections; migration preserves earlier history and fingerprints existing theory/learning rows. Stop services that may write during this maintenance check.

The four new tables retain exam sessions, ten pinned positions, idempotent receipts and immutable results. Earlier migrations and Card01–03 tables are unchanged. Database rollback is not part of this source patch.

`Run-SimulationExamMigration.py` reads the running local `dts-practice-postgres` container's published port and existing credentials without printing them. It never changes the container. Supply another local container name with `--container` when needed.
