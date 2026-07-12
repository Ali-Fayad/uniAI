package com.uniai.chat.application.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.citation.GraduateCitationDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageResponseDtoTest {

    @Test
    void shouldSerializeCitationsWithMessageResponse() throws Exception {
        MessageResponseDto dto = MessageResponseDto.builder()
                .messageId(1L)
                .chatId(2L)
                .senderId(0L)
                .content("Answer [S1]")
                .timestamp(LocalDateTime.parse("2026-07-12T10:15:30"))
                .citations(List.of(new GraduateCitationDto(
                        "program-1",
                        "S1",
                        "AUB CS",
                        "https://aub.edu.lb/cs",
                        "PROGRAM",
                        1L,
                        "AUB",
                        11L,
                        "Computer Science"
                )))
                .build();

        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(dto);

        assertTrue(json.contains("\"citations\""));
        assertTrue(json.contains("\"label\":\"S1\""));
        assertTrue(json.contains("\"url\":\"https://aub.edu.lb/cs\""));
    }
}
