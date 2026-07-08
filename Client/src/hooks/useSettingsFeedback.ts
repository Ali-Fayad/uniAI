/**
 * useSettingsFeedback hook
 *
 * Owns the feedback form state and submission for the Settings page.
 * Uses the authenticated user's email automatically.
 * Separated from profile and theme concerns (ISP / SRP).
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { userService } from '../services/user';
import { ROUTES } from '../router';

export interface FeedbackState {
  rating: number;
  comment: string;
}

export interface UseSettingsFeedbackReturn {
  feedback: FeedbackState;
  setFeedback: React.Dispatch<React.SetStateAction<FeedbackState>>;
  handleFeedbackSubmit: (e: React.FormEvent) => Promise<void>;
}

export const useSettingsFeedback = (): UseSettingsFeedbackReturn => {
  const navigate = useNavigate();
  const [feedback, setFeedback] = useState<FeedbackState>({ rating: 0, comment: '' });

  const handleFeedbackSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await userService.sendFeedback({
        rating: feedback.rating > 0 ? feedback.rating : undefined,
        content: feedback.comment,
      });
      setFeedback({ rating: 0, comment: '' });
      console.log('Feedback submitted successfully');
    } catch (error: any) {
      if (error?.response?.status === 403) {
        alert('Please log in to submit feedback');
        const returnTo = encodeURIComponent(window.location.pathname);
        navigate(`${ROUTES.AUTH}?returnTo=${returnTo}`);
        return;
      }
      console.error('Failed to submit feedback', error);
    }
  };

  return { feedback, setFeedback, handleFeedbackSubmit };
};
