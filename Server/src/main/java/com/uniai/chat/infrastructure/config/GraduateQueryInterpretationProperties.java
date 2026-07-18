package com.uniai.chat.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.interpretation")
public class GraduateQueryInterpretationProperties {

    private boolean enabled = true;
    private long maxInputTokens = 4500L;
    private int maxOutputTokens = 250;
    private int historyMessageLimit = 4;
    private String promptPath = "prompts/graduate-query-interpreter-prompt.txt";
}
