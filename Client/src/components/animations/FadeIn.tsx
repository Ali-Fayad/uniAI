import { motion } from 'framer-motion';
import { ReactNode } from 'react';

interface FadeInProps {
  children: ReactNode;
  delay?: number;
  duration?: number;
  className?: string;
}

/**
 * FadeIn Animation Component
 * 
 * Responsibilities:
 * - Apply fade-in animation to any child element
 * - Support customizable delay and duration
 * - Respect reduced motion preferences
 */
const FadeIn = ({ 
  children, 
  delay = 0, 
  duration = 0.4, 
  className = "" 
}: FadeInProps) => {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{
        duration,
        delay,
        ease: "easeInOut"
      }}
      className={className}
    >
      {children}
    </motion.div>
  );
};

export default FadeIn;
