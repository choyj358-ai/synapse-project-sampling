package com.synapse.learning.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.learning.card.dto.DeckCreateRequest;
import com.synapse.learning.card.dto.DeckResponse;
import com.synapse.learning.card.dto.DeckUpdateRequest;
import com.synapse.learning.card.exception.DeckNotFoundException;
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
class DeckControllerTest {

    @Autowired private WebApplicationContext wac;
    @MockitoBean private DeckService deckService;

    private MockMvc mockMvc;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final UUID userId = UUID.randomUUID();
    private final UUID deckId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    private DeckResponse sampleResponse() {
        return new DeckResponse(deckId, userId, "Java 기초", "설명", LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void 덱_생성_성공_201() throws Exception {
        given(deckService.create(any(), any())).willReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/decks")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new DeckCreateRequest("Java 기초", "설명"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Java 기초"));
    }

    @Test
    void 덱_목록_조회_200() throws Exception {
        given(deckService.findAll(any(), any())).willReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/decks")
                .header("X-User-Id", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("Java 기초"));
    }

    @Test
    void 덱_단건_조회_200() throws Exception {
        given(deckService.findById(any(), any())).willReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/decks/{deckId}", deckId)
                .header("X-User-Id", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(deckId.toString()));
    }

    @Test
    void 존재하지_않는_덱_조회시_404() throws Exception {
        given(deckService.findById(any(), any())).willThrow(new DeckNotFoundException(deckId));

        mockMvc.perform(get("/api/v1/decks/{deckId}", deckId)
                .header("X-User-Id", userId))
            .andExpect(status().isNotFound());
    }

    @Test
    void 덱_수정_200() throws Exception {
        given(deckService.update(any(), any(), any())).willReturn(sampleResponse());

        mockMvc.perform(put("/api/v1/decks/{deckId}", deckId)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new DeckUpdateRequest("수정된 제목", null))))
            .andExpect(status().isOk());
    }

    @Test
    void 덱_삭제_204() throws Exception {
        mockMvc.perform(delete("/api/v1/decks/{deckId}", deckId)
                .header("X-User-Id", userId))
            .andExpect(status().isNoContent());
    }

    @Test
    void 제목_없이_덱_생성시_400() throws Exception {
        mockMvc.perform(post("/api/v1/decks")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new DeckCreateRequest("", null))))
            .andExpect(status().isBadRequest());
    }
}
