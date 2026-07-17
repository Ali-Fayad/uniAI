/**
 * useChatSidebar hook
 *
 * Owns all data-fetching, deletion, and UI-state concerns for the chat
 * sidebar.  Extracted from ChatSidebar.tsx so the component is responsible
 * only for rendering (SRP).  Also replaces the direct `useContext(AuthContext)!`
 * bang-cast with the guarded `useAuth()` hook (LSP).
 */

import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { chatService } from '../services/chat';
import { useAuth } from './useAuth';
import { useOnClickOutside } from './useOnClickOutside';
import type { Chat } from '../types/dto';

export interface UseChatSidebarReturn {
  user: ReturnType<typeof useAuth>['user'];
  chats: Chat[];
  isLoading: boolean;
  isSidebarOpen: boolean;
  profileMenuOpen: boolean;
  profileMenuRef: React.RefObject<HTMLDivElement | null>;
  setIsSidebarOpen: (open: boolean) => void;
  setProfileMenuOpen: React.Dispatch<React.SetStateAction<boolean>>;
  handleDeleteChat: (chatId: number, e: React.MouseEvent) => Promise<void>;
  handleLogout: () => void;
  handleNavigateSettings: () => void;
}

export const useChatSidebar = (
  chatListRefreshKey: number,
  onDeleteChat: (chatId: number) => void,
): UseChatSidebarReturn => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const userId = user?.id;

  const [chats, setChats] = useState<Chat[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);
  const profileMenuRef = useRef<HTMLDivElement | null>(null);
  const previousUserIdRef = useRef<number | null | undefined>(undefined);
  const latestRequestIdRef = useRef(0);

  /** Reload chats whenever the active user changes or an explicit refresh is requested. */
  useEffect(() => {
    if (userId == null) {
      previousUserIdRef.current = undefined;
      setChats([]);
      setIsLoading(false);
      return;
    }

    const hasUserChanged = previousUserIdRef.current !== userId;
    previousUserIdRef.current = userId;

    if (hasUserChanged) {
      setChats([]);
    }

    const requestId = ++latestRequestIdRef.current;
    setIsLoading(true);

    const loadChats = async () => {
      try {
        const data = await chatService.getChats();
        if (requestId === latestRequestIdRef.current) {
          setChats(data);
        }
      } catch (error) {
        console.error('Failed to load chats:', error);
      } finally {
        if (requestId === latestRequestIdRef.current) {
          setIsLoading(false);
        }
      }
    };

    loadChats();
  }, [chatListRefreshKey, userId]);

  /** Close the profile menu when the user clicks outside it. */
  useOnClickOutside(profileMenuRef, () => setProfileMenuOpen(false), {
    eventType: 'click',
    enabled: profileMenuOpen,
  });

  const handleDeleteChat = async (chatId: number, e: React.MouseEvent) => {
    e.stopPropagation();
    if (confirm('Are you sure you want to delete this chat?')) {
      try {
        await chatService.deleteChat(chatId);
        setChats((prev) => prev.filter((c) => c.id !== chatId));
        onDeleteChat(chatId);
      } catch (error) {
        console.error('Failed to delete chat:', error);
      }
    }
  };

  const handleLogout = () => {
    setProfileMenuOpen(false);
    logout();
    navigate('/');
  };

  const handleNavigateSettings = () => {
    setProfileMenuOpen(false);
    navigate('/settings');
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
