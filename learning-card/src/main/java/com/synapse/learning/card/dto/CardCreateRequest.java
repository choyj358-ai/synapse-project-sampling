package com.synapse.learning.card.dto;

import jakarta.validation.constraints.NotBlank;

public record CardCreateRequest(
    @NotBlank String front,
    @NotBlank String back
) {}
