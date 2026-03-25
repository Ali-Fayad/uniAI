import { useEffect, useState } from 'react';
import type { UniversityCatalogDto } from '../types/dto';
import { cvService } from '../services/cv';

export const useUniversities = (search: string) => {
  const [items, setItems] = useState<UniversityCatalogDto[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const query = search.trim();
    if (!query) {
      setItems([]);
      return;
    }

    const timeout = setTimeout(async () => {
      setIsLoading(true);
      try {
        const response = await cvService.getUniversities(query);
        setItems(response.slice(0, 8));
      } catch {
        setItems([]);
      } finally {
        setIsLoading(false);
      }
    }, 300);

    return () => clearTimeout(timeout);
  }, [search]);

  return { items, isLoading };
};
