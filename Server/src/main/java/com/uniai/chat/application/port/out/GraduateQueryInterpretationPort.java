package com.uniai.chat.application.port.out;

import com.uniai.chat.application.interpretation.GraduateQueryInterpretation;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;

public interface GraduateQueryInterpretationPort extends CanonicalGraduateQueryDraftPort {

    GraduateQueryInterpretation interpret(GraduateQueryInterpretationRequest request);

    @Override
    default com.uniai.chat.application.interpretation.CanonicalGraduateQueryDraft interpretDraft(
            GraduateQueryInterpretationRequest request
    ) {
        throw new UnsupportedOperationException("Canonical draft interpretation is not available on this compatibility port");
    }
}
