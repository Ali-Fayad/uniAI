package com.uniai.chat.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.port.out.AiServicePort;
import com.uniai.chat.infrastructure.ai.GeminiAiProperties;
import com.uniai.chat.infrastructure.ai.GeminiAiServiceAdapter;
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
            ObjectMapper objectMapper
    ) {
        if ("gemini".equalsIgnoreCase(provider)) {
            logger.info("Using Gemini AI provider");
            return new GeminiAiServiceAdapter(geminiAiProperties, objectMapper);
        }

        logger.info("Using placeholder AI provider");
        return new PlaceholderAiServiceAdapter();
    }
}
