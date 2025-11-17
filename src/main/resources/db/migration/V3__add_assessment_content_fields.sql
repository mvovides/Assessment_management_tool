-- Add content fields to assessment table
ALTER TABLE assessment ADD COLUMN description CLOB;
ALTER TABLE assessment ADD COLUMN file_name VARCHAR(255);
ALTER TABLE assessment ADD COLUMN file_url VARCHAR(500);
