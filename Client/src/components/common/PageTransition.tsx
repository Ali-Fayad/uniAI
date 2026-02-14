import { motion } from 'framer-motion';
import { ReactNode } from 'react';

interface PageTransitionProps {
  children: ReactNode;
}

/**
 * PageTransition Component
 * 
 * Wraps page content with smooth fade and slide animations
 * using Framer Motion for entering and exiting transitions.
 */
const PageTransition = ({ children }: PageTransitionProps) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      transition={{
        duration: 0.3,
        ease: 'easeInOut',
      }}
      style={{ width: '100%', height: '100%' }}
    >
      {children}
    </motion.div>
  );
};

export default PageTransition;
