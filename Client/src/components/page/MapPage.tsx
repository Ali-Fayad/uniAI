import { useEffect, useMemo, useRef, useState } from "react";
import {
  MapContainer,
  TileLayer,
  CircleMarker,
  Popup,
  useMap as useLeafletMap,
} from "react-leaflet";
import { LatLngBounds } from "leaflet";
import { useTheme } from "@/hooks/useTheme";
import "leaflet/dist/leaflet.css";
import useMap from "@/hooks/useMap";
import { ROUTES } from "@/router";
import { useNavigate } from "react-router-dom";
import LoadingSpinner from "@/components/common/LoadingSpinner";
import {
  formatLocation,
  type MapUniversity,
} from "@/components/page/map/mapUniversityMapper";

interface MapViewportControllerProps {
  campuses: MapUniversity[];
}

const MapViewportController = ({
  campuses,
}: MapViewportControllerProps) => {
  const map = useLeafletMap();
  const fittedBoundsKey = useRef<string | null>(null);

  useEffect(() => {
    if (!campuses.length) {
      fittedBoundsKey.current = null;
      return;
    }

    const boundsKey = campuses
      .map(({ universityId, campusId, coordinates }) =>
        `${universityId}:${campusId}:${coordinates[0]}:${coordinates[1]}`,
      )
      .join("|");

    if (fittedBoundsKey.current === boundsKey) {
      return;
    }

    fittedBoundsKey.current = boundsKey;

    map.fitBounds(
      new LatLngBounds(
        campuses.map(({ coordinates }) => coordinates),
      ),
      {
        padding: [40, 40],
        maxZoom: 12,
        animate: false,
      },
    );
  }, [campuses, map]);

  return null;
};

const MapPage = () => {
  const { themeName } = useTheme();
  const navigate = useNavigate();

  const {
    universities,
    setSelected,
    isSidebarOpen,
    toggleSidebar,
    openSidebar,
    closeSidebar,
    center,
    isLoading,
    error,
  } = useMap();

  /*
   * Controls the sidebar university filter.
   *
   * null:
   *   Show all university campus markers.
   *
   * university ID:
   *   Show only campus markers belonging to that university.
   */
  const [selectedUniversityId, setSelectedUniversityId] = useState<
    number | null
  >(null);

  /*
   * `universities` contains one item per campus because every campus needs
   * its own marker.
   *
   * The sidebar should display each university only once.
   */
  const sidebarUniversities = useMemo(() => {
    const uniqueUniversities = new Map<
      number,
      (typeof universities)[number]
    >();

    universities.forEach((university) => {
      if (!uniqueUniversities.has(university.universityId)) {
        uniqueUniversities.set(
          university.universityId,
          university,
        );
      }
    });

    return Array.from(uniqueUniversities.values());
  }, [universities]);

  /*
   * Keep the original campus marker list when no university is selected.
   * Otherwise, show only the campuses of the selected university.
   */
  const visibleUniversities = useMemo(() => {
    if (selectedUniversityId === null) {
      return universities;
    }

    return universities.filter(
      (university) =>
        university.universityId === selectedUniversityId,
    );
  }, [universities, selectedUniversityId]);

  useEffect(() => {
    const mapContainer = document.querySelector(
      ".leaflet-container",
    );

    if (mapContainer) {
      if (themeName === "dark") {
        mapContainer.classList.add("dark-mode-map");
      } else {
        mapContainer.classList.remove("dark-mode-map");
      }
    }
  }, [themeName]);

  const handleSidebarUniversityClick = (
    university: (typeof universities)[number],
  ) => {
    const isAlreadySelected =
      selectedUniversityId === university.universityId;

    if (isAlreadySelected) {
      /*
       * Clicking the active university deselects it and restores
       * every campus marker.
       */
      setSelectedUniversityId(null);
      setSelected(null);
      return;
    }

    /*
     * Only one university can be selected.
     * Selecting another university replaces the previous filter.
     */
    setSelectedUniversityId(university.universityId);
    setSelected(university);
  };

  const tileLayerUrl =
    themeName === "dark"
      ? "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
      : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";

  const attribution =
    themeName === "dark"
      ? '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
      : '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';

  return (
    <div className="relative h-[calc(100vh-64px)] w-full overflow-hidden bg-[var(--color-background)]">
      {/* Fullscreen map */}
      <div className="absolute inset-0 z-0">
        <MapContainer
          center={center}
          zoom={9}
          scrollWheelZoom
          style={{
            height: "100%",
            width: "100%",
          }}
        >
          <TileLayer
            attribution={attribution}
            url={tileLayerUrl}
          />

          <MapViewportController campuses={visibleUniversities} />

          {!isLoading &&
            !error &&
            visibleUniversities.map((university) => (
              <CircleMarker
                key={`${university.universityId}-${university.campusId}`}
                center={university.coordinates}
                radius={10}
                pathOptions={{
                  color: university.color,
                  fillColor: university.color,
                  fillOpacity: 0.9,
                }}
                eventHandlers={{
                  click: () => {
                    /*
                     * Preserve the existing marker behavior.
                     * Clicking a marker selects its campus and opens
                     * the sidebar without activating a sidebar filter.
                     */
                    setSelected(university);
                    openSidebar();
                  },
                }}
              >
                <Popup>
                  <div className="max-w-xs">
                    <h3 className="font-bold">
                      {university.name}
                    </h3>

                    <p className="text-sm">
                      {university.campusName}
                    </p>

                    <p className="text-sm">
                      {formatLocation(university)}
                    </p>
                  </div>
                </Popup>
              </CircleMarker>
            ))}
        </MapContainer>
      </div>

      {(isLoading ||
        error ||
        (!universities.length && !isLoading)) && (
        <div className="pointer-events-none absolute inset-0 z-10 flex items-center justify-center">
          <div className="pointer-events-auto rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)]/95 px-6 py-5 text-center shadow-lg">
            {isLoading && (
              <LoadingSpinner text="Loading universities..." />
            )}

            {!isLoading && error && (
              <p className="text-sm text-[var(--color-error)]">
                {error}
              </p>
            )}

            {!isLoading &&
              !error &&
              !universities.length && (
                <p className="text-sm text-[var(--color-textSecondary)]">
                  No university campuses with map coordinates are
                  available.
                </p>
              )}
          </div>
        </div>
      )}

      {/* Floating sidebar toggle */}
      <div className="fixed right-6 top-6 z-50">
        <button
          type="button"
          aria-label={
            isSidebarOpen
              ? "Close universities sidebar"
              : "Open universities sidebar"
          }
          onClick={toggleSidebar}
          className="inline-flex h-12 w-12 items-center justify-center rounded-full border border-[var(--color-border)] bg-[var(--color-primary)] text-[var(--color-background)] shadow-lg hover:shadow-xl"
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

      {/* Universities sidebar */}
      <aside
        className={`fixed right-0 top-0 z-20 h-full w-full transform transition-transform duration-300 ease-in-out sm:w-96 ${
          isSidebarOpen
            ? "translate-x-0"
            : "translate-x-full"
        }`}
        style={{
          background: "var(--color-surface)",
          borderLeft: "1px solid var(--color-border)",
        }}
      >
        <div className="flex h-full flex-col p-4">
          <div className="mb-4 flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold text-[var(--color-textPrimary)]">
                Universities
              </h2>

              {selectedUniversityId !== null && (
                <p className="mt-1 text-xs text-[var(--color-textSecondary)]">
                  Click the selected university again to show all
                  campuses.
                </p>
              )}
            </div>

            <button
              type="button"
              onClick={closeSidebar}
              aria-label="Close universities sidebar"
              className="inline-flex h-8 w-8 items-center justify-center rounded bg-transparent text-[var(--color-textSecondary)] hover:bg-[var(--color-elevatedSurface)]"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-5 w-5"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                  clipRule="evenodd"
                />
              </svg>
            </button>
          </div>

          <div className="flex-1 space-y-3 overflow-auto">
            {sidebarUniversities.map((university) => {
              const isSelected =
                selectedUniversityId ===
                university.universityId;

              return (
                <button
                  type="button"
                  key={university.universityId}
                  onClick={() =>
                    handleSidebarUniversityClick(university)
                  }
                  aria-pressed={isSelected}
                  className={`flex w-full cursor-pointer items-start gap-3 rounded-lg border p-3 text-left transition-shadow hover:shadow-md ${
                    isSelected
                      ? "border-[var(--color-primary)] bg-[var(--color-elevatedSurface)] ring-2 ring-[var(--color-primary)]"
                      : "border-[var(--color-border)]"
                  }`}
                >
                  <span
                    className="mt-1.5 h-3 w-3 shrink-0 rounded-full"
                    style={{
                      background: university.color,
                    }}
                  />

                  <span className="min-w-0">
                    <span className="block font-medium text-[var(--color-textPrimary)]">
                      {university.name}
                    </span>

                    {university.acronym && (
                      <span className="block text-sm text-[var(--color-textSecondary)]">
                        {university.acronym}
                      </span>
                    )}

                    {university.nameAr && (
                      <span className="block text-sm text-[var(--color-textSecondary)]">
                        {university.nameAr}
                      </span>
                    )}
                  </span>
                </button>
              );
            })}
          </div>
        </div>
      </aside>

      {/* Floating back-to-menu button */}
      <div className="fixed bottom-6 right-6 z-30">
        <button
          type="button"
          onClick={() => navigate(ROUTES.HOME)}
          className="inline-flex h-12 items-center justify-center rounded-full bg-[var(--color-primary)] px-4 text-[var(--color-background)] shadow-lg hover:shadow-xl"
        >
          Back to Menu
        </button>
      </div>

      {/* Dark-mode styles for Leaflet */}
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
