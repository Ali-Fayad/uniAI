/**
 * ProfileSettingsSection
 *
 * Responsibility:
 * - Render the profile settings form (profile fields + 2FA toggle + save actions).
 *
 * Does NOT:
 * - Own profile state (provided via props)
 * - Call APIs directly (delegates submit handler)
 */

import React from 'react';
import SettingsSection from '../../settings/SettingsSection';
import FormButton from '../../settings/FormButton';
import { TEXT } from '../../../constants/static';
import type { ProfileState } from '../../../hooks/useProfileSettings';
import AnimatedInput from '../../common/AnimatedInput';

export interface ProfileSettingsSectionProps {
  profile: ProfileState;
  isLoading: boolean;
  onProfileChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onProfileSubmit: (e: React.FormEvent) => Promise<void>;
}

const ProfileSettingsSection: React.FC<ProfileSettingsSectionProps> = ({
  profile,
  isLoading,
  onProfileChange,
  onProfileSubmit,
}) => {
  return (
    <SettingsSection title={TEXT.settings.profile.title} icon="person">
      <form onSubmit={onProfileSubmit}>
        <div className="grid grid-cols-1 gap-x-6 gap-y-6 sm:grid-cols-6">
          <div className="sm:col-span-3">
            <AnimatedInput
              id="first-name"
              name="firstName"
              label={TEXT.settings.profile.firstName}
              value={profile.firstName}
              onChange={onProfileChange}
            />
          </div>
          <div className="sm:col-span-3">
            <AnimatedInput
              id="last-name"
              name="lastName"
              label={TEXT.settings.profile.lastName}
              value={profile.lastName}
              onChange={onProfileChange}
            />
          </div>
          <div className="sm:col-span-4">
            <AnimatedInput
              id="username"
              name="username"
              label={TEXT.settings.profile.username}
              
              value={profile.username}
              onChange={onProfileChange}
            />
          </div>
          <div className="sm:col-span-4">
            <AnimatedInput
              id="email"
              name="email"
              label={TEXT.settings.profile.email}
              type="email"
              value={profile.email}
              disabled={true}
              onChange={onProfileChange}
            />
          </div>

          <div className="sm:col-span-6 border-t border-[var(--color-border)] pt-6 mt-2">
            <div className="flex items-center justify-between">
              <div className="flex flex-col">
                <span className="text-sm font-medium leading-6 text-[var(--color-textPrimary)]">
                  {TEXT.settings.profile.twoFactor.title}
                </span>
                <span className="text-sm text-[var(--color-textSecondary)]">
                  {TEXT.settings.profile.twoFactor.description}
                </span>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  className="sr-only peer"
                  type="checkbox"
                  name="twoFactorEnabled"
                  checked={profile.twoFactorEnabled}
                  onChange={onProfileChange}
                />
                <div className="w-11 h-6 bg-[var(--color-elevatedSurface)] peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-[var(--color-focusRing)]/30 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-[var(--color-surface)] after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-[var(--color-surface)] after:border-[var(--color-border)] after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[var(--color-primary)]"></div>
              </label>
            </div>
          </div>
        </div>

        <div className="mt-8 flex justify-end gap-x-4">
          <FormButton variant="secondary" type="button">
            {TEXT.common.cancel}
          </FormButton>
          <FormButton
            variant="primary"
            type="submit"
            disabled={isLoading}
          >
            {isLoading ? TEXT.settings.profile.saveButtonLoading : TEXT.settings.profile.saveButton}
          </FormButton>
        </div>
      </form>
    </SettingsSection>
  );
};

export default ProfileSettingsSection;
