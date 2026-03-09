/**
 * useSettings – composition facade
 *
 * Composes the three focused settings hooks into a single return value so
 * SettingsPage can be updated incrementally.  Prefer consuming the focused
 * hooks directly in new code:
 *   - useProfileSettings  (profile form state + API)
 *   - useThemeSettings    (theme selection)
 *   - useSettingsFeedback (feedback form + API)
 */

import { useProfileSettings } from './useProfileSettings';
import { useThemeSettings } from './useThemeSettings';
import { useSettingsFeedback } from './useSettingsFeedback';

export const useSettings = () => {
  const profile = useProfileSettings();
  const theme = useThemeSettings();
  const feedbackHook = useSettingsFeedback();

  return {
    // Profile
    profile: profile.profile,
    isLoading: profile.isLoading,
    handleProfileChange: profile.handleProfileChange,
    handleProfileSubmit: profile.handleProfileSubmit,

    // Theme
    selectedTheme: theme.selectedTheme,
    setSelectedTheme: theme.setSelectedTheme,

    // Feedback
    feedback: feedbackHook.feedback,
    setFeedback: feedbackHook.setFeedback,
    handleFeedbackSubmit: feedbackHook.handleFeedbackSubmit,
  };
};
