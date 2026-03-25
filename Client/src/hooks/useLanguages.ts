import { useEffect, useState } from 'react';
import type { LanguageCatalogDto } from '../types/dto';
import { cvService } from '../services/cv';

export const useLanguages = (search: string) => {
  const [items, setItems] = useState<LanguageCatalogDto[]>([]);
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
        const response = await cvService.getLanguages(query);
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
