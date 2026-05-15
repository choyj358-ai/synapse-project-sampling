package com.synapse.learning.card;

import com.synapse.learning.card.dto.DeckCreateRequest;
import com.synapse.learning.card.dto.DeckResponse;
import com.synapse.learning.card.dto.DeckUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @GetMapping
    public Page<DeckResponse> getDecks(
            @RequestHeader("X-User-Id") UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return deckService.findAll(userId, pageable);
    }

    @GetMapping("/{deckId}")
    public DeckResponse getDeck(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID deckId) {
        return deckService.findById(userId, deckId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeckResponse createDeck(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid DeckCreateRequest request) {
        return deckService.create(userId, request);
    }

    @PutMapping("/{deckId}")
    public DeckResponse updateDeck(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID deckId,
            @RequestBody @Valid DeckUpdateRequest request) {
        return deckService.update(userId, deckId, request);
    }

    @DeleteMapping("/{deckId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeck(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID deckId) {
        deckService.delete(userId, deckId);
    }
}
