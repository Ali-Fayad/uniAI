import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { cvService } from '../../../services/cv';
import { ROUTES } from '../../../router';
import type { CVDto } from '../../../types/dto';

export interface UseCVListControllerReturn {
  isLoading: boolean;
  error: string | null;
  cvs: CVDto[];
  createNew: () => void;
  editCv: (cvId: number) => void;
  deleteCv: (cvId: number) => Promise<void>;
  setDefaultCv: (cvId: number) => Promise<void>;
}

export const useCVListController = (): UseCVListControllerReturn => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [cvs, setCvs] = useState<CVDto[]>([]);

  const loadCVs = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await cvService.getCVs();
      setCvs(response);
    } catch {
      setError('Failed to load your saved CVs. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void loadCVs();
  }, []);

  const createNew = () => {
    navigate(ROUTES.CV_BUILDER);
  };

  const editCv = (cvId: number) => {
    navigate(`${ROUTES.CV_BUILDER}/${cvId}`);
  };

  const deleteCv = async (cvId: number) => {
    if (!window.confirm('Delete this CV configuration?')) {
      return;
    }

    try {
      await cvService.deleteCV(cvId);
      await loadCVs();
    } catch {
      setError('Failed to delete CV. Please try again.');
    }
  };

  const setDefaultCv = async (cvId: number) => {
    try {
      await cvService.updateCV(cvId, { isDefault: true });
      await loadCVs();
    } catch {
      setError('Failed to set default CV. Please try again.');
    }
  };

  return {
    isLoading,
    error,
    cvs,
    createNew,
    editCv,
    deleteCv,
    setDefaultCv,
  };
};
