package com.uniai.builder;

import com.uniai.model.Feedback;

public class FeedbackBuilder {
        public static Feedback getFeedbackFromFeedbackRequest(com.uniai.dto.user.FeedbackRequest feedbackRequest) {
        return Feedback.builder()
                .email(feedbackRequest.getEmail())
                .comment(feedbackRequest.getComment())
                .build();
    }
}
