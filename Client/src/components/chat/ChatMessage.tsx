import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import type { MessageResponseDto } from "../../types/dto";
import { buildCitationRows } from "./citationUtils.js";

interface ChatMessageProps {
  message: MessageResponseDto;
  isAI: boolean;
  index?: number;
  stream?: boolean;
}

const ChatMessage: React.FC<ChatMessageProps> = ({
  message,
  isAI,
  index = 0,
  stream = false,
}) => {
  const [displayedContent, setDisplayedContent] = useState(
    stream ? "" : message.content,
  );

  useEffect(() => {
    if (!stream || !isAI) {
      setDisplayedContent(message.content);
      return;
    }

    let currentLength = 0;
    let animationFrameId: number;
    let previousTime = performance.now();

    const charactersPerSecond = 120;

    const animate = (currentTime: number) => {
      const elapsedSeconds = (currentTime - previousTime) / 1000;
      const charactersToAdd = Math.max(
        1,
        Math.floor(elapsedSeconds * charactersPerSecond),
      );

      currentLength = Math.min(
        currentLength + charactersToAdd,
        message.content.length,
      );

      setDisplayedContent(message.content.slice(0, currentLength));
      previousTime = currentTime;

      if (currentLength < message.content.length) {
        animationFrameId = requestAnimationFrame(animate);
      }
    };

    animationFrameId = requestAnimationFrame(animate);

    return () => cancelAnimationFrame(animationFrameId);
  }, [isAI, message.content, stream]);

  const citationRows =
    isAI && displayedContent.length === message.content.length
      ? buildCitationRows(message.citations)
      : [];

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{
        duration: 0.3,
        delay: index * 0.05,
        ease: "easeOut",
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
        {isAI ? (
          <div className="chat-markdown text-sm leading-relaxed break-words">
            <ReactMarkdown
              remarkPlugins={[remarkGfm]}
              components={{
                a: (props) => (
                  <a
                    {...props}
                    target="_blank"
                    rel="noopener noreferrer"
                  />
                ),
              }}
            >
              {displayedContent}
            </ReactMarkdown>
          </div>
        ) : (
          <p className="text-sm leading-relaxed whitespace-pre-wrap break-words">
            {message.content}
          </p>
        )}

        <p className="text-xs opacity-60 mt-1">
          {new Date(message.timestamp).toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit",
          })}
        </p>

        {citationRows.length > 0 && (
          <div className="mt-3 pt-3 border-t border-current/10">
            <p className="text-xs font-semibold uppercase tracking-wide opacity-70 mb-2">
              Sources
            </p>

            <ul className="space-y-1">
              {citationRows.map((citation) => (
                <li key={citation.label} className="text-xs leading-relaxed">
                  <a
                    href={citation.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="underline underline-offset-2 hover:opacity-80"
                  >
                    {citation.title}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        )}
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