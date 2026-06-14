package com.uniai.admin.application.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserFeedbackResponse {
    private Long id;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}
