import { useState, useEffect } from 'react';
import { getSavedTheme, themes } from '../styles/themes';
import type { ThemeName } from '../styles/themes';

export function useTheme() {
  const [currentTheme, setCurrentTheme] = useState<ThemeName>(getSavedTheme());

  useEffect(() => {
    const handleThemeChange = (e: Event) => {
      const customEvent = e as CustomEvent<ThemeName>;
      setCurrentTheme(customEvent.detail);
    };

    window.addEventListener('themeChanged', handleThemeChange);
    return () => window.removeEventListener('themeChanged', handleThemeChange);
  }, []);

  return {
    themeName: currentTheme,
    colors: themes[currentTheme]
  };
}
