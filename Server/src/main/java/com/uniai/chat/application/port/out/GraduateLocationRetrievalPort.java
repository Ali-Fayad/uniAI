package com.uniai.chat.application.port.out;

import com.uniai.chat.application.citation.GraduateKnowledgeRetrievalResult;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;

/** Raw structured location retrieval kept separate from the canonical catalog projection. */
public interface GraduateLocationRetrievalPort {

    GraduateKnowledgeRetrievalResult retrieveContext(GraduateKnowledgeQuery query);
}
