import { motion } from 'framer-motion';
import { ReactNode } from 'react';

interface ScaleInProps {
  children: ReactNode;
  delay?: number;
  duration?: number;
  initialScale?: number;
  className?: string;
}

/**
 * ScaleIn Animation Component
 * 
 * Responsibilities:
 * - Apply scale animation to child elements
 * - Support customizable delay, duration, and initial scale
 * - Respect reduced motion preferences
 */
const ScaleIn = ({ 
  children, 
  delay = 0, 
  duration = 0.4,
  initialScale = 0.9,
  className = "" 
}: ScaleInProps) => {
  return (
    <motion.div
      initial={{ scale: initialScale, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      exit={{ scale: initialScale, opacity: 0 }}
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

export default ScaleIn;
