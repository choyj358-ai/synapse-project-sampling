package com.synapse.learning.card.dto;

import jakarta.validation.constraints.NotBlank;

public record DeckCreateRequest(
    @NotBlank String title,
    String description
) {}
