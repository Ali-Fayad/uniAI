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
    private int maxOutputTokens = 500;
    private int historyMessageLimit = 4;
    private String promptPath = "prompts/graduate-query-interpreter-prompt.txt";
    private boolean routePlannerEnabled = false;
    private boolean routePlannerShadowEnabled = false;
    private String routePlannerPromptPath = "prompts/graduate-route-planner-prompt.txt";
}
