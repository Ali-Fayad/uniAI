import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import "./index.css";
import themes, { themeToCssVars } from "./styles/themes";

// Apply initial theme CSS variables to :root so styles are available immediately
// Uses saved preference from localStorage (key: 'theme') or falls back to 'light'
const applyInitialTheme = () => {
  try {
    const saved = (localStorage.getItem("theme") as keyof typeof themes) || "light";
    const theme = themes[saved] || themes.light;
    const vars = themeToCssVars(theme);
    Object.entries(vars).forEach(([k, v]) => {
      document.documentElement.style.setProperty(k, v);
    });
  } catch (e) {
    // ignore runtime errors during initial paint
  }
};

applyInitialTheme();

// Mount the full application with routing
const root = document.getElementById("root");
if (root) {
  ReactDOM.createRoot(root).render(
    <React.StrictMode>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </React.StrictMode>
  );
}
