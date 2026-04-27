CREATE TABLE communities (
    id SERIAL PRIMARY KEY,
    community_id UUID NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    organizer_id UUID NOT NULL,
    is_private BOOLEAN NOT NULL DEFAULT false,
    cover_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_communities_community_id ON communities(community_id);
CREATE INDEX idx_communities_organizer_id ON communities(organizer_id);
CREATE INDEX idx_communities_status ON communities(status);

