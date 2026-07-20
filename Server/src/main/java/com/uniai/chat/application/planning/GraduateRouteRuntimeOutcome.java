package com.uniai.chat.application.planning;

import com.uniai.chat.application.interpretation.GraduateQueryInterpretationResult;

/** Trusted route execution prepared for the existing final-answer orchestration. */
public record GraduateRouteRuntimeOutcome(
        GraduateRouteExecutionResult executionResult,
        String finalContext,
        GraduateQueryInterpretationResult memoryCompatibilityResult
) {
}
