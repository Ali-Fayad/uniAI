package com.uniai.chat.application.port.out;

import com.uniai.chat.application.retrieval.GraduateKnowledgeQuery;

public interface GraduateKnowledgeRetrievalPort {

    String retrieveContext(GraduateKnowledgeQuery query);
}
