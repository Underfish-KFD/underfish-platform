ALTER TABLE events
    ALTER COLUMN location_id TYPE VARCHAR(255) USING location_id::TEXT;
