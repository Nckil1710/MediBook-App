-- One-time fix: slots table now uses doctor_id only (service comes from doctor).
-- Run this if you see: Field 'service_id' doesn't have a default value
-- Step 1: Drop the foreign key on service_id (constraint name may vary; check with SHOW CREATE TABLE slots;)
-- Step 2: Drop the column

-- MySQL: drop the FK first, then the column
ALTER TABLE slots DROP FOREIGN KEY FKfjpw9eyume5svnso2l1cyqq2g;
ALTER TABLE slots DROP COLUMN service_id;
