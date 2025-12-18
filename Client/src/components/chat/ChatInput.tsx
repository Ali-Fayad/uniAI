import React, { useState, useRef, useEffect } from "react";

interface ChatInputProps {
  onSendMessage: (message: string) => void;
  disabled?: boolean;
}

const ChatInput: React.FC<ChatInputProps> = ({
  onSendMessage,
  disabled = false,
}) => {
  const [message, setMessage] = useState("");
  const [isModelMenuOpen, setIsModelMenuOpen] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsModelMenuOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (message.trim() && !disabled) {
      onSendMessage(message.trim());
      setMessage("");
      if (textareaRef.current) {
        textareaRef.current.style.height = "auto";
      }
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  const adjustHeight = () => {
    if (textareaRef.current) {
      textareaRef.current.style.height = "auto";
      textareaRef.current.style.height = `${Math.min(
        textareaRef.current.scrollHeight,
        200
      )}px`;
    }
  };

  return (
    <div className="w-full relative pt-10 pb-6 px-4 overflow-hidden">
      
      {/* --- DIAMOND GRID PATTERN BACKGROUND --- */}
      <div className="absolute inset-0 z-0 pointer-events-none">
         {/* This creates the diamond shape using repeating gradients */}
        <div 
          className="absolute inset-0 opacity-40"
          style={{
            backgroundImage: `linear-gradient(135deg, #e5e7eb 25%, transparent 25%), 
                              linear-gradient(225deg, #e5e7eb 25%, transparent 25%), 
                              linear-gradient(45deg, #e5e7eb 25%, transparent 25%), 
                              linear-gradient(315deg, #e5e7eb 25%, transparent 25%)`,
            backgroundPosition: '10px 0, 10px 0, 0 0, 0 0',
            backgroundSize: '20px 20px',
            backgroundRepeat: 'repeat'
          }}
        />
        {/* This creates the fade/spotlight effect so it blends into the page */}
        <div className="absolute inset-0 bg-gradient-to-t from-white via-white/80 to-transparent" />
      </div>

      <div className="max-w-3xl mx-auto relative z-10">
        <form
          onSubmit={handleSubmit}
          className={`
            relative bg-white rounded-[26px] shadow-lg border border-gray-200 
            transition-all duration-200 focus-within:shadow-xl focus-within:border-custom-primary/50
            ${disabled ? "opacity-70 cursor-not-allowed" : ""}
          `}
        >
          {/* Text Area */}
          <textarea
            ref={textareaRef}
            value={message}
            onChange={(e) => {
              setMessage(e.target.value);
              adjustHeight();
            }}
            onKeyDown={handleKeyDown}
            placeholder="Message uniAI..."
            disabled={disabled}
            rows={1}
            className="w-full bg-transparent border-none px-5 py-4 text-gray-800 placeholder-gray-400 focus:ring-0 resize-none min-h-[60px] max-h-[200px] scrollbar-thin scrollbar-thumb-gray-200"
            style={{ height: "60px" }}
          />

          {/* Bottom Bar: Model Selector & Send Button */}
          <div className="flex justify-between items-center px-3 pb-3 pt-1">
            {/* Mini Model Selector */}
            <div className="relative" ref={menuRef}>
              <button
                type="button"
                onClick={() => setIsModelMenuOpen(!isModelMenuOpen)}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-gray-50 hover:bg-gray-100 text-xs font-medium text-gray-600 transition-colors border border-gray-100"
              >
                <span className="material-symbols-outlined text-[16px] text-custom-primary">
                  smart_toy
                </span>
                <span>uniAI Basic</span>
                <span className="material-symbols-outlined text-[14px]">
                  expand_more
                </span>
              </button>

              {/* Dropdown Menu */}
              {isModelMenuOpen && (
                <div className="absolute bottom-full left-0 mb-2 w-48 bg-white rounded-xl shadow-xl border border-gray-100 overflow-hidden animate-fadeIn z-10">
                  <div className="p-1">
                    <div className="px-3 py-2 bg-gray-50 rounded-lg mb-1">
                      <p className="text-xs font-semibold text-gray-800">
                        uniAI Basic
                      </p>
                      <p className="text-[10px] text-gray-500">
                        Fast & Efficient
                      </p>
                    </div>
                    <div className="px-3 py-2 opacity-50 cursor-not-allowed">
                      <p className="text-xs font-semibold text-gray-800">
                        GPT-4o
                      </p>
                      <p className="text-[10px] text-custom-primary">
                        Coming soon
                      </p>
                    </div>
                    <div className="px-3 py-2 opacity-50 cursor-not-allowed">
                      <p className="text-xs font-semibold text-gray-800">
                        Claude 3.5
                      </p>
                      <p className="text-[10px] text-custom-primary">
                        Coming soon
                      </p>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* Send Button */}
            <button
              type="submit"
              disabled={disabled || !message.trim()}
              className={`
                p-2 rounded-full transition-all duration-200 flex items-center justify-center
                ${
                  message.trim() && !disabled
                    ? "bg-custom-primary text-[#151514] shadow-md hover:bg-[#a69d8f] transform hover:scale-105"
                    : "bg-gray-100 text-gray-300 cursor-not-allowed"
                }
              `}
            >
              <span className="material-symbols-outlined text-[20px]">
                arrow_upward
              </span>
            </button>
          </div>
        </form>

        <p className="text-center text-[10px] text-gray-400 mt-3">
          AI can make mistakes. Check important info.
        </p>
      </div>
    </div>
  );
};

export default ChatInput;