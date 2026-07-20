package com.uniai.chat.application.port.out;

import com.uniai.chat.application.interpretation.GraduateQueryInterpretationRequest;
import com.uniai.chat.application.planning.ValidatedGraduateRoutePlan;

/** AI planning boundary. Implementations may select routes but never execute retrieval. */
public interface GraduateRoutePlannerPort {
    ValidatedGraduateRoutePlan<?> plan(GraduateQueryInterpretationRequest request);
}
