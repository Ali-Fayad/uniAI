// Centralized theme tokens for uniAI
// - This file centralizes all color hex values using semantic names
// - It is TypeScript-typed and supports multiple named themes
// - For now this only creates the tokens; wiring (CSS variables, context, tailwind config) is a follow-up

export type ThemeName = "light" | "dark" | "future";

export interface ThemeColors {
  // Brand
  primary: string;
  primaryVariant?: string;
  secondary: string;
  // Background / surfaces
  background: string;
  surface: string;
  elevatedSurface?: string;
  // Text
  textPrimary: string;
  textSecondary: string;
  textMuted?: string;
  // Borders / dividers
  border: string;
  // Interactive
  link: string;
  focusRing: string;
  // Semantic
  success: string;
  error: string;
  warning: string;
  info: string;
  // Misc (useful tokens seen in the codebase)
  accent: string;
  chipBackground?: string;
  // Utility / custom tokens used in code
  customPrimary?: string; // maps to existing `bg-custom-primary` usages
  customSecondary?: string; // maps to `bg-custom-secondary` usages
}

// Light theme (primary set to the project's brand color candidates)
export const light: ThemeColors = {
  // Align to existing project palette (from Client/index.html and index.css)
  primary: "#b7ae9f", // matches index.html primary
  primaryVariant: "#a69d8f", // hover/variant seen in index.css
  // keep secondary used in some places (#b0b3a6) and a separate customSecondary (tailwind config uses #C5C7BC)
  secondary: "#b0b3a6",

  background: "#f7f7f7", // matches index.html background-light
  surface: "#ffffff",
  elevatedSurface: "#f8fafb",

  textPrimary: "#151514", // used in labels/headings
  textSecondary: "#6b7280", // gray-500
  textMuted: "#9ca3af",

  border: "#e5e7eb", // gray-200

  link: "#b7ae9f",
  focusRing: "#b7ae9f",

  success: "#16a34a",
  error: "#dc2626",
  warning: "#f59e0b",
  info: "#0ea5e9",

  accent: "#FBF3D1", // maps to custom-accent from index.html
  chipBackground: "#f3f4f6",

  customPrimary: "#B6AE9F", // tailwind custom-primary
  customSecondary: "#C5C7BC", // tailwind custom-secondary
};

// Dark theme (values intentionally different; can be tuned later)
export const dark: ThemeColors = {
  primary: "#FB2576", // Last color: Font text / Primary action
  primaryVariant: "#D91C61",
  secondary: "#000000", // First color: Black (used for secondary/contrast)

  background: "#150050", // Second color: Background
  surface: "#3F0071", // Third color: Component backgrounds
  elevatedSurface: "#2A0048", // Darker than surface for sidebar items

  textPrimary: "#FB2576", // Last color: Font text
  textSecondary: "#E0E0E0", // Light gray for readability on purple
  textMuted: "#9ca3af",

  border: "#000000", // First color: Black borders

  link: "#FB2576",
  focusRing: "#FB2576",

  success: "#34d399",
  error: "#f87171",
  warning: "#f59e0b",
  info: "#60a5fa",

  accent: "#FB2576",
  chipBackground: "#000000", // First color

  customPrimary: "#FB2576",
  customSecondary: "#000000",
};

// Placeholder future theme (skeleton for expansion)
export const future: ThemeColors = {
  primary: "#7c3aed",
  primaryVariant: "#6d28d9",
  secondary: "#06b6d4",

  background: "#0f1724",
  surface: "#0b1220",
  elevatedSurface: "#071028",

  textPrimary: "#e6eef8",
  textSecondary: "#9aa6b2",
  textMuted: "#94a3b8",

  border: "#12303f",

  link: "#7c3aed",
  focusRing: "#7c3aed",

  success: "#34d399",
  error: "#fb7185",
  warning: "#f59e0b",
  info: "#60a5fa",

  accent: "#7c3aed",
  chipBackground: "#071028",

  customPrimary: "#7c3aed",
  customSecondary: "#071028",
};

export const themes: Record<ThemeName, ThemeColors> = {
  light,
  dark,
  future,
};

// Helper: convert a ThemeColors object into a map of CSS custom properties
export const themeToCssVars = (t: ThemeColors) => {
  const map: Record<string, string> = {};
  (Object.keys(t) as Array<keyof ThemeColors>).forEach((key) => {
    const val = t[key];
    if (val) map[`--color-${key}`] = val;
  });
  return map;
};

// Apply a named theme to document.documentElement by setting CSS vars
export const applyThemeByName = (name: ThemeName) => {
  try {
    const theme = themes[name] || themes.light;
    const vars = themeToCssVars(theme);
    Object.entries(vars).forEach(([k, v]) => {
      document.documentElement.style.setProperty(k, v);
    });
    // persist selection
    localStorage.setItem("theme", name);
  } catch (e) {
    // ignore errors (e.g., server-side rendering)
    // console.warn('Failed to apply theme', e);
  }
};

export const getSavedTheme = (): ThemeName => {
  try {
    const stored = localStorage.getItem("theme") as ThemeName | null;
    if (stored && Object.prototype.hasOwnProperty.call(themes, stored)) return stored;
  } catch (e) {
    // ignore
  }
  return "light";
};

// Usage notes (keep concise):
// - Import `themes` and apply to :root (or a provider) by setting the CSS variables produced by `themeToCssVars`.
// - Example (in React):
//    import { themes, themeToCssVars } from './styles/themes';
//    const vars = themeToCssVars(themes.light);
//    Object.entries(vars).forEach(([k,v]) => document.documentElement.style.setProperty(k, v));
// - Tailwind: reference CSS vars in `tailwind.config.js` or use them in classes: `bg-[var(--color-surface)] text-[var(--color-textPrimary)]`.
// - Next steps: replace hex literals across the codebase with `var(--color-...)` or with a small utility that resolves tokens.

// Usage note: call applyThemeByName('dark') to switch themes at runtime.

export default themes;
