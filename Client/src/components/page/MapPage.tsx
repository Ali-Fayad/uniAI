import { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { useTheme } from '@/hooks/useTheme';
import { TEXT } from '@/constants/static';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// Fix for default marker icon issue in React-Leaflet
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

// @ts-ignore
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
});

// Lebanese Universities Data
const universities = [
  {
    id: 1,
    name: 'American University of Beirut (AUB)',
    coordinates: [33.8990, 35.4807] as [number, number],
    description: 'Founded in 1866, AUB is a leading institution of higher learning in Lebanon and the Middle East.',
    website: 'https://www.aub.edu.lb',
  },
  {
    id: 2,
    name: 'Lebanese American University (LAU)',
    coordinates: [33.8201, 35.5352] as [number, number],
    description: 'A leading non-profit institution with campuses in Beirut and Byblos.',
    website: 'https://www.lau.edu.lb',
  },
  {
    id: 3,
    name: 'Université Saint-Joseph (USJ)',
    coordinates: [33.8835, 35.5059] as [number, number],
    description: 'A private French-language university founded in 1875.',
    website: 'https://www.usj.edu.lb',
  },
  {
    id: 4,
    name: 'Beirut Arab University (BAU)',
    coordinates: [33.8607, 35.5078] as [number, number],
    description: 'An Arab university offering programs in Arabic, English, and French.',
    website: 'https://www.bau.edu.lb',
  },
  {
    id: 5,
    name: 'Lebanese University (UL)',
    coordinates: [33.8719, 35.5138] as [number, number],
    description: 'The only public university in Lebanon, founded in 1951.',
    website: 'https://www.ul.edu.lb',
  },
  {
    id: 6,
    name: 'Notre Dame University (NDU)',
    coordinates: [34.0007, 35.6499] as [number, number],
    description: 'A Catholic institution with multiple campuses across Lebanon.',
    website: 'https://www.ndu.edu.lb',
  },
  {
    id: 7,
    name: 'Lebanese International University (LIU)',
    coordinates: [33.8422, 35.8394] as [number, number],
    description: 'A private university with multiple campuses across Lebanon.',
    website: 'https://www.liu.edu.lb',
  },
  {
    id: 8,
    name: 'Holy Spirit University of Kaslik (USEK)',
    coordinates: [33.9780, 35.6477] as [number, number],
    description: 'A private Catholic university founded in 1938.',
    website: 'https://www.usek.edu.lb',
  },
  {
    id: 9,
    name: 'Haigazian University',
    coordinates: [33.8800, 35.5100] as [number, number],
    description: 'A private university affiliated with the Union of Armenian Evangelical Churches.',
    website: 'https://www.haigazian.edu.lb',
  },
  {
    id: 10,
    name: 'Antonine University (UA)',
    coordinates: [34.0024, 35.6512] as [number, number],
    description: 'A private Catholic university run by the Antonine Maronite Order.',
    website: 'https://www.ua.edu.lb',
  },
];

const MapPage = () => {
  const { themeName } = useTheme();

  // Apply dark mode styles to Leaflet controls
  useEffect(() => {
    const mapContainer = document.querySelector('.leaflet-container');
    if (mapContainer) {
      if (themeName === 'dark') {
        mapContainer.classList.add('dark-mode-map');
      } else {
        mapContainer.classList.remove('dark-mode-map');
      }
    }
  }, [themeName]);

  // OpenStreetMap tile layer URL (different for light/dark themes)
  const tileLayerUrl = themeName === 'dark'
    ? 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'
    : 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';

  const attribution = themeName === 'dark'
    ? '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
    : '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';

  return (
    <div className="min-h-screen bg-[var(--color-background)]">
      {/* Header Section */}
      <section className="bg-[var(--color-surface)] py-12 px-4 sm:px-6 lg:px-8 border-b border-[var(--color-border)]">
        <div className="container mx-auto max-w-4xl text-center">
          <h1 className="text-4xl font-extrabold tracking-tight text-[var(--color-textPrimary)] sm:text-5xl mb-4">
            {TEXT.map.title}
          </h1>
          <p className="text-xl text-[var(--color-textSecondary)] max-w-2xl mx-auto">
            {TEXT.map.subtitle}
          </p>
        </div>
      </section>

      {/* Map Section */}
      <section className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-[var(--color-surface)] rounded-lg overflow-hidden border border-[var(--color-border)] shadow-lg">
          <div style={{ height: '600px', width: '100%' }}>
            <MapContainer
              center={[33.8547, 35.8623]}
              zoom={9}
              scrollWheelZoom={true}
              style={{ height: '100%', width: '100%' }}
            >
              <TileLayer
                attribution={attribution}
                url={tileLayerUrl}
              />
              {universities.map((university) => (
                <Marker key={university.id} position={university.coordinates}>
                  <Popup className="custom-popup">
                    <div className="p-2">
                      <h3 className="font-bold text-base mb-2">{university.name}</h3>
                      <p className="text-sm mb-2">{university.description}</p>
                      <a
                        href={university.website}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-sm text-blue-600 hover:text-blue-800 underline"
                      >
                        {TEXT.map.visitWebsite}
                      </a>
                    </div>
                  </Popup>
                </Marker>
              ))}
            </MapContainer>
          </div>
        </div>

        {/* Universities List */}
        <div className="mt-8 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {universities.map((university) => (
            <div
              key={university.id}
              className="bg-[var(--color-surface)] border border-[var(--color-border)] rounded-lg p-4 hover:shadow-md transition-shadow"
            >
              <h3 className="font-semibold text-[var(--color-textPrimary)] mb-2">
                {university.name}
              </h3>
              <p className="text-sm text-[var(--color-textSecondary)] mb-3">
                {university.description}
              </p>
              <a
                href={university.website}
                target="_blank"
                rel="noopener noreferrer"
                className="text-sm text-[var(--color-primary)] hover:text-[var(--color-primaryVariant)] underline"
              >
                {TEXT.map.visitWebsite}
              </a>
            </div>
          ))}
        </div>
      </section>

      {/* Dark mode styles for Leaflet */}
      <style>{`
        .dark-mode-map .leaflet-control-attribution {
          background-color: rgba(0, 0, 0, 0.7) !important;
          color: #fff !important;
        }

        .dark-mode-map .leaflet-control-attribution a {
          color: #8ab4f8 !important;
        }

        .dark-mode-map .leaflet-popup-content-wrapper {
          background-color: #2c2c2c !important;
          color: #e0e0e0 !important;
        }

        .dark-mode-map .leaflet-popup-tip {
          background-color: #2c2c2c !important;
        }

        .dark-mode-map .leaflet-control-zoom a {
          background-color: #2c2c2c !important;
          color: #e0e0e0 !important;
        }

        .dark-mode-map .leaflet-control-zoom a:hover {
          background-color: #3c3c3c !important;
        }
      `}</style>
    </div>
  );
};

export default MapPage;
