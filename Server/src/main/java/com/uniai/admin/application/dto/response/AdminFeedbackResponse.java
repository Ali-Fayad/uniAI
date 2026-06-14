package com.uniai.admin.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFeedbackResponse {
    private Long id;
    private Long userId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}
