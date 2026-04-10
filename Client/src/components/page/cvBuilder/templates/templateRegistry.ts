import { lazy } from 'react';
import type { ComponentType, LazyExoticComponent } from 'react';
import type { CVTemplateComponentProps } from './templateTypes';

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

const fallbackComponent = lazy(templateLoaders.ModernTemplate);

const cache = new Map<string, TemplateLazyComponent>();

export const getTemplateComponent = (componentName?: string): TemplateLazyComponent => {
  if (!componentName) {
    return fallbackComponent;
  }

  if (cache.has(componentName)) {
    return cache.get(componentName)!;
  }

  const loader = templateLoaders[componentName];
  if (!loader) {
    return fallbackComponent;
  }

  const lazyComponent = lazy(loader);
  cache.set(componentName, lazyComponent);
  return lazyComponent;
};
