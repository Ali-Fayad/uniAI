import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';

interface AuthCardProps {
  children: React.ReactNode;
}

/**
 * AuthCard Component
 * 
 * Responsibilities:
 * - Animated container for auth forms
 * - Smooth layout transitions when content changes
 * - Back button with entrance/exit animation
 */
export const AuthCard: React.FC<AuthCardProps> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();

  // Show back button on all pages EXCEPT the default view (/auth)
  const showBackButton = location.pathname !== '/auth';

  return (
    <motion.div 
      layout
      className="w-full max-w-md bg-[var(--color-surface)] p-8 sm:p-10 rounded-3xl shadow-lg border border-[var(--color-border)] relative overflow-hidden"
      transition={{ 
        layout: { 
          type: "spring", 
          stiffness: 300, 
          damping: 30 
        } 
      }}
    >
      <AnimatePresence mode="wait">
        {showBackButton && (
          <motion.button
            initial={{ opacity: 0, x: -10 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -10 }}
            transition={{ duration: 0.2 }}
            onClick={() => navigate('/auth')}
            className="absolute top-6 left-6 p-2 bg-transparent text-[var(--color-primary)] hover:text-[var(--color-primaryVariant)] transition-colors z-20"
            aria-label="Back to options"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M19 12H5"/>
              <path d="M12 19l-7-7 7-7"/>
            </svg>
          </motion.button>
        )}
      </AnimatePresence>
      <AnimatePresence mode="wait">
        <motion.div
          key={location.pathname}
          initial={{ opacity: 0, y: 15 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -15 }}
          transition={{ 
            duration: 0.25,
            ease: "easeInOut"
          }}
        >
          {children}
        </motion.div>
      </AnimatePresence>
    </motion.div>
  );
};
