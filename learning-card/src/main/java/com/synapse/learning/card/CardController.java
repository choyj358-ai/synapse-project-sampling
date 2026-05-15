package com.synapse.learning.card;

import com.synapse.learning.card.dto.CardCreateRequest;
import com.synapse.learning.card.dto.CardResponse;
import com.synapse.learning.card.dto.CardUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/decks/{deckId}/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public Page<CardResponse> getCards(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID deckId,
            @PageableDefault(size = 20) Pageable pageable) {
        return cardService.findAll(userId, deckId, pageable);
    }

    @GetMapping("/{cardId}")
    public CardResponse getCard(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID deckId,
            @PathVariable UUID cardId) {
        return cardService.findById(userId, deckId, cardId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse createCard(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID deckId,
            @RequestBody @Valid CardCreateRequest request) {
        return cardService.create(userId, deckId, request);
    }

    @PutMapping("/{cardId}")
    public CardResponse updateCard(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID deckId,
            @PathVariable UUID cardId,
            @RequestBody @Valid CardUpdateRequest request) {
        return cardService.update(userId, deckId, cardId, request);
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID deckId,
            @PathVariable UUID cardId) {
        cardService.delete(userId, deckId, cardId);
    }
}
