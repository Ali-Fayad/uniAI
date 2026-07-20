package com.uniai.chat.application.port.out;

import com.uniai.chat.application.planning.GraduateRoutePlanningRequest;
import com.uniai.chat.application.planning.ValidatedGraduateRoutePlan;

/** AI planning boundary. Implementations may select routes but never execute retrieval. */
public interface GraduateRoutePlannerPort {
    ValidatedGraduateRoutePlan<?> plan(GraduateRoutePlanningRequest request);
}
