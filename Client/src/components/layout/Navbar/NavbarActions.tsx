import React from 'react';
import { useAuth } from '../../../hooks/useAuth';
import { FiLogOut } from 'react-icons/fi';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '../../../router';

const NavbarActions: React.FC = () => {
  const { isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate(ROUTES.HOME);
  };

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="flex items-center gap-2">
      <button
        onClick={handleLogout}
        className="p-2 rounded-md text-[var(--color-textSecondary)] hover:text-[var(--color-primary)] transition-colors"
        aria-label="Logout"
      >
        <FiLogOut size={20} />
      </button>
    </div>
  );
};

export default NavbarActions;
