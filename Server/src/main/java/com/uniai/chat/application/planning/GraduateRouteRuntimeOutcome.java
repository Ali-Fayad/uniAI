package com.uniai.chat.application.planning;

/** Trusted route execution prepared for the existing final-answer orchestration. */
public record GraduateRouteRuntimeOutcome(
        GraduateRouteExecutionResult executionResult,
        String finalContext
) {
}
