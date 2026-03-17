import React from 'react';
import { NavLink } from 'react-router-dom';
import { ROUTES } from '../../../router';

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `px-3 py-2 rounded-md text-sm font-medium transition-colors ${isActive ? 'text-[var(--color-primary)]' : 'text-[var(--color-textSecondary)] hover:text-[var(--color-primary)]'}`;

const NavbarLinks: React.FC = () => {
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
    </div>
  );
};

export default NavbarLinks;
