/**
 * useChatInputState
 *
 * Responsibility:
 * - Own chat input state (message) and submission/keyboard handlers.
 * - Integrate textarea autosizing behavior.
 *
 * Does NOT:
 * - Perform API calls
 * - Own chat persistence
 */

import { useCallback, useMemo, useRef, useState } from "react";
import type { ChangeEvent, FormEvent, KeyboardEvent, RefObject } from "react";
import { useAutosizeTextarea } from "../../hooks/useAutosizeTextarea";

export interface UseChatInputStateArgs {
  disabled: boolean;
  onSendMessage: (message: string) => void;
}

export interface UseChatInputStateReturn {
  message: string;
  textareaRef: RefObject<HTMLTextAreaElement | null>;
  canSend: boolean;
  handleChange: (e: ChangeEvent<HTMLTextAreaElement>) => void;
  handleKeyDown: (e: KeyboardEvent<HTMLTextAreaElement>) => void;
  handleSubmit: (e: FormEvent) => void;
}

export const useChatInputState = ({ disabled, onSendMessage }: UseChatInputStateArgs): UseChatInputStateReturn => {
  const [message, setMessage] = useState("");
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const { resize: resizeTextarea, reset: resetTextarea } = useAutosizeTextarea(textareaRef, {
    minHeightPx: 60,
    maxHeightPx: 200,
  });

  const canSend = useMemo(() => Boolean(message.trim()) && !disabled, [message, disabled]);

  const handleSubmit = useCallback(
    (e: FormEvent) => {
      e.preventDefault();
      if (!canSend) {
        return;
      }
      const trimmed = message.trim();
      onSendMessage(trimmed);
      setMessage("");
      resetTextarea();
    },
    [canSend, message, onSendMessage, resetTextarea],
  );

  const handleKeyDown = useCallback(
    (e: KeyboardEvent<HTMLTextAreaElement>) => {
      if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        handleSubmit(e);
      }
    },
    [handleSubmit],
  );

  const handleChange = useCallback(
    (e: ChangeEvent<HTMLTextAreaElement>) => {
      setMessage(e.target.value);
      resizeTextarea();
    },
    [resizeTextarea],
  );

  return {
    message,
    textareaRef,
    canSend,
    handleChange,
    handleKeyDown,
    handleSubmit,
  };
};
