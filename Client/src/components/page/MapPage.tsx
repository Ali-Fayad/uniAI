import { useEffect } from "react";
import { MapContainer, TileLayer, CircleMarker, Popup } from "react-leaflet";
import { useTheme } from "@/hooks/useTheme";
import { TEXT } from "@/constants/static";
import "leaflet/dist/leaflet.css";
import useMap from "@/hooks/useMap";
import { ROUTES } from "@/router";
import { useNavigate } from "react-router-dom";

const MapPage = () => {
  const { themeName } = useTheme();
  const navigate = useNavigate();

  const {
    universities,
    selected,
    setSelected,
    isSidebarOpen,
    toggleSidebar,
    openSidebar,
    closeSidebar,
    center,
  } = useMap();

  useEffect(() => {
    const mapContainer = document.querySelector(".leaflet-container");
    if (mapContainer) {
      if (themeName === "dark") {
        mapContainer.classList.add("dark-mode-map");
      } else {
        mapContainer.classList.remove("dark-mode-map");
      }
    }
  }, [themeName]);

  const tileLayerUrl =
    themeName === "dark"
      ? "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
      : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";

  const attribution =
    themeName === "dark"
      ? '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
      : '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';

  return (
    <div className="relative min-h-screen w-full bg-[var(--color-background)] overflow-hidden">
      {/* Fullscreen Map as background */}
      <div className="absolute inset-0 z-0">
        <MapContainer
          center={center}
          zoom={9}
          scrollWheelZoom
          style={{ height: "100%", width: "100%" }}
        >
          <TileLayer attribution={attribution} url={tileLayerUrl} />
          {universities.map((u) => (
            <CircleMarker
              key={u.id}
              center={u.coordinates}
              radius={10}
              pathOptions={{
                color: u.color,
                fillColor: u.color,
                fillOpacity: 0.9,
              }}
              eventHandlers={{
                click: () => {
                  setSelected(u);
                  openSidebar();
                },
              }}
            >
              <Popup>
                <div className="max-w-xs">
                  <h3 className="font-bold">{u.name}</h3>
                  <p className="text-sm">{u.description}</p>
                </div>
              </Popup>
            </CircleMarker>
          ))}
        </MapContainer>
      </div>

      {/* Floating sidebar toggle (top-right) */}
      <div className="fixed z-50 top-6 right-6">
        <button
          aria-label={
            isSidebarOpen
              ? "Close universities sidebar"
              : "Open universities sidebar"
          }
          onClick={toggleSidebar}
          className="inline-flex items-center justify-center h-12 w-12 rounded-full bg-[var(--color-primary)] text-[var(--color-background)] border border-[var(--color-border)] shadow-lg hover:shadow-xl"
        >
          {isSidebarOpen ? (
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-6 w-6"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                clipRule="evenodd"
              />
            </svg>
          ) : (
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-6 w-6"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path d="M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 15a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" />
            </svg>
          )}
        </button>
      </div>

      {/* Header removed by request: Map is full background and controls are overlaid */}

      {/* Sidebar (right) */}
      <aside
        className={`fixed top-0 right-0 h-full z-20 transform transition-transform duration-300 ease-in-out ${
          isSidebarOpen ? "translate-x-0" : "translate-x-full"
        } w-full sm:w-96`}
        style={{
          background: "var(--color-surface)",
          borderLeft: "1px solid var(--color-border)",
        }}
      >
        <div className="p-4 h-full flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-[var(--color-textPrimary)]">
              Universities
            </h2>
            <button
              onClick={closeSidebar}
              aria-label="Close universities sidebar"
              className="inline-flex items-center justify-center h-8 w-8 rounded bg-transparent text-[var(--color-textSecondary)] hover:bg-[var(--color-elevatedSurface)]"
            />
          </div>

          <div className="overflow-auto flex-1 space-y-3">
            {universities.map((u) => (
              <div
                key={u.id}
                onClick={() => {
                  setSelected(u);
                }}
                className={`p-3 rounded-lg border border-[var(--color-border)] hover:shadow-md cursor-pointer flex items-start gap-3 ${
                  selected?.id === u.id
                    ? "ring-2 ring-[var(--color-primary)]"
                    : ""
                }`}
              >
                <div
                  style={{
                    width: 12,
                    height: 12,
                    background: u.color,
                    borderRadius: 6,
                    marginTop: 6,
                  }}
                />
                <div>
                  <h3 className="font-medium text-[var(--color-textPrimary)]">
                    {u.name}
                  </h3>
                  <p className="text-sm text-[var(--color-textSecondary)]">
                    {u.description}
                  </p>
                  <a
                    href={u.website}
                    target="_blank"
                    rel="noreferrer"
                    className="text-sm text-[var(--color-primary)] underline mt-1 inline-block"
                  >
                    {TEXT.map.visitWebsite}
                  </a>
                </div>
              </div>
            ))}
          </div>
        </div>
      </aside>

      {/* Floating back to menu button */}
      <div className="fixed z-30 bottom-6 right-6">
        <button
          onClick={() => navigate(ROUTES.HOME)}
          className="inline-flex items-center justify-center h-12 px-4 rounded-full bg-[var(--color-primary)] text-[var(--color-background)] shadow-lg hover:shadow-xl"
        >
          Back to Menu
        </button>
      </div>

      {/* Dark mode styles for Leaflet controls and popup tweaks */}
      <style>{`
        .dark-mode-map .leaflet-control-attribution {
          background-color: rgba(0, 0, 0, 0.6) !important;
          color: #fff !important;
        }
        .dark-mode-map .leaflet-popup-content-wrapper {
          background-color: #222 !important;
          color: #eee !important;
        }
      `}</style>
    </div>
  );
};

export default MapPage;
