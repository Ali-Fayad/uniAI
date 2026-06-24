import { AuthProvider } from "./context/AuthContext";
import { NotificationProvider } from "./components/common/Notification/NotificationProvider";
import Header from "./components/common/Header";
import AppDock from "./components/layout/AppDock";
import { AppRouter } from "./router";

/**
 * Main Application Component
 * 
 * Responsibilities:
 * - Provide authentication context
 * - Render global header/navbar on all pages
 * - Render application router
 * 
 * All routing configuration has been moved to router.tsx
 */
const App = () => {
  return (
    <AuthProvider>
      <NotificationProvider>
        <div className="min-h-screen flex flex-col bg-custom-light relative">
          <Header />
          <AppDock />

          <div className="flex-grow md:pr-28">
            <AppRouter />
          </div>
        </div>
      </NotificationProvider>
    </AuthProvider>
  );
};

export default App;
