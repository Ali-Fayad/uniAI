/**
 * DangerZoneSettingsSection
 *
 * Responsibility:
 * - Render the Settings danger zone section.
 *
 * Does NOT:
 * - Execute destructive actions (no handlers; UI only)
 */

import React from 'react';
import SettingsSection from '../../settings/SettingsSection';
import FormButton from '../../settings/FormButton';
import { TEXT } from '../../../constants/static';

const DangerZoneSettingsSection: React.FC = () => {
  return (
    <SettingsSection title={TEXT.settings.dangerZone.title} icon="warning">
      <p className="text-sm text-[var(--color-error)]/80 mb-6">
        {TEXT.settings.dangerZone.description}
      </p>
      <div className="flex flex-col sm:flex-row gap-4">
        <FormButton variant="danger">{TEXT.settings.dangerZone.changePassword}</FormButton>
        <FormButton
          variant="ghost"
          className="border-2 border-[var(--color-error)] text-[var(--color-error)]"
        >
          {TEXT.settings.dangerZone.deleteAccount}
        </FormButton>
      </div>
    </SettingsSection>
  );
};

export default DangerZoneSettingsSection;
