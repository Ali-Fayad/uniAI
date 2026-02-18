import { AuthProvider } from "./context/AuthContext";
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
      <div className="min-h-screen flex flex-col bg-custom-light">
        <Header />

        <div className="flex-grow">
          <AppRouter />
        </div>
      </div>
    </AuthProvider>
  );
};

export default App;
