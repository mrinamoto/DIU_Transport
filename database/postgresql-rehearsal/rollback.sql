-- Run only in the explicitly approved disposable rehearsal database.
BEGIN;
DROP TABLE IF EXISTS rehearsal_manifest;
COMMIT;
