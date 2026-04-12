package com.uniai.cvbuilder.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.uniai.cvbuilder.infrastructure.web.ProfileCompletionInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ProfileCompletionInterceptor profileCompletionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(profileCompletionInterceptor)
                .addPathPatterns("/api/cv/**")
                .excludePathPatterns(
                    "/api/cv/personal-info", 
                    "/api/cv/personal-info/**",
                    "/api/cv/skills",
                    "/api/cv/positions",
                    "/api/cv/universities",
                    "/api/cv/languages"
                );
    }
}
