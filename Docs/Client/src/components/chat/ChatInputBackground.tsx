/**
 * ChatInputBackground
 *
 * Responsibility:
 * - Render the decorative background behind the chat input area.
 *
 * Does NOT:
 * - Manage input state
 * - Handle message submission
 * - Perform API calls
 */

import React from "react";

const ChatInputBackground: React.FC = () => {
  return (
    <div className="absolute inset-0 z-0 pointer-events-none">
      <div
        className="absolute inset-0 opacity-20"
        style={{
          backgroundImage: `linear-gradient(135deg, var(--color-primary) 25%, transparent 25%),
                              linear-gradient(225deg, var(--color-primary) 25%, transparent 25%),
                              linear-gradient(45deg, var(--color-primary) 25%, transparent 25%),
                              linear-gradient(315deg, var(--color-primary) 25%, transparent 25%)`,
          backgroundPosition: "10px 0, 10px 0, 0 0, 0 0",
          backgroundSize: "20px 20px",
          backgroundRepeat: "repeat",
        }}
      />
      <div className="absolute inset-0 bg-gradient-to-b from-[var(--color-background)] via-[var(--color-background)] to-transparent" />
    </div>
  );
};

export default ChatInputBackground;
