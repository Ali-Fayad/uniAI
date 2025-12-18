import React from "react";
import type { MessageResponseDto } from "../../types/dto";

interface ChatMessageProps {
  message: MessageResponseDto;
  isAI: boolean;
}

const ChatMessage: React.FC<ChatMessageProps> = ({ message, isAI }) => {
  return (
    <div
      className={`flex gap-3 mb-4 animate-fadeIn ${
        isAI ? "justify-start" : "justify-end"
      }`}
    >
      {isAI && (
        <div className="flex-shrink-0 w-8 h-8 rounded-full bg-[var(--color-primary)] flex items-center justify-center">
          <span className="material-symbols-outlined text-[var(--color-background)] text-sm">
            smart_toy
          </span>
        </div>
      )}

      <div
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
      </div>

      {!isAI && (
        <div className="flex-shrink-0 w-8 h-8 rounded-full bg-[var(--color-secondary)] flex items-center justify-center">
          <span className="material-symbols-outlined text-[var(--color-background)] text-sm">
            person
          </span>
        </div>
      )}
    </div>
  );
};

export default ChatMessage;
