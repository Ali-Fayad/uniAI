import { motion, AnimatePresence } from 'framer-motion';

interface AuthHeadingProps {
  title: string;
  subtitle?: string;
}

/**
 * AuthHeading Component
 *
 * Responsibility:
 * - Render an animated title/subtitle block for auth flows
 *
 * Does NOT:
 * - Manage authentication state
 * - Perform API calls
 */
const AuthHeading = ({ title, subtitle }: AuthHeadingProps) => {
  return (
    <AnimatePresence mode="wait">
      <motion.div
        key={title}
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: 10 }}
        transition={{ duration: 0.25, ease: "easeInOut" }}
        className="text-center mb-6"
      >
        <h2 className="text-3xl font-bold text-[var(--color-textPrimary)] mb-2">
          {title}
        </h2>
        {subtitle && (
          <motion.p
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.1, duration: 0.2 }}
            className="text-sm text-[var(--color-textSecondary)]"
          >
            {subtitle}
          </motion.p>
        )}
      </motion.div>
    </AnimatePresence>
  );
};

export default AuthHeading;
