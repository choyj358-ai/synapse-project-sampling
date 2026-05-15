package com.synapse.learning.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.learning.card.dto.CardCreateRequest;
import com.synapse.learning.card.dto.CardResponse;
import com.synapse.learning.card.dto.CardUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class CardControllerTest {

    @Autowired private WebApplicationContext wac;
    @MockitoBean private CardService cardService;

    private MockMvc mockMvc;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final UUID userId = UUID.randomUUID();
    private final UUID deckId = UUID.randomUUID();
    private final UUID cardId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    private CardResponse sampleResponse() {
        return new CardResponse(cardId, deckId, "앞면", "뒷면", LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void 카드_생성_성공_201() throws Exception {
        given(cardService.create(any(), any(), any())).willReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/decks/{deckId}/cards", deckId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CardCreateRequest("앞면", "뒷면"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.front").value("앞면"));
    }

    @Test
    void 카드_목록_조회_200() throws Exception {
        given(cardService.findAll(any(), any(), any())).willReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/decks/{deckId}/cards", deckId)
                .header("X-User-Id", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].front").value("앞면"));
    }

    @Test
    void 카드_단건_조회_200() throws Exception {
        given(cardService.findById(any(), any(), any())).willReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/decks/{deckId}/cards/{cardId}", deckId, cardId)
                .header("X-User-Id", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(cardId.toString()));
    }

    @Test
    void 카드_수정_200() throws Exception {
        given(cardService.update(any(), any(), any(), any())).willReturn(sampleResponse());

        mockMvc.perform(put("/api/v1/decks/{deckId}/cards/{cardId}", deckId, cardId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CardUpdateRequest("수정된 앞면", "수정된 뒷면"))))
            .andExpect(status().isOk());
    }

    @Test
    void 카드_삭제_204() throws Exception {
        mockMvc.perform(delete("/api/v1/decks/{deckId}/cards/{cardId}", deckId, cardId)
                .header("X-User-Id", userId))
            .andExpect(status().isNoContent());
    }

    @Test
    void 앞면_없이_카드_생성시_400() throws Exception {
        mockMvc.perform(post("/api/v1/decks/{deckId}/cards", deckId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CardCreateRequest("", "뒷면"))))
            .andExpect(status().isBadRequest());
    }
}
