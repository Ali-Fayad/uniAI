package com.uniai.chat.application.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.chat.application.port.out.GraduateTuitionRouteDao;
import com.uniai.chat.application.retrieval.ResolvedUniversity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.uniai.chat.application.planning.GraduateRouteArguments.*;
import static com.uniai.chat.application.port.out.GraduateTuitionRouteDao.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduateTuitionRankingRouteTest {
    @Test
    void universityRankingHandlerUsesTheTypedRankingDaoPath() {
        GraduateTuitionRouteDao dao = new EmptyDao() {
            @Override
            public List<UniversityTuitionRankingRow> rankUniversitiesByTuition(TuitionRankingCriteria criteria) {
                assertTrue(criteria.universityIds().contains(1L));
                assertTrue(criteria.programs().contains("Computer Science"));
                assertTrue(criteria.billingBasis().equals("PER_CREDIT"));
                return List.of(new UniversityTuitionRankingRow(1L, "American University of Beirut", "AUB",
                        "2026-2027", "USD", "PER_CREDIT", "FACULTY",
                        new BigDecimal("1136"), new BigDecimal("1100"), new BigDecimal("1200"), 2));
            }
        };
        GraduateAiRouteHandler<RankUniversitiesByTuitionArguments> handler =
                new GraduateTuitionRouteHandler<>(GraduateAiRoute.RANK_UNIVERSITIES_BY_TUITION,
                        RankUniversitiesByTuitionArguments.class, dao);
        RankUniversitiesByTuitionArguments args = new RankUniversitiesByTuitionArguments(
                List.of("AUB"), List.of("Computer Science"), List.of("MASTER"), List.of(),
                "2026-2027", "USD", BillingBasis.PER_CREDIT, TuitionRankingOrder.ASC, 10);
        GraduateRouteExecutionResult result = handler.executeResolved(new ResolvedGraduateRoutePlan<>(
                GraduateAiRoute.RANK_UNIVERSITIES_BY_TUITION, args,
                new ObjectMapper().valueToTree(args), List.of(new ResolvedUniversity(1L,
                "American University of Beirut", "AUB"))));

        assertTrue(result.formattedContext().contains("Average tuition: USD 1136"));
        assertTrue(result.formattedContext().contains("Scope level: FACULTY"));
    }

    @Test
    void programRankingHandlerReportsComparableProgramMetrics() {
        GraduateTuitionRouteDao dao = new EmptyDao() {
            @Override
            public List<ProgramTuitionRankingRow> rankProgramsByTuition(TuitionRankingCriteria criteria) {
                return List.of(new ProgramTuitionRankingRow(6L, "Master of Science in Computer Science", 1L,
                        "American University of Beirut", "AUB", "2026-2027", "USD", "PER_CREDIT", "FACULTY",
                        new BigDecimal("1136"), new BigDecimal("1136"), new BigDecimal("1136"), 1));
            }
        };
        GraduateAiRouteHandler<RankProgramsByTuitionArguments> handler =
                new GraduateTuitionRouteHandler<>(GraduateAiRoute.RANK_PROGRAMS_BY_TUITION,
                        RankProgramsByTuitionArguments.class, dao);
        RankProgramsByTuitionArguments args = new RankProgramsByTuitionArguments(
                List.of(), List.of(), List.of(), List.of("Computer Science"), List.of("MASTER"), List.of(),
                "2026-2027", "USD", BillingBasis.PER_CREDIT, TuitionRankingOrder.ASC, 5);
        GraduateRouteExecutionResult result = handler.executeResolved(new ResolvedGraduateRoutePlan<>(
                GraduateAiRoute.RANK_PROGRAMS_BY_TUITION, args,
                new ObjectMapper().valueToTree(args), List.of()));

        assertTrue(result.formattedContext().contains("Program: Master of Science in Computer Science"));
        assertTrue(result.formattedContext().contains("Matching records: 1"));
    }

    private static class EmptyDao implements GraduateTuitionRouteDao {
        @Override public TuitionPage findTuition(TuitionCriteria criteria) { return new TuitionPage(List.of(), 0); }
        @Override public List<TuitionAggregateRow> aggregateTuition(TuitionCriteria criteria) { return List.of(); }
        @Override public List<UniversityTuitionRankingRow> rankUniversitiesByTuition(TuitionRankingCriteria criteria) { return List.of(); }
        @Override public List<ProgramTuitionRankingRow> rankProgramsByTuition(TuitionRankingCriteria criteria) { return List.of(); }
        @Override public FeePage findFees(TuitionCriteria criteria) { return new FeePage(List.of(), 0); }
    }
}
