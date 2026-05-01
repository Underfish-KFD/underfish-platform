CREATE TABLE stored_files
(
    id            UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    created_by    VARCHAR(255),
    created_by_id UUID,
    updated_by    VARCHAR(255),
    updated_by_id UUID,
    original_name VARCHAR(500)                NOT NULL,
    object_key    VARCHAR(500)                NOT NULL,
    content_type  VARCHAR(100)                NOT NULL,
    size_bytes    BIGINT                      NOT NULL,
    CONSTRAINT pk_stored_files PRIMARY KEY (id)
);
