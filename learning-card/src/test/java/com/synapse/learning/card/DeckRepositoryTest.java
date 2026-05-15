package com.synapse.learning.card;

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
class DeckRepositoryTest {

    @Autowired
    private DeckRepository deckRepository;

    @Test
    void 덱_저장_후_사용자ID로_조회() {
        UUID userId = UUID.randomUUID();
        deckRepository.save(Deck.create(userId, "Java 기초", "Java 기초 덱"));

        Page<Deck> result = deckRepository.findByUserIdAndDeletedAtIsNull(userId, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java 기초");
    }

    @Test
    void 소프트_삭제된_덱은_목록에_나타나지_않음() {
        UUID userId = UUID.randomUUID();
        Deck deck = deckRepository.save(Deck.create(userId, "삭제될 덱", null));
        deck.delete();
        deckRepository.save(deck);

        Page<Deck> result = deckRepository.findByUserIdAndDeletedAtIsNull(userId, PageRequest.of(0, 20));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void 다른_사용자_덱은_조회되지_않음() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Deck deck = deckRepository.save(Deck.create(otherUserId, "다른 사람 덱", null));

        Optional<Deck> result = deckRepository.findByIdAndUserIdAndDeletedAtIsNull(deck.getId(), userId);

        assertThat(result).isEmpty();
    }
}
