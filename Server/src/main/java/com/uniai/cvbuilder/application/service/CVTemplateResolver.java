package com.uniai.cvbuilder.application.service;

import com.uniai.cvbuilder.application.dto.command.CreateCVCommand;
import com.uniai.cvbuilder.domain.model.CVTemplate;
import com.uniai.cvbuilder.domain.repository.CVTemplateRepository;
import com.uniai.shared.exception.CVTemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Resolves CV template references from API commands into active template entities.
 */
@Service
@RequiredArgsConstructor
public class CVTemplateResolver {

    private static final String DEFAULT_TEMPLATE_COMPONENT = "ModernTemplate";

    private final CVTemplateRepository cvTemplateRepository;

    public CVTemplate resolveForCreate(CreateCVCommand command) {
        CVTemplate template = resolve(command.getTemplateId(), command.getTemplate());
        if (template != null) {
            return template;
        }
        return cvTemplateRepository.findActiveByComponentName(DEFAULT_TEMPLATE_COMPONENT)
            .or(() -> cvTemplateRepository.findAllActive().stream().findFirst())
            .orElse(null);
    }

    public CVTemplate resolve(Long templateId, String componentName) {
        if (templateId != null) {
            return cvTemplateRepository.findById(templateId)
                .filter(CVTemplate::isActive)
                .orElseThrow(CVTemplateNotFoundException::new);
        }
        if (componentName != null && !componentName.isBlank()) {
            return cvTemplateRepository.findActiveByComponentName(componentName)
                .orElseThrow(CVTemplateNotFoundException::new);
        }
        return null;
    }
}
