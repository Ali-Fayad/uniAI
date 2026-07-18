import {
  Info,
  LayoutDashboard,
  MessageCircle,
  MapPinned,
  Settings2,
  Files,
  type LucideIcon,
} from "lucide-react";
import { ROUTES } from "../router";

export type AppNavigationItem = {
  id: "chat" | "cvs" | "map" | "settings" | "about" | "admin";
  label: string;
  displayLabel?: string;
  route: string;
  icon: LucideIcon;
  requiresAuth: boolean;
  requiresAdmin?: boolean;
  activeRoutes: readonly string[];
  description?: string;
};

export const APP_NAVIGATION_ITEMS: readonly AppNavigationItem[] = [
  {
    id: "chat",
    label: "Chat",
    route: ROUTES.CHAT,
    icon: MessageCircle,
    requiresAuth: true,
    activeRoutes: [ROUTES.CHAT],
    description: "Open the chat assistant",
  },
  {
    id: "cvs",
    label: "CVs",
    route: ROUTES.CVS,
    icon: Files,
    requiresAuth: true,
    activeRoutes: [ROUTES.CVS, ROUTES.CV_BUILDER],
    description: "View and edit CVs",
  },
  {
    id: "map",
    label: "Map",
    route: ROUTES.MAP,
    icon: MapPinned,
    requiresAuth: true,
    activeRoutes: [ROUTES.MAP],
    description: "Explore the university map",
  },
  {
    id: "settings",
    label: "Settings",
    route: ROUTES.SETTINGS,
    icon: Settings2,
    requiresAuth: true,
    activeRoutes: [ROUTES.SETTINGS],
    description: "Update profile and preferences",
  },
  {
    id: "about",
    label: "About",
    route: ROUTES.ABOUT,
    icon: Info,
    requiresAuth: true,
    activeRoutes: [ROUTES.ABOUT],
    description: "Learn about uniAI",
  },
  {
    id: "admin",
    label: "Admin Dashboard",
    displayLabel: "Admin",
    route: ROUTES.ADMIN,
    icon: LayoutDashboard,
    requiresAuth: true,
    requiresAdmin: true,
    activeRoutes: [ROUTES.ADMIN],
    description: "Open the admin dashboard",
  },
];

export const isAppNavigationItemActive = (
  item: AppNavigationItem,
  pathname: string
) =>
  item.activeRoutes.some(
    (route) => pathname === route || pathname.startsWith(`${route}/`)
  );

export const filterAppNavigationItems = (
  items: readonly AppNavigationItem[],
  isAuthenticated: boolean,
  isAdmin: boolean
) =>
  items.filter((item) => {
    if (item.requiresAuth && !isAuthenticated) {
      return false;
    }

    if (item.requiresAdmin && !isAdmin) {
      return false;
    }

    return true;
  });