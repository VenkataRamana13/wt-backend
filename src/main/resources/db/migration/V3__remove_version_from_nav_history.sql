-- Remove version column from nav_history table as it's not needed for append-only operations
ALTER TABLE nav_history DROP COLUMN IF EXISTS version; 