import React, { useState } from "react";
import { FaUserCircle } from "react-icons/fa";
import { TokenStorage } from "../../utils/storage";

const ChatNavBar: React.FC = () => {
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const userData = TokenStorage.getData();

  const handleLogout = () => {
    TokenStorage.logout();
    window.location.href = "/auth"; // redirect after logout
  };

  return (
    <header className="sticky top-0 z-30 flex h-16 w-full items-center justify-center border-b border-custom-secondary/50 bg-white/70 backdrop-blur-md px-4">
      <div className="absolute left-4">
        <svg
          className="h-8 w-auto text-custom-primary"
          fill="currentColor"
          viewBox="0 0 54 44"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path d="M26.5816 43.3134L53.1633 0H39.8724L26.5816 26.5816L13.2908 0H0L26.5816 43.3134Z"></path>
        </svg>
      </div>

      <span className="font-playful text-2xl font-bold tracking-tight text-[#151514]">
        uniAI
      </span>

      {/* User dropdown */}
      <div className="absolute right-4 relative">
        <button
          onClick={() => setDropdownOpen(!dropdownOpen)}
          className="flex items-center gap-2 rounded-full p-1 hover:bg-custom-light transition-colors"
        >
          <FaUserCircle size={28} className="text-[#151514]" />
        </button>

        {dropdownOpen && userData && (
          <div className="absolute right-0 mt-2 w-48 rounded-lg border border-custom-secondary/50 bg-white shadow-lg z-50">
            <div className="px-4 py-2 border-b border-custom-secondary/50">
              <p className="font-semibold text-sm">
                {userData.firstName} {userData.lastName}
              </p>
              <p className="text-xs text-[#797672]">@{userData.username}</p>
            </div>
            <button
              className="w-full text-left px-4 py-2 text-[#151514] hover:bg-custom-light transition-colors"
              onClick={() => console.log("Profile clicked")}
            >
              Profile
            </button>
            <button
              className="w-full text-left px-4 py-2 text-[#151514] hover:bg-custom-light transition-colors"
              onClick={handleLogout}
            >
              Logout
            </button>
          </div>
        )}
      </div>
    </header>
  );
};

export default ChatNavBar;
