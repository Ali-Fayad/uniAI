import { useCallback, useEffect, RefObject } from "react";

type Options = {
  maxHeight?: number;
  minHeight?: number;
};

export const useAutoResizeTextarea = (
  ref: RefObject<HTMLTextAreaElement | null>,
  value?: string,
  opts: Options = {}
) => {
  const { maxHeight = 200, minHeight = 60 } = opts;

  const adjust = useCallback(() => {
    const el = ref.current;
    if (!el) return;
    el.style.height = "auto";
    const newHeight = Math.min(el.scrollHeight, maxHeight);
    el.style.height = `${Math.max(newHeight, minHeight)}px`;
  }, [ref, maxHeight, minHeight]);

  useEffect(() => {
    adjust();
  }, [value, adjust]);

  return { adjust };
};

export default useAutoResizeTextarea;
