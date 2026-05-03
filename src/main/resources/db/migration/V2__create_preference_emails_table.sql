CREATE TABLE emails
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY,
    version          BIGINT DEFAULT 0,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by       VARCHAR(100) NOT NULL,
    last_modified_by VARCHAR(100) NOT NULL,

    preference_id    UUID         NOT NULL,
    verified         BOOLEAN      NOT NULL,
    email            VARCHAR(150) NOT NULL,
    type             VARCHAR(20)  NOT NULL CHECK (
        type IN ('PESSOAL', 'COMERCIAL')
        ),

    CONSTRAINT pk_emails PRIMARY KEY (id),
    CONSTRAINT uk_emails_by_email UNIQUE (email),
    CONSTRAINT fk_emails_by_preference_id FOREIGN KEY (preference_id)
        REFERENCES communication_preferences (id) ON DELETE CASCADE
);

CREATE INDEX idx_emails_by_preference_id ON emails (preference_id);
