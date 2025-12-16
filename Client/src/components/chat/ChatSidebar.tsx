import React, { useState, useEffect } from 'react';
import { chatService } from '../../services/chat';
import type { Chat } from '../../types/dto';

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
  const [chats, setChats] = useState<Chat[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  useEffect(() => {
    loadChats();
  }, []);

  const loadChats = async () => {
    try {
      const data = await chatService.getChats();
      setChats(data);
    } catch (error) {
      console.error('Failed to load chats:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteChat = async (chatId: number, e: React.MouseEvent) => {
    e.stopPropagation();
    
    if (confirm('Are you sure you want to delete this chat?')) {
      try {
        await chatService.deleteChat(chatId);
        setChats(chats.filter(chat => chat.id !== chatId));
        onDeleteChat(chatId);
      } catch (error) {
        console.error('Failed to delete chat:', error);
      }
    }
  };

  const handleDeleteAllChats = async () => {
    if (confirm('Are you sure you want to delete ALL chats? This cannot be undone.')) {
      try {
        await chatService.deleteAllChats();
        setChats([]);
        onDeleteChat(-1); // Signal to clear current chat
      } catch (error) {
        console.error('Failed to delete all chats:', error);
      }
    }
  };

  return (
    <>
      {/* Toggle Button for Mobile */}
      <button
        onClick={() => setIsSidebarOpen(!isSidebarOpen)}
        className="lg:hidden fixed top-20 left-4 z-50 bg-custom-primary text-[#151514] p-2 rounded-full shadow-lg hover:bg-[#a69d8f] transition"
      >
        <span className="material-symbols-outlined">
          {isSidebarOpen ? 'close' : 'menu'}
        </span>
      </button>

      {/* Sidebar */}
      <div
        className={`
          fixed lg:relative top-0 left-0 h-full bg-white/50 backdrop-blur-sm border-r border-custom-secondary/30 
          transition-transform duration-300 z-40 w-72
          ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
        `}
      >
        <div className="flex flex-col h-full p-4">
          {/* New Chat Button */}
          <button
            onClick={onNewChat}
            className="flex items-center justify-center gap-2 w-full bg-custom-primary text-[#151514] px-4 py-3 rounded-xl font-bold hover:bg-[#a69d8f] transition mb-4"
          >
            <span className="material-symbols-outlined">add</span>
            New Chat
          </button>

          {/* Chat List */}
          <div className="flex-1 overflow-y-auto space-y-2">
            {isLoading ? (
              <div className="text-center text-[#797672] py-8">Loading chats...</div>
            ) : chats.length === 0 ? (
              <div className="text-center text-[#797672] py-8">
                No chats yet. Start a new one!
              </div>
            ) : (
              chats.map((chat) => (
                <div
                  key={chat.id}
                  onClick={() => onSelectChat(chat.id)}
                  className={`
                    group flex items-center justify-between px-4 py-3 rounded-xl cursor-pointer transition
                    ${selectedChatId === chat.id 
                      ? 'bg-custom-primary text-[#151514]' 
                      : 'bg-white/30 text-[#151514] hover:bg-white/50'
                    }
                  `}
                >
                  <div className="flex-1 min-w-0">
                    <p className="font-medium truncate">{chat.title}</p>
                    <p className="text-xs opacity-70">
                      {new Date(chat.updatedAt).toLocaleDateString()}
                    </p>
                  </div>
                  <button
                    onClick={(e) => handleDeleteChat(chat.id, e)}
                    className="opacity-0 group-hover:opacity-100 p-1 hover:bg-red-100 rounded transition"
                  >
                    <span className="material-symbols-outlined text-red-600 text-sm">delete</span>
                  </button>
                </div>
              ))
            )}
          </div>

          {/* Delete All Button */}
          {chats.length > 0 && (
            <button
              onClick={handleDeleteAllChats}
              className="flex items-center justify-center gap-2 w-full bg-red-100 text-red-600 px-4 py-3 rounded-xl font-medium hover:bg-red-200 transition mt-4"
            >
              <span className="material-symbols-outlined text-sm">delete_sweep</span>
              Delete All Chats
            </button>
          )}
        </div>
      </div>
    </>
  );
};

export default ChatSidebar;
