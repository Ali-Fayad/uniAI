import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import "./index.css";

const mountApp = (selector: string) => {
  const el = document.getElementById(selector);
  if (!el) return null;
  return ReactDOM.createRoot(el).render(
    <React.StrictMode>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </React.StrictMode>
  );
};

// Mount to root for the main page
mountApp("root");

// Mount to auth-root for the auth split
mountApp("auth-root");

