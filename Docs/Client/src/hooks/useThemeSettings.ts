/**
 * useThemeSettings hook
 *
 * Owns theme-selection state for the Settings page.
 * Separated from profile and feedback concerns (ISP / SRP).
 */

import { useState, useEffect } from 'react';
import { applyThemeByName, getSavedTheme } from '../styles/themes';
import type { ThemeName } from '../styles/themes';

export interface UseThemeSettingsReturn {
  selectedTheme: ThemeName;
  setSelectedTheme: (theme: ThemeName) => void;
}

export const useThemeSettings = (): UseThemeSettingsReturn => {
  const [selectedTheme, setSelectedTheme] = useState<ThemeName>(getSavedTheme);

  useEffect(() => {
    applyThemeByName(selectedTheme);
  }, [selectedTheme]);

  return { selectedTheme, setSelectedTheme };
};
