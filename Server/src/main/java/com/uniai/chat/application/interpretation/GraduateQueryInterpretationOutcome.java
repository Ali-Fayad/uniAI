package com.uniai.chat.application.interpretation;

/** Explicit outcome of the canonical graduate-query interpretation pipeline. */
public enum GraduateQueryInterpretationOutcome {
    SUCCESS,
    RESOLVED_NO_DATA,
    UNRESOLVED_ENTITY,
    AMBIGUOUS_ENTITY,
    UNSUPPORTED,
    INVALID_INTERPRETATION
}
