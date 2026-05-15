package com.synapse.learning.card.dto;

import jakarta.validation.constraints.NotBlank;

public record DeckUpdateRequest(
    @NotBlank String title,
    String description
) {}
