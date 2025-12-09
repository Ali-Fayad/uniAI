import React, { useEffect, useState } from "react";

/**
 * Sidebar shows chat history. For demo purposes it fakes an API call.
 */

type ChatItem = {
  id: string;
  title: string;
  preview?: string;
  lastUpdated?: string;
};

type Props = {
  onSelect: (chat: ChatItem) => void;
};

const Sidebar: React.FC<Props> = ({ onSelect }) => {
  const [items, setItems] = useState<ChatItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Demo: simulate API fetch for chat history
    setLoading(true);
    const timer = setTimeout(() => {
      const demo: ChatItem[] = [
        {
          id: "1",
          title: "The Impact of AI on Education",
          preview: "Hey uniAI, can you explain the main impacts...",
          lastUpdated: "2025-12-08",
        },
        {
          id: "2",
          title: "Python Code for Data Scraping",
          preview: "Write a Python script that scrapes data...",
          lastUpdated: "2025-12-07",
        },
        {
          id: "3",
          title: "Creative Story Ideas",
          preview: "Give me five story prompts for a sci-fi novel.",
          lastUpdated: "2025-12-06",
        },
        {
          id: "4",
          title: "Marketing Strategy for a Startup",
          preview: "What are the key components of a good marketing...",
          lastUpdated: "2025-12-01",
        },
      ];
      setItems(demo);
      setLoading(false);
    }, 350);
    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="flex h-full flex-col">
      <button
        className="mb-4 flex w-full items-center justify-center gap-2 rounded-full border border-custom-primary bg-custom-accent/50 py-3 px-4 font-semibold text-[#151514] transition-colors hover:bg-custom-accent"
        onClick={() =>
          onSelect({
            id: "new",
            title: "New Chat",
            preview: "",
            lastUpdated: new Date().toISOString(),
          })
        }
      >
        <span className="material-symbols-outlined text-xl">add</span>
        New Chat
      </button>

      <div className="flex-1 space-y-2 overflow-y-auto">
        {loading ? (
          <p className="text-sm text-[#151514]/70">Loading chats…</p>
        ) : (
          items.map((it) => (
            <button
              key={it.id}
              onClick={() => onSelect(it)}
              className="w-full text-left rounded-lg p-4 transition-colors hover:bg-custom-secondary/40"
            >
              <h3 className="font-bold text-sm text-[#151514] truncate">{it.title}</h3>
              <p className="text-xs text-[#151514]/70 mt-1 truncate">{it.preview}</p>
            </button>
          ))
        )}
      </div>
    </div>
  );
};

export default Sidebar;
