/**
 * ChatInputBackground
 *
 * Responsibility:
 * - Render a subtle decorative glow behind the floating chat composer.
 *
 * Does NOT:
 * - Manage input state
 * - Handle message submission
 * - Perform API calls
 */

import React from "react";

const ChatInputBackground: React.FC = () => {
  return (
    <div
      className="pointer-events-none absolute inset-x-0 bottom-0 z-0 h-32 overflow-visible"
      aria-hidden="true"
    >
      <div
        className="absolute bottom-0 left-1/2 h-24 w-[min(42rem,88vw)] -translate-x-1/2 rounded-full opacity-15 blur-3xl"
        style={{
          background:
            "radial-gradient(circle at center, color-mix(in srgb, var(--color-primary) 45%, transparent) 0%, transparent 70%)",
        }}
      />
    </div>
  );
};

export default ChatInputBackground;