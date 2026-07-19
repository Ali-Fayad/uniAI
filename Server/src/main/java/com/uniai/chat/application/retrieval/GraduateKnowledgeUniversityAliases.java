package com.uniai.chat.application.retrieval;

import java.util.Map;

/** Central server-owned alias registry for university identity resolution. */
public final class GraduateKnowledgeUniversityAliases {
    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("lu", "ul"),
            Map.entry("uob", "uob"),
            Map.entry("balamand", "uob"),
            Map.entry("balamand university", "uob"),
            Map.entry("balamand uni", "uob")
    );

    private GraduateKnowledgeUniversityAliases() {}

    public static Map<String, String> all() {
        return ALIASES;
    }
}
