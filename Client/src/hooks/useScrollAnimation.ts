import { useRef, useEffect } from 'react';

/**
 * SRP: this hook is responsible only for wiring an IntersectionObserver
 * to a DOM element and toggling fade-in/out CSS on scroll.
 *
 * OCP: behaviour is extended via options without modifying the hook itself.
 */
export interface ScrollAnimationOptions {
  /** Delay in ms before the element animates in (default: 0). */
  delay?: number;
  /** CSS transition duration in ms (default: 500). */
  duration?: number;
  /** IntersectionObserver visibility threshold 0–1 (default: 0.15). */
  threshold?: number;
}

export function useScrollAnimation({
  delay = 0,
  duration = 500,
  threshold = 0.15,
}: ScrollAnimationOptions = {}) {
  const ref = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    el.style.opacity = '0';
    el.style.transform = 'translateY(20px)';
    el.style.transition = `opacity ${duration}ms ease, transform ${duration}ms ease`;

    const obs = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            setTimeout(() => {
              el.style.opacity = '1';
              el.style.transform = 'translateY(0)';
            }, delay);
          } else {
            el.style.opacity = '0';
            el.style.transform = 'translateY(20px)';
          }
        });
      },
      { threshold },
    );

    obs.observe(el);
    return () => obs.disconnect();
  }, [delay, duration, threshold]);

  return ref;
}
