import React from "react";
import ReactDOM from "react-dom/client";
import MainPage from "./pages/MainPage";
import "./index.css";

// Mount only the main landing page (no router needed)
const root = document.getElementById("root");
if (root) {
  ReactDOM.createRoot(root).render(
    <React.StrictMode>
      <MainPage />
    </React.StrictMode>
  );
}
