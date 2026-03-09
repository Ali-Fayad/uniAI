/**
 * useSettingsFeedback hook
 *
 * Owns the feedback form state and submission for the Settings page.
 * Uses the authenticated user's email automatically.
 * Separated from profile and theme concerns (ISP / SRP).
 */

import { useState } from 'react';
import { userService } from '../services/user';
import { Storage } from '../utils/Storage';

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
  const [feedback, setFeedback] = useState<FeedbackState>({ rating: 0, comment: '' });

  const handleFeedbackSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const email = Storage.getUser()?.email ?? '';
      const commentWithRating = `Rating: ${feedback.rating}/5. ${feedback.comment}`;
      await userService.sendFeedback({ email, comment: commentWithRating });
      setFeedback({ rating: 0, comment: '' });
      console.log('Feedback submitted successfully');
    } catch (error) {
      console.error('Failed to submit feedback', error);
    }
  };

  return { feedback, setFeedback, handleFeedbackSubmit };
};
