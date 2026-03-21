package com.uniai.cvbuilder.domain.builder;

import com.uniai.cvbuilder.domain.model.CV;

/**
 * Builder facade for {@link CV} with named factory methods for clarity.
 */
public final class CVBuilder {

    private final CV.CVBuilder builder;

    private CVBuilder(Long userId, String cvName) {
        this.builder = CV.builder()
                .userId(userId)
                .cvName(cvName)
                .isDefault(false);
    }

    public static CVBuilder newCv(Long userId, String cvName) {
        return new CVBuilder(userId, cvName);
    }

    public CVBuilder template(String template) {
        builder.template(template);
        return this;
    }

    public CVBuilder isDefault(boolean isDefault) {
        builder.isDefault(isDefault);
        return this;
    }

    public CV build() {
        return builder.build();
    }
}
