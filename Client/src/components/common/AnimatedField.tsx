import React from 'react';
import { useScrollAnimation } from '../../hooks/useScrollAnimation';

interface AnimatedFieldProps {
  children: React.ReactNode;
  /** Animation delay in milliseconds */
  delay?: number;
  /** IntersectionObserver threshold (0–1). Lower = triggers earlier. */
  threshold?: number;
  /** CSS transition string applied during the animation */
  transition?: string;
}

/**
 * AnimatedField
 *
 * Shared scroll-triggered animation wrapper extracted from MainPage and
 * SettingsPage into a single reusable component (SRP / DRY).
 *
 * Callers can override threshold/transition to match local needs, keeping
 * the component open for extension without modifying it (OCP).
 */
const AnimatedField: React.FC<AnimatedFieldProps> = ({
  children,
  delay = 0,
  threshold = 0.1,
  transition,
}) => {
  const ref = useScrollAnimation({ delay, threshold, ...(transition ? { transition } : {}) });
  return <div ref={ref}>{children}</div>;
};

export default AnimatedField;
