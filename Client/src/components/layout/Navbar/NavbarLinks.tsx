import React from 'react';
import { NavLink } from 'react-router-dom';
import { ROUTES } from '../../../router';
import { useAuth } from '../../../hooks/useAuth';

/**
 * NavbarLinks
 *
 * Responsible for rendering the primary navigation links.
 *
 * Does NOT contain authorization logic and does NOT perform API calls.
 */

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `px-3 py-2 rounded-md text-sm font-medium transition-colors ${isActive ? 'text-[var(--color-primary)]' : 'text-[var(--color-textSecondary)] hover:text-[var(--color-primary)]'}`;

const NavbarLinks: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const isAdmin = isAuthenticated && user?.role === 'ADMIN';

  return (
    <div className="flex items-center gap-2">
      <NavLink to={ROUTES.MAP} className={linkClass}>
        Map
      </NavLink>
      <NavLink to={ROUTES.CHAT} className={linkClass}>
        Chat
      </NavLink>
      <NavLink to={ROUTES.SETTINGS} className={linkClass}>
        Settings
      </NavLink>
      <NavLink to={ROUTES.ABOUT} className={linkClass}>
        About Us
      </NavLink>
      {isAdmin && (
        <NavLink to={ROUTES.ADMIN} className={linkClass}>
          Admin
        </NavLink>
      )}
    </div>
  );
};

export default NavbarLinks;
