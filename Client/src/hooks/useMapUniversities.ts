import { useEffect, useState } from 'react';
import { cvService } from '../services/cv';
import {
  mapUniversityCatalog,
  type MapUniversity,
} from '../components/page/map/mapUniversityMapper';

interface UseMapUniversitiesResult {
  universities: MapUniversity[];
  isLoading: boolean;
  error: string | null;
}

export const useMapUniversities = (): UseMapUniversitiesResult => {
  const [universities, setUniversities] = useState<MapUniversity[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    const loadUniversities = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const catalog = await cvService.getUniversities();
        if (active) {
          setUniversities(mapUniversityCatalog(catalog));
        }
      } catch {
        if (active) {
          setUniversities([]);
          setError('Failed to load university locations. Please try again.');
        }
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    };

    void loadUniversities();

    return () => {
      active = false;
    };
  }, []);

  return { universities, isLoading, error };
};
