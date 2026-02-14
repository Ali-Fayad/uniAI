/**
 * useSettings Hook
 * 
 * Encapsulates settings page business logic including:
 * - Profile management
 * - Theme preferences
 * - User feedback
 */

import { useState, useEffect } from "react";
import { Storage } from "../utils/Storage";
import { userService } from "../services/user";
import type { UpdateUserDto } from "../types/dto";
import { applyThemeByName, getSavedTheme } from "../styles/themes";
import type { ThemeName } from "../styles/themes";

interface ProfileState {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  twoFactorEnabled: boolean;
}

interface FeedbackState {
  rating: number;
  comment: string;
}

export const useSettings = () => {
  // Initialize state from storage
  const [profile, setProfile] = useState<ProfileState>(() => {
    const stored = Storage.getUser();
    return {
      firstName: stored?.firstName || "",
      lastName: stored?.lastName || "",
      username: stored?.username || "",
      email: stored?.email || "",
      twoFactorEnabled: stored?.twoFactorEnabled || false,
    };
  });

  const [feedback, setFeedback] = useState<FeedbackState>({
    rating: 0,
    comment: "",
  });

  const [isLoading, setIsLoading] = useState(false);
  const [selectedTheme, setSelectedTheme] = useState<ThemeName>(() => getSavedTheme());

  // Apply theme when it changes
  useEffect(() => {
    applyThemeByName(selectedTheme);
  }, [selectedTheme]);

  // Load user data if storage is empty
  useEffect(() => {
    if (!Storage.getUser()) {
      const loadUserData = async () => {
        try {
          const userData = await userService.getMe();
          setProfile({
            firstName: userData.firstName || "",
            lastName: userData.lastName || "",
            username: userData.username || "",
            email: userData.email || "",
            twoFactorEnabled: userData.isTwoFacAuth,
          });

          Storage.setUser({
            id: 0,
            ...userData,
            twoFactorEnabled: userData.isTwoFacAuth,
          } as any);
        } catch (error) {
          console.error("Failed to fetch user data", error);
        }
      };
      loadUserData();
    }
  }, []);

  const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setProfile((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const stored = Storage.getUser();
      const updateDto: UpdateUserDto = {};
      let hasChanges = false;

      // Only include fields that have changed
      if (profile.firstName !== (stored?.firstName || "")) {
        updateDto.firstName = profile.firstName;
        hasChanges = true;
      }
      if (profile.lastName !== (stored?.lastName || "")) {
        updateDto.lastName = profile.lastName;
        hasChanges = true;
      }
      if (profile.username !== (stored?.username || "")) {
        updateDto.username = profile.username;
        hasChanges = true;
      }
      if (profile.twoFactorEnabled !== (stored?.twoFactorEnabled || false)) {
        updateDto.enableTwoFactor = profile.twoFactorEnabled;
        hasChanges = true;
      }

      if (!hasChanges) {
        console.log("No changes to save");
        setIsLoading(false);
        return;
      }

      const updatedUser = await userService.updateMe(updateDto);

      // Update local state and storage
      setProfile((prev) => ({
        ...prev,
        firstName: updatedUser.firstName || "",
        lastName: updatedUser.lastName || "",
        username: updatedUser.username || "",
      }));

      const currentStorage = Storage.getUser();
      if (currentStorage) {
        Storage.setUser({
          ...currentStorage,
          firstName: updatedUser.firstName,
          lastName: updatedUser.lastName,
          username: updatedUser.username,
          twoFactorEnabled: updatedUser.isTwoFacAuth,
        });
      }

      console.log("Profile updated successfully");
    } catch (error) {
      console.error("Failed to update profile", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleFeedbackChange = (rating: number, comment: string) => {
    setFeedback({ rating, comment });
  };

  const handleFeedbackSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const commentWithRating = `Rating: ${feedback.rating}/5. ${feedback.comment}`;
      await userService.sendFeedback({
        email: profile.email,
        comment: commentWithRating,
      });
      setFeedback({ rating: 0, comment: "" });
      console.log("Feedback submitted successfully");
    } catch (error) {
      console.error("Failed to submit feedback", error);
    }
  };

  return {
    profile,
    feedback,
    isLoading,
    selectedTheme,
    setSelectedTheme,
    handleProfileChange,
    handleProfileSubmit,
    handleFeedbackChange,
    handleFeedbackSubmit,
    setFeedback,
  };
};
