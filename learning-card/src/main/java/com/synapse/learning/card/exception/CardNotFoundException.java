package com.synapse.learning.card.exception;

import com.synapse.learning.BusinessException;

import java.util.UUID;

public class CardNotFoundException extends BusinessException {

    public CardNotFoundException(UUID cardId) {
        super("CARD-002", "카드를 찾을 수 없습니다: " + cardId);
    }
}
