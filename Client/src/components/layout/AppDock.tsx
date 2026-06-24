import React from "react";
import { NavLink, useLocation } from "react-router-dom";
import { Dock, DockIcon } from "../ui/dock";
import { useAuth } from "../../hooks/useAuth";
import {
  APP_NAVIGATION_ITEMS,
  filterAppNavigationItems,
  isAppNavigationItemActive,
} from "../../config/navigation";
import { cn } from "@/lib/utils";

const AppDock: React.FC = () => {
  const location = useLocation();
  const { isAuthenticated, user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const visibleItems = filterAppNavigationItems(
    APP_NAVIGATION_ITEMS,
    isAuthenticated,
    isAdmin
  );

  if (!isAuthenticated || visibleItems.length === 0) {
    return null;
  }

  return (
    <aside
      className="fixed right-4 top-1/2 z-50 hidden -translate-y-1/2 md:block print:hidden"
      aria-label="Application navigation"
    >
      <Dock
        orientation="vertical"
        className="mx-0 mt-0 w-[76px] items-center justify-center gap-2 rounded-[28px] border border-[var(--color-border)] bg-[var(--color-surface)]/85 p-2.5 shadow-[0_18px_45px_rgba(0,0,0,0.12)] backdrop-blur-xl"
        iconSize={60}
        iconMagnification={68}
        iconDistance={180}
      >
        {visibleItems.map((item) => {
          const isActive = isAppNavigationItemActive(item, location.pathname);
          const Icon = item.icon;

          return (
            <DockIcon
              key={item.id}
              className={cn(
                "overflow-hidden border transition-all duration-200 focus-within:ring-2 focus-within:ring-[var(--color-primary)] focus-within:ring-offset-2 focus-within:ring-offset-[var(--color-surface)]",
                isActive
                  ? "border-[var(--color-primary)] bg-[var(--color-primary)]/12 text-[var(--color-primary)] shadow-[0_10px_30px_rgba(0,0,0,0.08)]"
                  : "border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-textSecondary)] hover:border-[var(--color-primary)] hover:bg-[var(--color-background)] hover:text-[var(--color-primary)]"
              )}
              title={item.description ?? item.label}
            >
              <NavLink
                to={item.route}
                aria-label={item.label}
                aria-current={isActive ? "page" : undefined}
                className="flex h-full w-full flex-col items-center justify-center gap-1 rounded-[inherit] px-2 py-2 text-center outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)] focus-visible:ring-offset-2 focus-visible:ring-offset-[var(--color-surface)]"
              >
                <Icon className="h-5 w-5" aria-hidden="true" />
                <span className="max-w-[56px] text-[9px] font-semibold leading-tight tracking-tight whitespace-normal">
                  {item.displayLabel ?? item.label}
                </span>
              </NavLink>
            </DockIcon>
          );
        })}
      </Dock>
    </aside>
  );
};

export default AppDock;
