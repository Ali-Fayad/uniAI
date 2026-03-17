import { motion } from 'framer-motion';

/**
 * TypingIndicator Component
 * 
 * Responsibilities:
 * - Display animated typing indicator during AI response
 * - Bouncing dots animation
 * - Consistent with chat message styling
 */
const TypingIndicator = () => {
  const dotVariants = {
    initial: { y: 0 },
    animate: { y: -8 }
  };

  const dotTransition = {
    duration: 0.5,
    repeat: Infinity,
    repeatType: "reverse" as const,
    ease: "easeInOut"
  };

  return (
    <div className="flex justify-start gap-3 mb-4">
      <div className="flex-shrink-0 w-8 h-8 rounded-full bg-[var(--color-primary)] flex items-center justify-center">
        <span className="material-symbols-outlined text-[var(--color-background)] text-sm">
          smart_toy
        </span>
      </div>
      <div className="bg-[var(--color-surface)] px-4 py-3 rounded-2xl rounded-tl-none shadow-sm">
        <div className="flex gap-1 items-center h-5">
          <motion.span
            variants={dotVariants}
            initial="initial"
            animate="animate"
            transition={{ ...dotTransition, delay: 0 }}
            className="w-2 h-2 bg-[var(--color-primary)] rounded-full"
          />
          <motion.span
            variants={dotVariants}
            initial="initial"
            animate="animate"
            transition={{ ...dotTransition, delay: 0.15 }}
            className="w-2 h-2 bg-[var(--color-primary)] rounded-full"
          />
          <motion.span
            variants={dotVariants}
            initial="initial"
            animate="animate"
            transition={{ ...dotTransition, delay: 0.3 }}
            className="w-2 h-2 bg-[var(--color-primary)] rounded-full"
          />
        </div>
      </div>
    </div>
  );
};

export default TypingIndicator;
