import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import styles from './Navbar.module.css';
import NavbarLinks from './NavbarLinks';
import NavbarActions from './NavbarActions';

interface NavbarProps {
  // If parent wants to override auth state handling it can pass handlers
}

const Navbar: React.FC<NavbarProps> = () => {
  return (
    <header className={`${styles.navbar} bg-[var(--color-surface)] border-b border-[var(--color-border)] z-50`}
      style={{ height: 'var(--navbar-height)' }}
    >
      <div className={`${styles.container} container mx-auto px-4 sm:px-6 lg:px-8 h-full`}>
        <div className="flex items-center justify-between h-full">
          <div className="flex items-center gap-4">
            <NavLink to="/" className="flex items-center gap-3">
              <svg className="h-8 w-auto text-[var(--color-surface)]" fill="currentColor" viewBox="0 0 54 44" xmlns="http://www.w3.org/2000/svg">
                <path d="M26.5816 43.3134L53.1633 0H39.8724L26.5816 26.5816L13.2908 0H0L26.5816 43.3134Z"></path>
              </svg>
              <span className="font-playful text-xl font-bold tracking-tight text-[var(--color-textPrimary)]">uniAI</span>
            </NavLink>
            <nav className="hidden md:flex">
              <NavbarLinks />
            </nav>
          </div>

          <div className="hidden md:flex items-center">
            <NavbarActions />
          </div>

          {/* Mobile menu placeholder */}
          <div className="md:hidden">
            <button aria-label="Open menu" className="p-2">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
