package com.synapse.learning.card;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    Page<Card> findByDeckIdAndDeletedAtIsNull(UUID deckId, Pageable pageable);

    Optional<Card> findByIdAndDeckIdAndDeletedAtIsNull(UUID id, UUID deckId);
}
