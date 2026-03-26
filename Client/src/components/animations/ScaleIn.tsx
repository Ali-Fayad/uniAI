import { motion } from 'framer-motion';
import type { ReactNode } from 'react';

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
 * Responsibility:
 * - Apply a scale-in animation to child content
 *
 * Does NOT:
 * - Fetch data
 * - Perform API calls
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
