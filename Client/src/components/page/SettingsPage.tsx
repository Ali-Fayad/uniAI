import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useSettings } from '../../hooks/useSettings';
import { ROUTES } from '../../router';
import SettingsPageShell from './settings/SettingsPageShell';

/**
 * SettingsPage
 *
 * Responsibility:
 * - Composition-only route component for Settings.
 *
 * Does NOT:
 * - Render section UI (delegates to SettingsPageShell)
 * - Own settings logic (delegates to useSettings)
 */
const SettingsPage: React.FC = () => {
  const navigate = useNavigate();

  const {
    profile,
    feedback,
    isLoading,
    selectedTheme,
    setSelectedTheme,
    handleProfileChange,
    handleProfileSubmit,
    handleFeedbackSubmit,
    setFeedback,
  } = useSettings();

  return (
    <SettingsPageShell
      profile={profile}
      feedback={feedback}
      isLoading={isLoading}
      selectedTheme={selectedTheme}
      setSelectedTheme={setSelectedTheme}
      handleProfileChange={handleProfileChange}
      handleProfileSubmit={handleProfileSubmit}
      handleFeedbackSubmit={handleFeedbackSubmit}
      setFeedback={setFeedback}
      onUpdatePersonalInfo={() => navigate(ROUTES.PERSONAL_INFO, { state: { fromOnboarding: false } })}
    />
  );
};

export default SettingsPage;
