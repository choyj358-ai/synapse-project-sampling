package com.synapse.learning.card;

import com.synapse.learning.card.dto.CardCreateRequest;
import com.synapse.learning.card.dto.CardResponse;
import com.synapse.learning.card.dto.CardUpdateRequest;
import com.synapse.learning.card.exception.CardNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final DeckService deckService;

    @Transactional(readOnly = true)
    public Page<CardResponse> findAll(UUID userId, UUID deckId, Pageable pageable) {
        deckService.getDeck(userId, deckId);
        return cardRepository.findByDeckIdAndDeletedAtIsNull(deckId, pageable)
            .map(CardResponse::from);
    }

    @Transactional(readOnly = true)
    public CardResponse findById(UUID userId, UUID deckId, UUID cardId) {
        deckService.getDeck(userId, deckId);
        return CardResponse.from(getCard(deckId, cardId));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CardResponse create(UUID userId, UUID deckId, CardCreateRequest request) {
        Deck deck = deckService.getDeck(userId, deckId);
        Card card = Card.create(deck, request.front(), request.back());
        return CardResponse.from(cardRepository.save(card));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CardResponse update(UUID userId, UUID deckId, UUID cardId, CardUpdateRequest request) {
        deckService.getDeck(userId, deckId);
        Card card = getCard(deckId, cardId);
        card.update(request.front(), request.back());
        return CardResponse.from(card);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(UUID userId, UUID deckId, UUID cardId) {
        deckService.getDeck(userId, deckId);
        getCard(deckId, cardId).delete();
    }

    private Card getCard(UUID deckId, UUID cardId) {
        return cardRepository.findByIdAndDeckIdAndDeletedAtIsNull(cardId, deckId)
            .orElseThrow(() -> new CardNotFoundException(cardId));
    }
}
