package com.synapse.learning.card;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeckRepository extends JpaRepository<Deck, UUID> {

    Page<Deck> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Optional<Deck> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}
