import { useLocation } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Header from "./components/common/Header";
import { AppRouter, ROUTES } from "./router";

/**
 * Main Application Component
 * 
 * Responsibilities:
 * - Provide authentication context
 * - Conditionally render header
 * - Render application router
 * 
 * All routing configuration has been moved to router.tsx
 */
const App = () => {
  const location = useLocation();
  // Hide header on chat and map routes where we want full-bleed UI
  const showHeader = location.pathname !== ROUTES.CHAT && location.pathname !== ROUTES.MAP;

  return (
    <AuthProvider>
      <div className="min-h-screen flex flex-col bg-custom-light">
        {showHeader && <Header />}

        <div className="flex-grow">
          <AppRouter />
        </div>
      </div>
    </AuthProvider>
  );
};

export default App;
