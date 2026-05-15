package com.synapse.learning.card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CardRepositoryTest {

    @Autowired private CardRepository cardRepository;
    @Autowired private DeckRepository deckRepository;

    private Deck deck;

    @BeforeEach
    void setUp() {
        deck = deckRepository.save(Deck.create(UUID.randomUUID(), "테스트 덱", null));
    }

    @Test
    void 카드_저장_후_덱ID로_조회() {
        cardRepository.save(Card.create(deck, "앞면", "뒷면"));

        Page<Card> result = cardRepository.findByDeckIdAndDeletedAtIsNull(deck.getId(), PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFront()).isEqualTo("앞면");
    }

    @Test
    void 소프트_삭제된_카드는_목록에_나타나지_않음() {
        Card card = cardRepository.save(Card.create(deck, "앞면", "뒷면"));
        card.delete();
        cardRepository.save(card);

        Page<Card> result = cardRepository.findByDeckIdAndDeletedAtIsNull(deck.getId(), PageRequest.of(0, 20));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void 카드ID와_덱ID로_단건_조회() {
        Card card = cardRepository.save(Card.create(deck, "앞면", "뒷면"));

        Optional<Card> result = cardRepository.findByIdAndDeckIdAndDeletedAtIsNull(card.getId(), deck.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getBack()).isEqualTo("뒷면");
    }
}
