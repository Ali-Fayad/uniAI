import { motion, useAnimationControls, useReducedMotion } from 'framer-motion';
import { useEffect, useLayoutEffect, type ReactNode } from 'react';

interface AnimatedContentProps {
  children: ReactNode;
  className?: string;
  activeKey?: string;
  duration?: number;
  delay?: number;
  yOffset?: number;
  disabled?: boolean;
}

/**
 * AnimatedContent
 *
 * Responsibility:
 * - Provide a subtle reusable content entrance transition
 *
 * Does NOT:
 * - Remount child content
 * - Fetch data
 * - Perform API calls
 */
const AnimatedContent = ({
  children,
  className = '',
  activeKey,
  duration = 0.2,
  delay = 0,
  yOffset = 8,
  disabled = false,
}: AnimatedContentProps) => {
  const controls = useAnimationControls();
  const prefersReducedMotion = useReducedMotion();
  const shouldDisableAnimation = disabled || prefersReducedMotion;

  useLayoutEffect(() => {
    if (shouldDisableAnimation) {
      return;
    }

    controls.set({ opacity: 0, y: yOffset });

    const frameId = window.requestAnimationFrame(() => {
      void controls.start({
        opacity: 1,
        y: 0,
        transition: {
          duration,
          delay,
          ease: 'easeOut',
        },
      });
    });

    return () => {
      window.cancelAnimationFrame(frameId);
    };
  }, [activeKey, controls, delay, duration, shouldDisableAnimation, yOffset]);

  useEffect(() => {
    if (shouldDisableAnimation) {
      controls.set({ opacity: 1, y: 0 });
    }
  }, [controls, shouldDisableAnimation]);

  if (shouldDisableAnimation) {
    return <div className={className}>{children}</div>;
  }

  return (
    <motion.div
      initial={false}
      animate={controls}
      className={className}
      style={{ willChange: 'opacity, transform' }}
    >
      {children}
    </motion.div>
  );
};

export default AnimatedContent;
