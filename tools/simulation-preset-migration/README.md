# Explicit V33 Practice migration

Use the guarded Card06 kit wrapper `DbCheck`, then `DbMigrate -CheckAudit ...` with the exact passing all-suite900/43 candidate. New tool is test-scope native Flyway, not a Spring application. The Python wrapper checks root/branch, reviewed V33 SHA, every candidate source file and all43 canonical Maven XML suites before obtaining the known `dts-practice-postgres` container's loopback DB configuration in memory. Passwords are never arguments or printed.

Required history: V1–V32 plus V27.1, optionally already-correct V33. Readonly Check validates every old checksum and schema shape. Migrate holds an upgrade lock, sets targetV33, and accepts only zero migrations if installed or exactly V33 otherwise. It never enables out-of-order/clean/baseline/repair or starts application services.

Before/after fingerprints cover all existing business tables, including Card05 positions/events/results; `simulation_exam_sessions` projection excludes only the3 newly added nullable preset fields so old RANDOM records compare faithfully. Old history rows remain byte-equivalent. Check→Migrate also requires an unchanged raw source/Git snapshot. Generated Python caches are disabled and ignored; no repository cache is generated or deleted.

On any failed or uncertain migration, retain audit files and inspect result/history before retrying. Do not edit SQL checksums/history or rerun old migrations. Once migration succeeds, build/restart Practice with its existing configuration and continue HTTP acceptance. Source installation is separate from DB installation and runtime acceptance.
