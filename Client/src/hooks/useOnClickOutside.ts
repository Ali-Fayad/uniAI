/**
 * useOnClickOutside
 *
 * Responsible for invoking a callback when a pointer event occurs outside the
 * referenced element.
 *
 * Does NOT manage UI state by itself and does NOT perform any API calls.
 */

import { useEffect } from 'react';
import type { RefObject } from 'react';

type AnyEvent = MouseEvent | TouchEvent;

export type UseOnClickOutsideOptions = {
  /** DOM event type used to detect outside interactions. */
  eventType?: 'mousedown' | 'click' | 'touchstart';
  /** When false, the listener is not attached. */
  enabled?: boolean;
};

export const useOnClickOutside = <T extends HTMLElement>(
  ref: RefObject<T | null>,
  onOutside: (event: AnyEvent) => void,
  options: UseOnClickOutsideOptions = {},
) => {
  const { eventType = 'mousedown', enabled = true } = options;

  useEffect(() => {
    if (!enabled) {
      return;
    }

    const handle = (event: AnyEvent) => {
      const element = ref.current;
      if (!element) {
        return;
      }

      if (event.target instanceof Node && !element.contains(event.target)) {
        onOutside(event);
      }
    };

    document.addEventListener(eventType, handle);
    return () => document.removeEventListener(eventType, handle);
  }, [enabled, eventType, onOutside, ref]);
};
