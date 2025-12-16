import React from 'react';
import type { MessageResponseDto } from '../../types/dto';

interface ChatMessageProps {
  message: MessageResponseDto;
  isAI: boolean;
}

const ChatMessage: React.FC<ChatMessageProps> = ({ message, isAI }) => {
  return (
    <div
      className={`flex gap-3 mb-4 animate-fadeIn ${
        isAI ? 'justify-start' : 'justify-end'
      }`}
    >
      {isAI && (
        <div className="flex-shrink-0 w-8 h-8 rounded-full bg-custom-primary flex items-center justify-center">
          <span className="material-symbols-outlined text-[#151514] text-sm">smart_toy</span>
        </div>
      )}
      
      <div
        className={`max-w-[70%] px-4 py-3 rounded-2xl shadow-sm ${
          isAI
            ? 'bg-white/70 text-[#151514] rounded-tl-none'
            : 'bg-custom-primary text-[#151514] rounded-tr-none'
        }`}
      >
        <p className="text-sm leading-relaxed whitespace-pre-wrap break-words">
          {message.message}
        </p>
        <p className="text-xs opacity-60 mt-1">
          {new Date(message.createdAt).toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit',
          })}
        </p>
      </div>

      {!isAI && (
        <div className="flex-shrink-0 w-8 h-8 rounded-full bg-custom-secondary flex items-center justify-center">
          <span className="material-symbols-outlined text-[#151514] text-sm">person</span>
        </div>
      )}
    </div>
  );
};

export default ChatMessage;
