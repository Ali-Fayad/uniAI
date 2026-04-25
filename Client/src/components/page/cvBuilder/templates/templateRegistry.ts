import { lazy } from 'react';
import type { ComponentType, LazyExoticComponent } from 'react';
import type { CVTemplateComponentProps, CVTemplatePalette } from './templateTypes';

type TemplateLazyComponent = LazyExoticComponent<ComponentType<CVTemplateComponentProps>>;

type TemplateLoader = () => Promise<{ default: ComponentType<CVTemplateComponentProps> }>;

const templateLoaders: Record<string, TemplateLoader> = {
  ModernTemplate: () => import('./ModernTemplate'),
  ClassicTemplate: () => import('./ClassicTemplate'),
  AcademicTemplate: () => import('./AcademicTemplate'),
  CreativeTemplate: () => import('./CreativeTemplate'),
  ExecutiveTemplate: () => import('./ExecutiveTemplate'),
  TechnicalTemplate: () => import('./TechnicalTemplate'),
  CompactTemplate: () => import('./CompactTemplate'),
};

const templateComponents: Record<string, TemplateLazyComponent> = Object.fromEntries(
  Object.entries(templateLoaders).map(([name, loader]) => [name, lazy(loader)]),
) as Record<string, TemplateLazyComponent>;

export interface TemplatePreviewConfig {
  palette: Pick<CVTemplatePalette, 'paper' | 'ink' | 'muted' | 'accent' | 'accentSoft' | 'rule' | 'sidebar' | 'sidebarInk'>;
  layout: 'single' | 'classic' | 'academic' | 'sidebar' | 'executive' | 'technical' | 'compact';
}

const templatePreviewConfigs: Record<string, TemplatePreviewConfig> = {
  ModernTemplate: {
    layout: 'single',
    palette: {
      paper: '#ffffff',
      ink: '#1f2933',
      muted: '#667085',
      accent: '#2563eb',
      accentSoft: '#dbeafe',
      rule: '#d9e2ec',
    },
  },
  ClassicTemplate: {
    layout: 'classic',
    palette: {
      paper: '#fffdf8',
      ink: '#1f1a17',
      muted: '#6f6259',
      accent: '#7a3415',
      accentSoft: '#f6e7d8',
      rule: '#d9c6b8',
    },
  },
  AcademicTemplate: {
    layout: 'academic',
    palette: {
      paper: '#ffffff',
      ink: '#202124',
      muted: '#5f6368',
      accent: '#174ea6',
      accentSoft: '#e8f0fe',
      rule: '#cfd8dc',
    },
  },
  CreativeTemplate: {
    layout: 'sidebar',
    palette: {
      paper: '#ffffff',
      ink: '#253238',
      muted: '#65737e',
      accent: '#0f766e',
      accentSoft: '#ccfbf1',
      rule: '#d7e4e2',
      sidebar: '#12343b',
      sidebarInk: '#f8fafc',
    },
  },
  ExecutiveTemplate: {
    layout: 'executive',
    palette: {
      paper: '#fbfbf8',
      ink: '#172033',
      muted: '#5b6472',
      accent: '#9a6a16',
      accentSoft: '#f3ead7',
      rule: '#d8d2c4',
    },
  },
  TechnicalTemplate: {
    layout: 'technical',
    palette: {
      paper: '#fdfefe',
      ink: '#101828',
      muted: '#667085',
      accent: '#047857',
      accentSoft: '#d1fae5',
      rule: '#d0d5dd',
    },
  },
  CompactTemplate: {
    layout: 'compact',
    palette: {
      paper: '#ffffff',
      ink: '#111827',
      muted: '#6b7280',
      accent: '#4f46e5',
      accentSoft: '#e0e7ff',
      rule: '#e5e7eb',
    },
  },
};

const fallbackComponent = templateComponents.ModernTemplate;

export const getTemplateComponent = (componentName?: string): TemplateLazyComponent => {
  if (!componentName) {
    return fallbackComponent;
  }

  return templateComponents[componentName] ?? fallbackComponent;
};

export const getTemplatePreviewConfig = (componentName?: string): TemplatePreviewConfig =>
  templatePreviewConfigs[componentName ?? ''] ?? templatePreviewConfigs.ModernTemplate;
