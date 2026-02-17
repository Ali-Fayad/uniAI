import React from "react";
import { motion } from "framer-motion";
import type { MessageResponseDto } from "../../types/dto";

interface ChatMessageProps {
  message: MessageResponseDto;
  isAI: boolean;
  index?: number;
}

/**
 * ChatMessage Component
 * 
 * Responsibilities:
 * - Display individual chat message with animation
 * - Slide up animation on appearance
 * - Different styling for AI vs user messages
 */
const ChatMessage: React.FC<ChatMessageProps> = ({ message, isAI, index = 0 }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ 
        duration: 0.3, 
        delay: index * 0.05,
        ease: "easeOut" 
      }}
      className={`flex gap-3 mb-4 ${
        isAI ? "justify-start" : "justify-end"
      }`}
    >
      {isAI && (
        <motion.div 
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ delay: index * 0.05 + 0.1, duration: 0.2 }}
          className="flex-shrink-0 w-8 h-8 rounded-full bg-[var(--color-primary)] flex items-center justify-center"
        >
          <span className="material-symbols-outlined text-[var(--color-background)] text-sm">
            smart_toy
          </span>
        </motion.div>
      )}

      <motion.div
        initial={{ scale: 0.95 }}
        animate={{ scale: 1 }}
        transition={{ delay: index * 0.05 + 0.15 }}
        className={`max-w-[70%] px-4 py-3 rounded-2xl shadow-sm ${
          isAI
            ? "bg-[var(--color-surface)] text-[var(--color-textPrimary)] rounded-tl-none"
            : "bg-[var(--color-primary)] text-[var(--color-background)] rounded-tr-none"
        }`}
      >
        <p className="text-sm leading-relaxed whitespace-pre-wrap break-words">
          {message.content}
        </p>
        <p className="text-xs opacity-60 mt-1">
          {new Date(message.timestamp).toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit",
          })}
        </p>
      </motion.div>

      {!isAI && (
        <motion.div 
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ delay: index * 0.05 + 0.1, duration: 0.2 }}
          className="flex-shrink-0 w-8 h-8 rounded-full bg-[var(--color-secondary)] flex items-center justify-center"
        >
          <span className="material-symbols-outlined text-[var(--color-background)] text-sm">
            person
          </span>
        </motion.div>
      )}
    </motion.div>
  );
};

export default ChatMessage;
