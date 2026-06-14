package com.uniai.admin.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOverviewResponse {
    private long totalUsers;
    private long totalChats;
    private long totalMessages;
    private long totalFeedback;
    private double averageChatsPerUser;
    private double averageMessagesPerChat;
    private double averageMessagesPerUser;
}
