/**
 * ChatInput
 *
 * Responsible for rendering the chat message input UI and emitting a message
 * string to the parent when the user submits.
 *
 * Does NOT perform API calls and does NOT own chat persistence.
 */

import React from "react";
import { motion } from "framer-motion";
import ChatInputBackground from "./ChatInputBackground";
import ChatModelSelector from "./ChatModelSelector";
import { useChatInputState } from "./useChatInputState";

interface ChatInputProps {
  onSendMessage: (message: string) => void;
  disabled?: boolean;
}

const ChatInput: React.FC<ChatInputProps> = ({
  onSendMessage,
  disabled = false,
}) => {
  const {
    message,
    textareaRef,
    canSend,
    handleChange,
    handleKeyDown,
    handleSubmit,
  } = useChatInputState({ disabled, onSendMessage });

  return (
    <div className="w-full relative pt-10 pb-6 px-4 overflow-hidden">

      <ChatInputBackground />

      <div className="max-w-3xl mx-auto relative z-10">
        <motion.form
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2, duration: 0.3 }}
          onSubmit={handleSubmit}
          className={`
            relative bg-[var(--color-surface)] rounded-[26px] shadow-lg border border-[var(--color-border)]
            transition-all duration-200
            ${disabled ? "opacity-70 cursor-not-allowed" : ""}
          `}
        >
          {/* Text Area */}
          <textarea
            ref={textareaRef}
            value={message}
            onChange={handleChange}
            onKeyDown={handleKeyDown}
            placeholder="Message uniAI..."
            disabled={disabled}
            rows={1}
            className="w-full bg-transparent border-none px-5 py-4 text-[var(--color-textPrimary)] placeholder-[var(--color-textSecondary)] focus:ring-0 focus:outline-none focus-visible:ring-0 focus-visible:outline-none focus-visible:shadow-none focus-visible:border-[var(--color-border)] resize-none min-h-[60px] max-h-[200px] scrollbar-thin scrollbar-thumb-[var(--color-border)]"
            style={{ height: "60px" }}
          />

          {/* Bottom Bar: Model Selector & Send Button */}
          <div className="flex justify-between items-center px-3 pb-3 pt-1">
            <ChatModelSelector />

            {/* Send Button */}
            <button
              type="submit"
              disabled={!canSend}
              className={`
                p-2 rounded-full transition-all duration-200 flex items-center justify-center
                ${
                  canSend
                    ? "bg-[var(--color-primary)] text-[var(--color-surface)] shadow-md hover:bg-[var(--color-primaryHover)] transform hover:scale-105"
                    : "bg-[var(--color-surfaceHover)] text-[var(--color-border)] cursor-not-allowed"
                }
              `}
            >
              <span className="material-symbols-outlined text-[20px]">
                arrow_upward
              </span>
            </button>
          </div>
        </motion.form>

        <p className="text-center text-[10px] text-[var(--color-textSecondary)] mt-3">
          AI can make mistakes. Check important info.
        </p>
      </div>
    </div>
  );
};

export default ChatInput;
