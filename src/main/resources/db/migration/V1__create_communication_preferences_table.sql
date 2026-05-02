CREATE TABLE communication_preferences
(
    id                    UUID,
    version               BIGINT DEFAULT 0,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by            VARCHAR(100) NOT NULL,
    last_modified_by      VARCHAR(100) NOT NULL,

    customer_id           UUID         NOT NULL,
    communication_channel VARCHAR(20)  NOT NULL CHECK (
        communication_channel IN ('SMS', 'EMAIL', 'WHATSAPP', 'TELEFONE', 'RESIDENCIAL')
        ),

    CONSTRAINT pk_communication_preferences PRIMARY KEY (id)
);

CREATE INDEX idx_communication_preferences_by_customer_id ON communication_preferences (customer_id);
