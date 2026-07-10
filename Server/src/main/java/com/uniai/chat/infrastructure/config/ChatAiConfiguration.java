package com.uniai.chat.infrastructure.config;

import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.infrastructure.ai.GeminiAiProperties;
import com.uniai.chat.infrastructure.ai.GeminiAiServiceAdapter;
import com.uniai.chat.infrastructure.ai.GroqAiProperties;
import com.uniai.chat.infrastructure.ai.GroqAiServiceAdapter;
import com.uniai.chat.infrastructure.ai.PlaceholderAiServiceAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatAiConfiguration {

    private static final Logger logger = LogManager.getLogger(ChatAiConfiguration.class);

    @Bean
    public AiServicePort aiServicePort(
            @Value("${ai.provider:placeholder}") String provider,
            GeminiAiProperties geminiAiProperties,
            GroqAiProperties groqAiProperties
    ) {
        if ("gemini".equalsIgnoreCase(provider)) {
            logger.info("[AI] Provider selected provider=gemini model={}", geminiAiProperties.getModel());
            AiServicePort aiServicePort = new GeminiAiServiceAdapter(geminiAiProperties, new com.fasterxml.jackson.databind.ObjectMapper());
            logger.info("[AI] Provider initialized successfully provider=gemini model={}", geminiAiProperties.getModel());
            return aiServicePort;
        }

        if ("groq".equalsIgnoreCase(provider)) {
            logger.info("[AI] Provider selected provider=groq model={}", groqAiProperties.getModel());
            AiServicePort aiServicePort = new GroqAiServiceAdapter(groqAiProperties);
            logger.info("[AI] Provider initialized successfully provider=groq model={}", groqAiProperties.getModel());
            return aiServicePort;
        }

        if (!"placeholder".equalsIgnoreCase(provider)) {
            logger.warn("[AI] Unsupported provider configured provider={} falling back to placeholder", provider);
        }
        logger.info("[AI] Provider selected provider=placeholder model=placeholder");
        AiServicePort aiServicePort = new PlaceholderAiServiceAdapter();
        logger.info("[AI] Provider initialized successfully provider=placeholder model=placeholder");
        return aiServicePort;
    }
}
