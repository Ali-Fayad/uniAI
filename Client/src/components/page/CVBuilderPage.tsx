import { useParams } from 'react-router-dom';
import CVBuilderPageShell from './cvBuilder/CVBuilderPageShell';
import { useCVBuilderController } from './cvBuilder/useCVBuilderController';
import { useProfileCompleteCheck } from '../../hooks/useProfileCompleteCheck';
import { ProfileIncompleteBanner } from '../common/ProfileIncompleteBanner';

const CVBuilderPage = () => {
  const { cvId } = useParams<{ cvId: string }>();
  const { isChecking, isFilled } = useProfileCompleteCheck();
  const controller = useCVBuilderController(cvId ? Number(cvId) : null);

  if (isChecking) return null;
  if (!isFilled) return <ProfileIncompleteBanner />;

  return <CVBuilderPageShell controller={controller} />;
};

export default CVBuilderPage;
