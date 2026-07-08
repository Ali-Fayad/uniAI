/**
 * SettingsPageHeader
 *
 * Responsibility:
 * - Render the Settings page header.
 *
 * Does NOT:
 * - Own settings state
 * - Perform navigation
 */

import React from 'react';

export interface SettingsPageHeaderProps {
  title: string;
}

const SettingsPageHeader: React.FC<SettingsPageHeaderProps> = ({ title }) => {
  return (
    <div className="md:flex md:items-center md:justify-between">
      <div className="min-w-0 flex-1">
        <h2 className="text-3xl font-bold leading-7 text-[var(--color-textPrimary)] sm:truncate sm:text-4xl sm:tracking-tight">
          {title}
        </h2>
      </div>
    </div>
  );
};

export default SettingsPageHeader;
