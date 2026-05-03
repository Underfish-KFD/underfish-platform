CREATE TABLE community_organizers (
    id SERIAL PRIMARY KEY,
    community_id UUID NOT NULL,
    user_id UUID NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (community_id, user_id)
);

CREATE INDEX idx_community_organizers_community_id ON community_organizers(community_id);
CREATE INDEX idx_community_organizers_user_id ON community_organizers(user_id);

