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
    <div className="relative w-full min-w-0 overflow-visible px-4 pb-4 pt-2">
      <ChatInputBackground />

      <div className="relative z-10 mx-auto w-full min-w-0 max-w-4xl">
        <motion.form
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2, duration: 0.3 }}
          onSubmit={handleSubmit}
          className={`
            relative w-full min-w-0 rounded-[26px]
            border border-[var(--color-border)]
            bg-[var(--color-surface)] shadow-lg
            transition-all duration-200
            ${disabled ? "cursor-not-allowed opacity-70" : ""}
          `}
        >
          <textarea
            ref={textareaRef}
            value={message}
            onChange={handleChange}
            onKeyDown={handleKeyDown}
            placeholder="Message uniAI..."
            disabled={disabled}
            rows={1}
            wrap="soft"
            className="
              chat-composer-textarea
              block min-h-[60px] max-h-[200px] w-full min-w-0 resize-none
              overflow-x-hidden overflow-y-auto border-none bg-transparent
              px-5 py-4 text-[var(--color-textPrimary)]
              placeholder-[var(--color-textSecondary)]
              focus:border-[var(--color-border)] focus:outline-none focus:ring-0
              focus-visible:border-[var(--color-border)]
              focus-visible:outline-none focus-visible:ring-0
              focus-visible:shadow-none
              scrollbar-thin scrollbar-thumb-[var(--color-border)]
            "
            style={{ height: "60px" }}
          />

          <div className="flex min-w-0 items-center justify-between gap-3 px-3 pb-3 pt-1">
            <ChatModelSelector />

            <button
              type="submit"
              disabled={!canSend}
              className={`
                flex flex-shrink-0 items-center justify-center rounded-full p-2
                transition-all duration-200
                ${
                  canSend
                    ? "transform bg-[var(--color-primary)] text-[var(--color-surface)] shadow-md hover:scale-105 hover:bg-[var(--color-primaryHover)]"
                    : "cursor-not-allowed bg-[var(--color-surfaceHover)] text-[var(--color-border)]"
                }
              `}
              aria-label="Send message"
            >
              <span className="material-symbols-outlined text-[20px]">
                arrow_upward
              </span>
            </button>
          </div>
        </motion.form>

        <p className="mt-2 text-center text-[10px] text-[var(--color-textSecondary)]">
          AI can make mistakes. Check important info.
        </p>
      </div>
    </div>
  );
};

export default ChatInput;