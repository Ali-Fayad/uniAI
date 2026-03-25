import { useEffect, useState } from 'react';
import type { PositionCatalogDto } from '../types/dto';
import { cvService } from '../services/cv';

export const usePositions = (search: string) => {
  const [items, setItems] = useState<PositionCatalogDto[]>([]);
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
        const response = await cvService.getPositions(query);
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
