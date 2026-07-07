/**
 * useMap hook
 *
 * Responsible only for interactive map state (selected university, sidebar
 * open/close).  Static university data lives in data/universities.ts (SRP).
 */

import { useState } from 'react';
import { UNIVERSITIES } from '../data/universities';
import type { University } from '../data/universities';

// Re-export University type so existing consumers keep their import path.
export type { University };

export const useMap = () => {
  const [selected, setSelected] = useState<University | null>(null);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  const toggleSidebar = () => setIsSidebarOpen((s: boolean) => !s);
  const openSidebar = () => setIsSidebarOpen(true);
  const closeSidebar = () => setIsSidebarOpen(false);

  const center: [number, number] = [33.8547, 35.8623];

  return {
    universities: UNIVERSITIES,
    selected,
    setSelected,
    isSidebarOpen,
    toggleSidebar,
    openSidebar,
    closeSidebar,
    center,
  } as const;
};

export default useMap;

