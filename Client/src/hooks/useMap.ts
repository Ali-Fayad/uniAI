/**
 * useMap hook
 *
 * Owns interactive map state while delegating server-backed campus loading to
 * useMapUniversities.
 */

import { useState } from 'react';
import { useMapUniversities } from './useMapUniversities';
import type { MapUniversity } from '../components/page/map/mapUniversityMapper';

export type { MapUniversity };

export const useMap = () => {
  const [selected, setSelected] = useState<MapUniversity | null>(null);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const mapUniversities = useMapUniversities();

  const toggleSidebar = () => setIsSidebarOpen((s: boolean) => !s);
  const openSidebar = () => setIsSidebarOpen(true);
  const closeSidebar = () => setIsSidebarOpen(false);

  const center: [number, number] = [33.8547, 35.8623];

  return {
    universities: mapUniversities.universities,
    isLoading: mapUniversities.isLoading,
    error: mapUniversities.error,
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
