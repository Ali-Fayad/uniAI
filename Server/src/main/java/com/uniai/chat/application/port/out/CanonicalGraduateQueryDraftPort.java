package com.uniai.chat.application.port.out;

import com.uniai.chat.application.interpretation.CanonicalGraduateQueryDraft;
import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;

public interface CanonicalGraduateQueryDraftPort {
    CanonicalGraduateQueryDraft interpretDraft(GraduateQueryInterpretationRequest request);
}
