CREATE TABLE stored_files
(
    id            UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    original_name VARCHAR(500)                NOT NULL,
    object_key    VARCHAR(500)                NOT NULL,
    content_type  VARCHAR(100)                NOT NULL,
    size_bytes    BIGINT                      NOT NULL,
    entity_type   VARCHAR(50)                 NOT NULL,
    entity_id     UUID,
    uploaded_by   VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_stored_files PRIMARY KEY (id)
);
