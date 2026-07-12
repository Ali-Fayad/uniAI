package com.uniai.chat.application.port.out;

import com.uniai.chat.application.citation.GraduateKnowledgeRetrievalResult;
import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;

public interface GraduateKnowledgeRetrievalPort {

    GraduateKnowledgeRetrievalResult retrieveContext(GraduateKnowledgeQuery query);
}
