package com.synapse.learning.card;

import com.synapse.learning.card.dto.DeckCreateRequest;
import com.synapse.learning.card.dto.DeckResponse;
import com.synapse.learning.card.dto.DeckUpdateRequest;
import com.synapse.learning.card.exception.DeckNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;

    @Transactional(readOnly = true)
    public Page<DeckResponse> findAll(UUID userId, Pageable pageable) {
        return deckRepository.findByUserIdAndDeletedAtIsNull(userId, pageable)
            .map(DeckResponse::from);
    }

    @Transactional(readOnly = true)
    public DeckResponse findById(UUID userId, UUID deckId) {
        return DeckResponse.from(getDeck(userId, deckId));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public DeckResponse create(UUID userId, DeckCreateRequest request) {
        Deck deck = Deck.create(userId, request.title(), request.description());
        return DeckResponse.from(deckRepository.save(deck));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public DeckResponse update(UUID userId, UUID deckId, DeckUpdateRequest request) {
        Deck deck = getDeck(userId, deckId);
        deck.update(request.title(), request.description());
        return DeckResponse.from(deck);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(UUID userId, UUID deckId) {
        getDeck(userId, deckId).delete();
    }

    Deck getDeck(UUID userId, UUID deckId) {
        return deckRepository.findByIdAndUserIdAndDeletedAtIsNull(deckId, userId)
            .orElseThrow(() -> new DeckNotFoundException(deckId));
    }
}
