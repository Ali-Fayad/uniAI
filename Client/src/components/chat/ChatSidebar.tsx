import React, { useState, useEffect, useContext } from "react";
import { chatService } from "../../services/chat";
import { AuthContext } from "../../context/AuthContext";
import type { Chat } from "../../types/dto";

interface ChatSidebarProps {
  selectedChatId: number | null;
  onSelectChat: (chatId: number) => void;
  onNewChat: () => void;
  onDeleteChat: (chatId: number) => void;
}

const ChatSidebar: React.FC<ChatSidebarProps> = ({
  selectedChatId,
  onSelectChat,
  onNewChat,
  onDeleteChat,
}) => {
  const { user } = useContext(AuthContext)!;
  const [chats, setChats] = useState<Chat[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  useEffect(() => {
    loadChats();
  }, [selectedChatId]); // Reload when selection changes (e.g. new chat created)

  const loadChats = async () => {
    try {
      const data = await chatService.getChats();
      setChats(data);
    } catch (error) {
      console.error("Failed to load chats:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteChat = async (chatId: number, e: React.MouseEvent) => {
    e.stopPropagation();
    if (confirm("Are you sure you want to delete this chat?")) {
      try {
        await chatService.deleteChat(chatId);
        setChats(chats.filter((chat) => chat.id !== chatId));
        onDeleteChat(chatId);
      } catch (error) {
        console.error("Failed to delete chat:", error);
      }
    }
  };

  return (
    <>
      {/* Mobile Toggle Button */}
      <button
        onClick={() => setIsSidebarOpen(!isSidebarOpen)}
        className="lg:hidden fixed top-4 left-4 z-50 bg-custom-primary text-[#151514] p-2 rounded-full shadow-lg hover:bg-[#a69d8f] transition-colors"
        aria-label="Toggle Sidebar"
      >
        <span className="material-symbols-outlined">
          {isSidebarOpen ? "close" : "menu"}
        </span>
      </button>

      {/* Mobile Backdrop */}
      {isSidebarOpen && (
        <div
          className="fixed inset-0 bg-black/40 backdrop-blur-sm z-30 lg:hidden"
          onClick={() => setIsSidebarOpen(false)}
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`
          fixed inset-y-0 left-0 z-40 w-80 bg-[#fcfcfc] border-r border-gray-200
          transform transition-transform duration-300 ease-in-out lg:relative lg:translate-x-0
          ${isSidebarOpen ? "translate-x-0" : "-translate-x-full"}
          flex flex-col h-full shadow-sm
        `}
      >
        {/* Header */}
        <div className="p-6 flex items-center justify-between">
          <h2 className="text-xl font-bold text-gray-800">Chats</h2>
          <button
            onClick={() => {
              onNewChat();
              setIsSidebarOpen(false);
            }}
            className="p-2 rounded-full hover:bg-gray-100 transition-colors text-custom-primary"
            title="New Chat"
          >
            <span className="material-symbols-outlined">add_circle</span>
          </button>
        </div>

        {/* Chat List */}
        <div className="flex-1 overflow-y-auto px-4 space-y-2 scrollbar-thin scrollbar-thumb-gray-200">
          {isLoading ? (
            <div className="flex items-center justify-center h-20 text-gray-400">
              <span className="text-sm">Loading...</span>
            </div>
          ) : chats.length === 0 ? (
            <div className="text-center text-gray-400 py-8 text-sm">
              No chats yet
            </div>
          ) : (
            chats.map((chat) => (
              <div
                key={chat.id}
                onClick={() => {
                  onSelectChat(chat.id);
                  setIsSidebarOpen(false);
                }}
                className={`
                  group relative flex items-center justify-between px-4 py-3 rounded-2xl cursor-pointer transition-all duration-200
                  ${
                    selectedChatId === chat.id
                      ? "bg-custom-primary/10 text-custom-primary font-medium"
                      : "hover:bg-gray-50 text-gray-600"
                  }
                `}
              >
                <div className="flex-1 min-w-0 pr-8">
                  <p className="truncate text-sm">{chat.title || "New Chat"}</p>
                  <p className="text-[10px] opacity-60 mt-0.5">
                    {new Date(chat.updatedAt).toLocaleDateString()}
                  </p>
                </div>

                <button
                  onClick={(e) => handleDeleteChat(chat.id, e)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 p-1.5 rounded-lg opacity-0 group-hover:opacity-100 hover:bg-red-50 text-red-400 transition-all"
                >
                  <span className="material-symbols-outlined text-[18px]">
                    delete
                  </span>
                </button>
              </div>
            ))
          )}
        </div>

        {/* User Profile Section */}
        <div className="p-4 border-t border-gray-100 bg-white">
          <div className="flex items-center gap-3 p-2 rounded-xl hover:bg-gray-50 transition-colors cursor-pointer">
            <div className="w-10 h-10 rounded-full bg-custom-primary/20 flex items-center justify-center text-custom-primary font-bold">
              {user?.firstName?.[0] || user?.username?.[0] || "U"}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-gray-800 truncate">
                {user?.firstName} {user?.lastName}
              </p>
              <p className="text-xs text-gray-500 truncate">
                @{user?.username}
              </p>
            </div>
            <span className="material-symbols-outlined text-gray-400">
              more_vert
            </span>
          </div>
        </div>
      </aside>
    </>
  );
};

export default ChatSidebar;
