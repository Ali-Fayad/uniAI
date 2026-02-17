import { motion } from 'framer-motion';
import { ReactNode } from 'react';

type Direction = 'left' | 'right' | 'up' | 'down';

interface SlideInProps {
  children: ReactNode;
  direction?: Direction;
  delay?: number;
  duration?: number;
  distance?: number;
  className?: string;
}

/**
 * SlideIn Animation Component
 * 
 * Responsibilities:
 * - Slide elements in from specified direction
 * - Support customizable delay, duration, and distance
 * - Respect reduced motion preferences
 */
const SlideIn = ({ 
  children, 
  direction = 'up', 
  delay = 0, 
  duration = 0.4,
  distance = 30,
  className = "" 
}: SlideInProps) => {
  // Calculate initial position based on direction
  const getInitialPosition = () => {
    switch (direction) {
      case 'left':
        return { x: -distance, y: 0 };
      case 'right':
        return { x: distance, y: 0 };
      case 'up':
        return { x: 0, y: distance };
      case 'down':
        return { x: 0, y: -distance };
      default:
        return { x: 0, y: distance };
    }
  };

  const getExitPosition = () => {
    switch (direction) {
      case 'left':
        return { x: distance, y: 0 };
      case 'right':
        return { x: -distance, y: 0 };
      case 'up':
        return { x: 0, y: -distance };
      case 'down':
        return { x: 0, y: distance };
      default:
        return { x: 0, y: -distance };
    }
  };

  return (
    <motion.div
      initial={{ ...getInitialPosition(), opacity: 0 }}
      animate={{ x: 0, y: 0, opacity: 1 }}
      exit={{ ...getExitPosition(), opacity: 0 }}
      transition={{
        duration,
        delay,
        ease: "easeOut"
      }}
      className={className}
    >
      {children}
    </motion.div>
  );
};

export default SlideIn;
