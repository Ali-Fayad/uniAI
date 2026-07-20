package com.uniai.chat.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.route-planner")
public class GraduateRoutePlannerProperties {
    private long maxInputTokens = 4500L;
    private int maxOutputTokens = 500;
    private int historyMessageLimit = 4;
    private String promptPath = "prompts/graduate-route-planner-prompt.txt";
}
