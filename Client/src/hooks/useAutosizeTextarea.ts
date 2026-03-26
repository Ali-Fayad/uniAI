/**
 * useAutosizeTextarea
 *
 * Responsible for auto-resizing a textarea element based on its scroll height.
 *
 * Does NOT own form state, does NOT submit messages, and does NOT perform any
 * API calls.
 */

import { useCallback } from 'react';
import type { RefObject } from 'react';

export type AutosizeTextareaOptions = {
  /** Minimum height in pixels. */
  minHeightPx?: number;
  /** Maximum height in pixels. */
  maxHeightPx?: number;
};

export const useAutosizeTextarea = (
  textareaRef: RefObject<HTMLTextAreaElement | null>,
  options: AutosizeTextareaOptions = {},
) => {
  const { minHeightPx = 60, maxHeightPx = 200 } = options;

  const resize = useCallback(() => {
    const textarea = textareaRef.current;
    if (!textarea) {
      return;
    }

    textarea.style.height = 'auto';
    const nextHeight = Math.min(Math.max(textarea.scrollHeight, minHeightPx), maxHeightPx);
    textarea.style.height = `${nextHeight}px`;
  }, [maxHeightPx, minHeightPx, textareaRef]);

  const reset = useCallback(() => {
    const textarea = textareaRef.current;
    if (!textarea) {
      return;
    }

    textarea.style.height = `${minHeightPx}px`;
  }, [minHeightPx, textareaRef]);

  return { resize, reset };
};
