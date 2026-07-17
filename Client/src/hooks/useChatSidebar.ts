/**
 * useChatSidebar hook
 *
 * Owns all data-fetching, deletion, and UI-state concerns for the chat
 * sidebar. Extracted from ChatSidebar.tsx so the component is responsible
 * only for rendering.
 */

import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { chatService } from "../services/chat";
import { useAuth } from "./useAuth";
import { useOnClickOutside } from "./useOnClickOutside";
import type { Chat } from "../types/dto";

export interface UseChatSidebarReturn {
  user: ReturnType<typeof useAuth>["user"];
  chats: Chat[];
  isLoading: boolean;
  isSidebarOpen: boolean;
  profileMenuOpen: boolean;
  profileMenuRef: React.RefObject<HTMLDivElement | null>;
  setIsSidebarOpen: (open: boolean) => void;
  setProfileMenuOpen: React.Dispatch<React.SetStateAction<boolean>>;
  handleDeleteChat: (
    chatId: number,
    event: React.MouseEvent,
  ) => Promise<void>;
  handleLogout: () => void;
  handleNavigateSettings: () => void;
}

export const useChatSidebar = (
  chatListRefreshKey: number,
  onDeleteChat: (chatId: number) => void,
): UseChatSidebarReturn => {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuth();

  const [chats, setChats] = useState<Chat[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);

  const profileMenuRef = useRef<HTMLDivElement | null>(null);
  const latestRequestIdRef = useRef(0);

  /**
   * Reload chats whenever the authenticated session becomes available
   * or an explicit refresh is requested.
   */
  useEffect(() => {
    if (!isAuthenticated) {
      // Invalidate any request started by a previous authenticated session.
      latestRequestIdRef.current += 1;
      setChats([]);
      setIsLoading(false);
      return;
    }

    const requestId = ++latestRequestIdRef.current;
    let isDisposed = false;

    setIsLoading(true);

    const loadChats = async () => {
      try {
        const data = await chatService.getChats();

        if (
          !isDisposed &&
          requestId === latestRequestIdRef.current
        ) {
          setChats(data);
        }
      } catch (error) {
        if (
          !isDisposed &&
          requestId === latestRequestIdRef.current
        ) {
          console.error("Failed to load chats:", error);
          setChats([]);
        }
      } finally {
        if (
          !isDisposed &&
          requestId === latestRequestIdRef.current
        ) {
          setIsLoading(false);
        }
      }
    };

    void loadChats();

    return () => {
      isDisposed = true;
    };
  }, [chatListRefreshKey, isAuthenticated]);

  useOnClickOutside(profileMenuRef, () => setProfileMenuOpen(false), {
    eventType: "click",
    enabled: profileMenuOpen,
  });

  const handleDeleteChat = async (
    chatId: number,
    event: React.MouseEvent,
  ) => {
    event.stopPropagation();

    if (!window.confirm("Are you sure you want to delete this chat?")) {
      return;
    }

    try {
      await chatService.deleteChat(chatId);
      setChats((previousChats) =>
        previousChats.filter((chat) => chat.id !== chatId),
      );
      onDeleteChat(chatId);
    } catch (error) {
      console.error("Failed to delete chat:", error);
    }
  };

  const handleLogout = () => {
    setProfileMenuOpen(false);
    logout();
    navigate("/");
  };

  const handleNavigateSettings = () => {
    setProfileMenuOpen(false);
    navigate("/settings");
  };

  return {
    user,
    chats,
    isLoading,
    isSidebarOpen,
    profileMenuOpen,
    profileMenuRef,
    setIsSidebarOpen,
    setProfileMenuOpen,
    handleDeleteChat,
    handleLogout,
    handleNavigateSettings,
  };
};