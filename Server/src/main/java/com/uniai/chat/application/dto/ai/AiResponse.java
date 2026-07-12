package com.uniai.chat.application.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.uniai.chat.application.provider.AiProviderFailureCategory;

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
    @Builder.Default
    private AiProviderFailureCategory failureCategory = AiProviderFailureCategory.UNKNOWN;
    @Builder.Default
    private Boolean retryable = Boolean.FALSE;
}
