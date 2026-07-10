package com.uniai.chat.infrastructure.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.ollama")
public class OllamaAiProperties {

    private String baseUrl = "http://localhost:11434";
    private String model = "gemma3:4b";
    private Integer timeoutSeconds = 120;
}
