/**
 * ThemeSettingsSection
 *
 * Responsibility:
 * - Render theme selection cards.
 *
 * Does NOT:
 * - Persist theme choice (delegates state via props)
 */

import React from 'react';
import SettingsSection from '../../settings/SettingsSection';
import { TEXT } from '../../../constants/static';
import type { ThemeName } from '../../../styles/themes';

export interface ThemeSettingsSectionProps {
  selectedTheme: ThemeName;
  setSelectedTheme: (value: ThemeName) => void;
}

const ThemeSettingsSection: React.FC<ThemeSettingsSectionProps> = ({
  selectedTheme,
  setSelectedTheme,
}) => {
  return (
    <SettingsSection title={TEXT.settings.theme.title} icon="palette">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <label
          className={`relative flex cursor-pointer rounded-lg border p-4 shadow-sm focus:outline-none ring-2 ${
            selectedTheme === 'light'
              ? 'ring-[var(--color-focusRing)] bg-[var(--color-surface)]'
              : 'bg-[var(--color-surface)] border-[var(--color-border)]'
          }`}
          onClick={() => setSelectedTheme('light')}
        >
          <input
            checked={selectedTheme === 'light'}
            onChange={() => setSelectedTheme('light')}
            className="sr-only"
            name="theme-option"
            type="radio"
            value="light"
          />
          <span className="flex flex-1">
            <span className="flex flex-col">
              <span className="block text-sm font-medium text-[var(--color-textPrimary)] flex items-center gap-2">
                <span className="material-symbols-outlined text-yellow-500">light_mode</span>
                {TEXT.settings.theme.light.title}
              </span>
              <span className="mt-1 flex items-center text-sm text-[var(--color-textSecondary)]">
                {TEXT.settings.theme.light.description}
              </span>
            </span>
          </span>
          {selectedTheme === 'light' && (
            <span
              className="material-symbols-outlined text-[var(--color-primary)]"
              style={{ fontVariationSettings: "'FILL' 1" }}
            >
              check_circle
            </span>
          )}
        </label>

        <label
          className={`relative flex cursor-pointer rounded-lg border p-4 shadow-sm focus:outline-none ${
            selectedTheme === 'dark'
              ? 'ring-[var(--color-focusRing)] bg-[var(--color-background)] border-[var(--color-border)]'
              : 'bg-[var(--color-surface)] border-[var(--color-border)]'
          }`}
          onClick={() => setSelectedTheme('dark')}
        >
          <input
            className="sr-only"
            name="theme-option"
            type="radio"
            value="dark"
            checked={selectedTheme === 'dark'}
            onChange={() => setSelectedTheme('dark')}
          />
          <span className="flex flex-1">
            <span className="flex flex-col">
              <span className="block text-sm font-medium text-[var(--color-textPrimary)] flex items-center gap-2">
                <span className="material-symbols-outlined text-[var(--color-textPrimary)]">dark_mode</span>
                {TEXT.settings.theme.dark.title}
              </span>
              <span className="mt-1 flex items-center text-[var(--color-textSecondary)]">
                {TEXT.settings.theme.dark.description}
              </span>
            </span>
          </span>
          {selectedTheme === 'dark' && (
            <span
              className="material-symbols-outlined text-[var(--color-primary)]"
              style={{ fontVariationSettings: "'FILL' 1" }}
            >
              check_circle
            </span>
          )}
        </label>
      </div>
    </SettingsSection>
  );
};

export default ThemeSettingsSection;
