import { useEffect, useRef } from "react";

type Options = {
  threshold?: number;
  duration?: number; // ms
};

export const useScrollAnimation = (delay = 0, opts: Options = {}) => {
  const ref = useRef<HTMLDivElement | null>(null);
  const { threshold = 0.15, duration = 500 } = opts;

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    // Initial state
    el.style.opacity = "0";
    el.style.transform = "translateY(20px)";
    el.style.transition = `opacity ${duration}ms ease, transform ${duration}ms ease`;

    const obs = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            setTimeout(() => {
              el.style.opacity = "1";
              el.style.transform = "translateY(0)";
            }, delay);
          } else {
            el.style.opacity = "0";
            el.style.transform = "translateY(20px)";
          }
        });
      },
      { threshold }
    );

    obs.observe(el);
    return () => obs.disconnect();
  }, [delay, threshold, duration]);

  return ref;
};

export default useScrollAnimation;
