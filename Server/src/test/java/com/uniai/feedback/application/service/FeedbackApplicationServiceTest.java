package com.uniai.feedback.application.service;

import com.uniai.feedback.application.dto.command.SubmitFeedbackCommand;
import com.uniai.feedback.domain.model.Feedback;
import com.uniai.feedback.domain.repository.FeedbackRepository;
import com.uniai.shared.exception.FeedbackNotValidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeedbackApplicationServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackApplicationService feedbackApplicationService;

    @Test
    void submitFeedbackShouldSaveWhenCommandIsValid() {
        SubmitFeedbackCommand command = SubmitFeedbackCommand.builder()
                .rating(5)
                .content("Great platform")
                .build();

        feedbackApplicationService.submitFeedback(7L, command);

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository).save(captor.capture());

        Feedback saved = captor.getValue();
        assertEquals(7L, saved.getUserId());
        assertEquals("Great platform", saved.getContent());
        assertEquals(5, saved.getRating());
    }

    @Test
    void submitFeedbackShouldRejectInvalidRating() {
        SubmitFeedbackCommand command = SubmitFeedbackCommand.builder()
                .rating(7)
                .content("Too high rating")
                .build();

        assertThrows(
                FeedbackNotValidException.class,
                () -> feedbackApplicationService.submitFeedback(3L, command)
        );
    }
}
