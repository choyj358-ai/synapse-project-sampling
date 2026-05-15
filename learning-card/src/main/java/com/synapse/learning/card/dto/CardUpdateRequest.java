package com.synapse.learning.card.dto;

import jakarta.validation.constraints.NotBlank;

public record CardUpdateRequest(
    @NotBlank String front,
    @NotBlank String back
) {}
