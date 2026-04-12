import { AuthProvider } from "./context/AuthContext";
import { NotificationProvider } from "./components/common/Notification/NotificationProvider";
import Header from "./components/common/Header";
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
        <div className="min-h-screen flex flex-col bg-custom-light">
          <Header />

          <div className="flex-grow">
            <AppRouter />
          </div>
        </div>
      </NotificationProvider>
    </AuthProvider>
  );
};

export default App;
