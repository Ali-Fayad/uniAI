/**
 * PersonalInfoSettingsSection
 *
 * Responsibility:
 * - Render the Settings section entry-point to Personal Info.
 *
 * Does NOT:
 * - Perform navigation directly (delegates click handler)
 */

import React from 'react';
import SettingsSection from '../../settings/SettingsSection';
import FormButton from '../../settings/FormButton';

export interface PersonalInfoSettingsSectionProps {
  onUpdatePersonalInfo: () => void;
}

const PersonalInfoSettingsSection: React.FC<PersonalInfoSettingsSectionProps> = ({
  onUpdatePersonalInfo,
}) => {
  return (
    <SettingsSection title="Personal Information" icon="badge">
      <p className="text-sm text-[var(--color-textSecondary)] mb-6">
        Your personal information helps build your CV
      </p>
      <div className="flex justify-end">
        <FormButton
          type="button"
          onClick={onUpdatePersonalInfo}
        >
          Update Personal Info
        </FormButton>
      </div>
    </SettingsSection>
  );
};

export default PersonalInfoSettingsSection;
