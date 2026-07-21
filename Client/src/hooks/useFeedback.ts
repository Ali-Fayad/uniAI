/**
 * useFeedback Hook
 * 
 * Encapsulates feedback form logic for reuse across pages.
 */

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "./useAuth";
import { ROUTES } from "../router";
import { userService } from "../services/user";
import { isValidEmail } from "../lib/validation";

interface FeedbackFormState {
  rating: number;
  email: string;
  comment: string;
}

export const useFeedback = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [formState, setFormState] = useState<FeedbackFormState>({
    rating: 0,
    email: "",
    comment: "",
  });
  const [error, setError] = useState<string | null>(null);
  const setEmail = (email: string) => {
    setFormState((prev) => ({ ...prev, email }));
  };


  const setRating = (rating: number) => {
    setFormState((prev) => ({ ...prev, rating }));
  };

  const setComment = (comment: string) => {
    setFormState((prev) => ({ ...prev, comment }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // Check if user is authenticated before sending feedback
    if (!isAuthenticated) {
      navigate(ROUTES.AUTH);
      return;
    }

    if (formState.email.trim() && !isValidEmail(formState.email)) {
      setError('Please enter a valid email address.');
      return;
    }
    if (!formState.comment.trim()) {
      setError('Feedback cannot be empty.');
      return;
    }
    if (formState.comment.length > 5000) {
      setError('Feedback must be 5,000 characters or fewer.');
      return;
    }
    if (formState.rating < 0 || formState.rating > 5) {
      setError('Rating must be between 1 and 5.');
      return;
    }

    try {
      await userService.sendFeedback({
        rating: formState.rating > 0 ? formState.rating : undefined,
        content: formState.comment,
      });
    } catch (error: any) {
      if (error?.response?.status === 403) {
        alert("Please log in to submit feedback");
        const returnTo = encodeURIComponent(window.location.pathname);
        navigate(`${ROUTES.AUTH}?returnTo=${returnTo}`);
        return;
      }
      setError(error?.response?.data?.message || 'Unable to submit feedback.');
    }

    // Reset form
    setFormState({ rating: 0, email: "", comment: "" });
  };

  return {
    rating: formState.rating,
    email: formState.email,
    comment: formState.comment,
    setRating,
    setEmail,
    setComment,
    handleSubmit,
    error,
  };
};
