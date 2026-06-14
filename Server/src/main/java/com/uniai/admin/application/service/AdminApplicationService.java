package com.uniai.admin.application.service;

import com.uniai.admin.application.dto.response.AdminOverviewResponse;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.domain.repository.MessageRepository;
import com.uniai.feedback.domain.repository.FeedbackRepository;
import com.uniai.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Thin admin application service for basic admin access and overview aggregation.
 * Keeps the admin module orchestration in the application layer.
 */
@Service
@RequiredArgsConstructor
public class AdminApplicationService {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final FeedbackRepository feedbackRepository;

    public String getHealthMessage() {
        return "Admin access granted";
    }

    public AdminOverviewResponse getOverview() {
        long totalUsers = userRepository.count();
        long totalChats = chatRepository.count();
        long totalMessages = messageRepository.count();
        long totalFeedback = feedbackRepository.count();

        double averageChatsPerUser = totalUsers == 0 ? 0.0 : totalChats / (double) totalUsers;
        double averageMessagesPerChat = totalChats == 0 ? 0.0 : totalMessages / (double) totalChats;
        double averageMessagesPerUser = totalUsers == 0 ? 0.0 : totalMessages / (double) totalUsers;

        return AdminOverviewResponse.builder()
                .totalUsers(totalUsers)
                .totalChats(totalChats)
                .totalMessages(totalMessages)
                .totalFeedback(totalFeedback)
                .averageChatsPerUser(averageChatsPerUser)
                .averageMessagesPerChat(averageMessagesPerChat)
                .averageMessagesPerUser(averageMessagesPerUser)
                .build();
    }
}
