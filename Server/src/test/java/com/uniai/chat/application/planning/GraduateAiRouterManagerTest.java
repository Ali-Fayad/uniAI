package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.catalog.domain.model.UniversityCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateAiRouterManagerTest {

    @Test
    void dispatchesOnlyToTheExplicitlyRegisteredHandler() {
        AtomicInteger executions = new AtomicInteger();
        GraduateAiRouteHandler<GraduateRouteArguments.ProgramArguments> handler = new GraduateAiRouteHandler<>() {
            @Override
            public GraduateAiRoute route() {
                return GraduateAiRoute.GET_PROGRAM_DETAILS;
            }

            @Override
            public Class<GraduateRouteArguments.ProgramArguments> argumentType() {
                return GraduateRouteArguments.ProgramArguments.class;
            }

            @Override
            public GraduateRouteExecutionResult execute(GraduateRouteArguments.ProgramArguments arguments) {
                executions.incrementAndGet();
                return new GraduateRouteExecutionResult(route(), new ObjectMapper().valueToTree(arguments),
                        "Program: " + arguments.programName(), List.of(), List.of(), false, null);
            }
        };
        GraduateAiRouterManager manager = manager(List.of(handler));

        GraduateRouteExecutionResult result = manager.execute("""
                        {"route":"GET_PROGRAM_DETAILS","arguments":{"university":"AUB","programName":"Computer Science"}}
                        """,
                "Tell me about Computer Science at AUB",
                List.of(university(1L, "American University of Beirut", "AUB")));

        assertEquals(1, executions.get());
        assertEquals(GraduateAiRoute.GET_PROGRAM_DETAILS, result.route());
        assertTrue(result.formattedContext().contains("Computer Science"));
    }

    @Test
    void rejectsAValidButUnregisteredRouteBeforeExecution() {
        GraduateAiRouterManager manager = manager(List.of());
        assertThrows(GraduateRoutePlanningException.class,
                () -> manager.execute("{\"route\":\"LIST_PROGRAMS\",\"arguments\":{}}"));
    }

    @Test
    void directAiHandlerCanReturnWithoutRetrievalContext() {
        GraduateAiRouteHandler<GraduateRouteArguments.DirectAiArguments> handler = new GraduateAiRouteHandler<>() {
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
                return GraduateRouteExecutionResult.direct(new ObjectMapper().valueToTree(arguments), arguments.reason());
            }
        };
        GraduateRouteExecutionResult result = manager(List.of(handler)).execute(
                "{\"route\":\"DIRECT_AI_RESPONSE\",\"arguments\":{\"reason\":\"GREETING\"}}");

        assertEquals(GraduateDirectAiReason.GREETING, result.directAiReason());
        assertEquals("", result.formattedContext());
        assertTrue(result.citations().isEmpty());
    }

    private GraduateAiRouterManager manager(List<GraduateAiRouteHandler<?>> handlers) {
        GraduateAiRouteCatalog catalog = new GraduateAiRouteCatalog();
        GraduateRoutePlanParser parser = new GraduateRoutePlanParser(catalog, new ObjectMapper());
        return new GraduateAiRouterManager(parser, new GraduateAiRouteRegistry(handlers));
    }

    private UniversityCatalog university(long id, String name, String acronym) {
        return UniversityCatalog.builder().id(id).name(name).acronym(acronym).country("Lebanon").build();
    }
}
