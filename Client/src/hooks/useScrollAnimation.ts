/**
 * useScrollAnimation Hook
 * 
 * Reusable hook for scroll-triggered animations.
 * Used across multiple pages for consistent animation behavior.
 */

import { useEffect, useRef } from "react";

interface UseScrollAnimationOptions {
  delay?: number;
  threshold?: number;
  translateY?: string;
  opacity?: string;
  transition?: string;
}

export const useScrollAnimation = (options: UseScrollAnimationOptions = {}) => {
  const {
    delay = 0,
    threshold = 0.15,
    translateY = "20px",
    opacity = "0",
    transition = "opacity 500ms ease, transform 500ms ease",
  } = options;

  const ref = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    // Initial state
    el.style.opacity = opacity;
    el.style.transform = `translateY(${translateY})`;
    el.style.transition = transition;

    const obs = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            // Animate in
            setTimeout(() => {
              el.style.opacity = "1";
              el.style.transform = "translateY(0)";
            }, delay);
          } else {
            // Reverse animation (hide) when scrolling up/away
            el.style.opacity = opacity;
            el.style.transform = `translateY(${translateY})`;
          }
        });
      },
      { threshold }
    );

    obs.observe(el);
    return () => obs.disconnect();
  }, [delay, threshold, translateY, opacity, transition]);

  return ref;
};
