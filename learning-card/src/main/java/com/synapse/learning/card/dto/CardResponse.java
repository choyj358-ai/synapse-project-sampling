package com.synapse.learning.card.dto;

import com.synapse.learning.card.Card;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardResponse(
    UUID id,
    UUID deckId,
    String front,
    String back,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CardResponse from(Card card) {
        return new CardResponse(
            card.getId(),
            card.getDeck().getId(),
            card.getFront(),
            card.getBack(),
            card.getCreatedAt(),
            card.getUpdatedAt()
        );
    }
}
