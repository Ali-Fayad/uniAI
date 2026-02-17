import { motion } from 'framer-motion';
import type { Variants } from 'framer-motion';
import { ReactNode } from 'react';

interface StaggerContainerProps {
  children: ReactNode;
  staggerDelay?: number;
  initialDelay?: number;
  className?: string;
}

/**
 * StaggerContainer Animation Component
 * 
 * Responsibilities:
 * - Stagger animation of child elements
 * - Support customizable stagger delay between children
 * - Respect reduced motion preferences
 * 
 * Usage:
 * Wrap direct children with motion components that use variants
 */
const StaggerContainer = ({ 
  children, 
  staggerDelay = 0.1,
  initialDelay = 0,
  className = "" 
}: StaggerContainerProps) => {
  const containerVariants: Variants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        delayChildren: initialDelay,
        staggerChildren: staggerDelay,
        when: "beforeChildren"
      }
    },
    exit: {
      opacity: 0,
      transition: {
        staggerChildren: 0.05,
        staggerDirection: -1
      }
    }
  };

  return (
    <motion.div
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      exit="exit"
      className={className}
    >
      {children}
    </motion.div>
  );
};

// Export child variant for easy use
export const staggerItemVariants: Variants = {
  hidden: { opacity: 0, y: 20 },
  visible: { 
    opacity: 1, 
    y: 0,
    transition: {
      duration: 0.4,
      ease: "easeOut"
    }
  },
  exit: {
    opacity: 0,
    y: -10,
    transition: {
      duration: 0.2
    }
  }
};

export default StaggerContainer;
