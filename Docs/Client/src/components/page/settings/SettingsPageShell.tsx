/**
 * SettingsPageShell
 *
 * Responsibility:
 * - Render the Settings page layout by composing extracted section components.
 *
 * Does NOT:
 * - Own business logic (provided via hooks + props)
 */

import React from 'react';
import AnimatedField from '../../common/AnimatedField';
import { TEXT } from '../../../constants/static';
import type { ProfileState } from '../../../hooks/useProfileSettings';
import type { FeedbackState } from '../../../hooks/useSettingsFeedback';
import type { ThemeName } from '../../../styles/themes';
import DangerZoneSettingsSection from './DangerZoneSettingsSection';
import FeedbackSettingsSection from './FeedbackSettingsSection';
import PersonalInfoSettingsSection from './PersonalInfoSettingsSection';
import ProfileSettingsSection from './ProfileSettingsSection';
import SettingsPageHeader from './SettingsPageHeader';
import ThemeSettingsSection from './ThemeSettingsSection';

export interface SettingsPageShellProps {
  profile: ProfileState;
  feedback: FeedbackState;
  isLoading: boolean;
  selectedTheme: ThemeName;
  setSelectedTheme: (value: ThemeName) => void;
  handleProfileChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  handleProfileSubmit: (e: React.FormEvent) => Promise<void>;
  handleFeedbackSubmit: (e: React.FormEvent) => Promise<void>;
  setFeedback: React.Dispatch<React.SetStateAction<FeedbackState>>;
  onUpdatePersonalInfo: () => void;
}

const SettingsPageShell: React.FC<SettingsPageShellProps> = ({
  profile,
  feedback,
  isLoading,
  selectedTheme,
  setSelectedTheme,
  handleProfileChange,
  handleProfileSubmit,
  handleFeedbackSubmit,
  setFeedback,
  onUpdatePersonalInfo,
}) => {
  return (
    <main className="flex-grow py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto space-y-8">
        <SettingsPageHeader title={TEXT.settings.title} />

        <AnimatedField delay={0} threshold={0.05} transition="opacity 300ms ease, transform 300ms ease">
          <ProfileSettingsSection
            profile={profile}
            isLoading={isLoading}
            onProfileChange={handleProfileChange}
            onProfileSubmit={handleProfileSubmit}
          />
        </AnimatedField>

        <AnimatedField delay={25} threshold={0.05} transition="opacity 300ms ease, transform 300ms ease">
          <PersonalInfoSettingsSection onUpdatePersonalInfo={onUpdatePersonalInfo} />
        </AnimatedField>

        <AnimatedField delay={50} threshold={0.05} transition="opacity 300ms ease, transform 300ms ease">
          <ThemeSettingsSection selectedTheme={selectedTheme} setSelectedTheme={setSelectedTheme} />
        </AnimatedField>

        <AnimatedField delay={100} threshold={0.05} transition="opacity 300ms ease, transform 300ms ease">
          <FeedbackSettingsSection
            feedback={feedback}
            setFeedback={setFeedback}
            onFeedbackSubmit={handleFeedbackSubmit}
          />
        </AnimatedField>

        <AnimatedField delay={150} threshold={0.05} transition="opacity 300ms ease, transform 300ms ease">
          <DangerZoneSettingsSection />
        </AnimatedField>
      </div>
    </main>
  );
};

export default SettingsPageShell;
