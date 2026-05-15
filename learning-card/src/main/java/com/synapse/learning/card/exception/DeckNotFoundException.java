package com.synapse.learning.card.exception;

import com.synapse.learning.BusinessException;

import java.util.UUID;

public class DeckNotFoundException extends BusinessException {

    public DeckNotFoundException(UUID deckId) {
        super("CARD-001", "덱을 찾을 수 없습니다: " + deckId);
    }
}
