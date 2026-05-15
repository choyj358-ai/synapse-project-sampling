package com.synapse.learning.card.dto;

import com.synapse.learning.card.Deck;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeckResponse(
    UUID id,
    UUID userId,
    String title,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static DeckResponse from(Deck deck) {
        return new DeckResponse(
            deck.getId(),
            deck.getUserId(),
            deck.getTitle(),
            deck.getDescription(),
            deck.getCreatedAt(),
            deck.getUpdatedAt()
        );
    }
}
