import React, { useState, useEffect, useContext, useRef } from "react";
import { useNavigate } from "react-router-dom";
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
  const { user, logout } = useContext(AuthContext)!;
  const navigate = useNavigate();
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);
  const profileMenuRef = useRef<HTMLDivElement | null>(null);
  const [chats, setChats] = useState<Chat[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  useEffect(() => {
    loadChats();
  }, [selectedChatId]); // Reload when selection changes (e.g. new chat created)

  // Close profile menu on outside click
  useEffect(() => {
    const onDocClick = (e: MouseEvent) => {
      if (!profileMenuRef.current) return;
      if (
        e.target instanceof Node &&
        !profileMenuRef.current.contains(e.target)
      ) {
        setProfileMenuOpen(false);
      }
    };

    document.addEventListener("click", onDocClick);
    return () => document.removeEventListener("click", onDocClick);
  }, []);

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
        className="lg:hidden fixed top-4 left-4 z-50 bg-[var(--color-primary)] text-[var(--color-background)] p-2 rounded-full shadow-lg hover:bg-[var(--color-primaryVariant)] transition-colors"
        aria-label="Toggle Sidebar"
      >
        <span className="material-symbols-outlined">
          {isSidebarOpen ? "close" : "menu"}
        </span>
      </button>

      {/* Mobile Backdrop */}
      {isSidebarOpen && (
        <div
          className="fixed inset-0 bg-black/40 z-30 lg:hidden"
          onClick={() => setIsSidebarOpen(false)}
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`
          fixed inset-y-0 left-0 z-40 w-80 bg-[var(--color-surface)] border-r border-[var(--color-border)]
          transform transition-transform duration-300 ease-in-out lg:relative lg:translate-x-0
          ${isSidebarOpen ? "translate-x-0" : "-translate-x-full"}
          flex flex-col h-full shadow-sm
        `}
      >
        {/* Header */}
        <div className="p-6 flex items-center justify-between">
          <h2 className="text-xl font-bold text-[var(--color-textPrimary)]">Chats</h2>
          <button
            onClick={() => {
              onNewChat();
              setIsSidebarOpen(false);
            }}
            className="p-2 rounded-full hover:bg-[var(--color-elevatedSurface)] transition-colors text-[var(--color-primary)]"
            title="New Chat"
          >
            <span className="material-symbols-outlined">add_circle</span>
          </button>
        </div>

        {/* Chat List */}
        <div className="flex-1 overflow-y-auto px-4 space-y-2 scrollbar-thin scrollbar-thumb-[var(--color-border)]">
          {isLoading ? (
            <div className="flex items-center justify-center h-20 text-[var(--color-textSecondary)]">
              <span className="text-sm">Loading...</span>
            </div>
          ) : chats.length === 0 ? (
            <div className="text-center text-[var(--color-textSecondary)] py-8 text-sm">
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
                  group relative flex items-center justify-between px-4 py-3 rounded-2xl cursor-pointer transition-all duration-200 mb-1
                  ${
                    selectedChatId === chat.id
                      ? "bg-[var(--color-primary)] text-[var(--color-background)] font-medium shadow-md"
                      : "bg-[var(--color-elevatedSurface)] hover:bg-[var(--color-elevatedSurface)] text-[var(--color-textSecondary)] hover:text-[var(--color-textPrimary)]"
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

        {/* User Profile Section with dropdown menu */}
        <div className="p-4 border-t border-[var(--color-border)] bg-[var(--color-surface)]">
          <div className="relative" ref={profileMenuRef}>
            <button
              onClick={() => setProfileMenuOpen((s) => !s)}
              className="flex items-center gap-3 p-2 rounded-xl bg-[var(--color-elevatedSurface)] hover:bg-[var(--color-elevatedSurface)] transition-colors w-full"
              aria-expanded={profileMenuOpen}
            >
              <div className="w-10 h-10 rounded-full bg-[var(--color-primary)] flex items-center justify-center text-[var(--color-background)] font-bold">
                {user?.firstName?.[0] || user?.username?.[0] || "U"}
              </div>
              <div className="flex-1 min-w-0 text-left">
                <p className="text-sm font-semibold text-[var(--color-textPrimary)] truncate">
                  {user?.firstName} {user?.lastName}
                </p>
                <p className="text-xs text-[var(--color-textSecondary)] truncate">
                  @{user?.username}
                </p>
              </div>
              <span className="material-symbols-outlined text-[var(--color-textSecondary)]">
                more_vert
              </span>
            </button>

            {profileMenuOpen && (
              <div className="absolute left-4 right-4 bottom-14 z-40">
                <div className="bg-[var(--color-surface)] rounded-md shadow-lg ring-1 ring-[var(--color-border)]">
                  <ul className="py-1">
                    <li>
                      <button
                        onClick={() => {
                          setProfileMenuOpen(false);
                          navigate("/settings");
                        }}
                        className="w-full text-left px-4 py-2 text-sm text-[var(--color-textPrimary)] hover:bg-[var(--color-elevatedSurface)]"
                      >
                        Settings
                      </button>
                    </li>
                    <li>
                      <button
                        onClick={() => {
                          setProfileMenuOpen(false);
                          logout();
                          navigate("/");
                        }}
                        className="w-full text-left px-4 py-2 text-sm text-[var(--color-error)] hover:bg-[var(--color-elevatedSurface)]"
                      >
                        Logout
                      </button>
                    </li>
                  </ul>
                </div>
              </div>
            )}
          </div>
        </div>
      </aside>
    </>
  );
};

export default ChatSidebar;
