CREATE TABLE decks (
    id          UUID         PRIMARY KEY,
    user_id     UUID         NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_decks_user_id ON decks(user_id);
