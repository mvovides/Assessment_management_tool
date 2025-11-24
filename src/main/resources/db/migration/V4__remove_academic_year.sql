-- Remove academic year from module table as it's not in project requirements

-- Drop the unique constraint that includes academic_year
ALTER TABLE module DROP CONSTRAINT IF EXISTS module_code_academic_year_key;

-- Drop the index on academic_year
DROP INDEX IF EXISTS idx_module_year;

-- Drop the academic_year column
ALTER TABLE module DROP COLUMN IF EXISTS academic_year;

-- Add new unique constraint on just the code
ALTER TABLE module ADD CONSTRAINT module_code_key UNIQUE (code);

-- Create index on code for performance
CREATE INDEX idx_module_code ON module(code);
