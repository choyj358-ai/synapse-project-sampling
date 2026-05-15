CREATE TABLE cards (
    id          UUID      PRIMARY KEY,
    deck_id     UUID      NOT NULL REFERENCES decks(id),
    front       TEXT      NOT NULL,
    back        TEXT      NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_cards_deck_id ON cards(deck_id);
