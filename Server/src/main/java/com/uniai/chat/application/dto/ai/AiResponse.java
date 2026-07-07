package com.uniai.chat.application.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {

    private String content;
    private String provider;
    private String model;
    private String finishReason;
    private Boolean fallback;
}
