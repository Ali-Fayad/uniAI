import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';

interface AuthCardProps {
  children: React.ReactNode;
}

export const AuthCard: React.FC<AuthCardProps> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();

  // Show back button on all pages EXCEPT the default view (/auth)
  const showBackButton = location.pathname !== '/auth';

  return (
    <div className="w-full max-w-md bg-[var(--color-surface)] p-8 sm:p-10 rounded-3xl shadow-lg border border-[var(--color-border)] relative">
      {showBackButton && (
        <button
          onClick={() => navigate('/auth')}
          className="absolute top-6 left-6 p-2 bg-transparent text-[var(--color-primary)] hover:text-[var(--color-primaryVariant)] transition-colors"
          aria-label="Back to options"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M19 12H5"/>
            <path d="M12 19l-7-7 7-7"/>
          </svg>
        </button>
      )}
      <AnimatePresence mode="wait">
        <motion.div
          key={location.pathname}
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          exit={{ opacity: 0, x: -20 }}
          transition={{
            duration: 0.3,
            ease: 'easeInOut',
          }}
        >
          {children}
        </motion.div>
      </AnimatePresence>
    </div>
  );
};
