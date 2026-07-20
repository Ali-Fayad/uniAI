package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Explicit non-retrieval handler; it never invokes a DAO. */
public final class GraduateDirectAiRouteHandler implements GraduateAiRouteHandler<GraduateRouteArguments.DirectAiArguments> {
    private final ObjectMapper objectMapper;

    public GraduateDirectAiRouteHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public GraduateAiRoute route() {
        return GraduateAiRoute.DIRECT_AI_RESPONSE;
    }

    @Override
    public Class<GraduateRouteArguments.DirectAiArguments> argumentType() {
        return GraduateRouteArguments.DirectAiArguments.class;
    }

    @Override
    public GraduateRouteExecutionResult execute(GraduateRouteArguments.DirectAiArguments arguments) {
        return GraduateRouteExecutionResult.direct(objectMapper.valueToTree(arguments), arguments.reason());
    }
}
