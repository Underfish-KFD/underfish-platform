CREATE TABLE event_photos (
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    photo_url TEXT NOT NULL
);
