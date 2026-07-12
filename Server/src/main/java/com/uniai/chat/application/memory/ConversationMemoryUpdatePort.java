package com.uniai.chat.application.memory;

public interface ConversationMemoryUpdatePort {

    ConversationMemoryPatch proposeUpdate(ConversationMemoryUpdateRequest request);
}
