import React from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

/**
 * Header component for the application
 */
const Header: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <header className="sticky top-0 z-50 bg-white/70 backdrop-blur-md shadow-sm">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8">
        <div className="relative flex h-16 items-center justify-center">
          <div className="absolute inset-y-0 left-0 flex items-center sm:hidden"></div>

          <div className="flex flex-1 items-center justify-center sm:items-stretch sm:justify-start">
            <div className="flex flex-shrink-0 items-center">
              <svg
                className="h-8 w-auto text-custom-primary"
                fill="currentColor"
                viewBox="0 0 54 44"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path d="M26.5816 43.3134L53.1633 0H39.8724L26.5816 26.5816L13.2908 0H0L26.5816 43.3134Z"></path>
              </svg>
            </div>
          </div>

          <div className="absolute inset-y-0 flex items-center justify-center">
            <button
              onClick={() => navigate("/")}
              className="font-playful text-2xl font-bold tracking-tight text-[#151514] hover:text-custom-primary transition-colors cursor-pointer bg-transparent"
            >
              uniAI
            </button>
          </div>

          <div className="absolute inset-y-0 right-0 flex items-center pr-2 sm:static sm:inset-auto sm:ml-6 sm:pr-0 gap-2">
            {isAuthenticated && (
              <button
                onClick={handleLogout}
                className="relative rounded-full bg-transparent px-4 py-2 text-sm font-medium text-gray-700 hover:text-custom-primary focus:outline-none focus:ring-2 focus:ring-custom-primary focus:ring-offset-2 transition-colors"
              >
                Logout
              </button>
            )}

            {/* Theme toggle removed to keep SPA navbar consistent */}
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
