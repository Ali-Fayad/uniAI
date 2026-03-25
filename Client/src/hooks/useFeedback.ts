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

    // Check if user is authenticated before sending feedback
    if (!isAuthenticated) {
      navigate(ROUTES.AUTH);
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
      throw error;
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
  };
};
