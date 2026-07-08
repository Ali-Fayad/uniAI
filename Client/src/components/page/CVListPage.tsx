import CVListPageShell from './cvList/CVListPageShell';
import { useCVListController } from './cvList/useCVListController';
import { useProfileCompleteCheck } from '../../hooks/useProfileCompleteCheck';
import { ProfileIncompleteBanner } from '../common/ProfileIncompleteBanner';

const CVListPage = () => {
  const { isChecking, isFilled } = useProfileCompleteCheck();
  const controller = useCVListController();

  if (isChecking) return null;
  if (!isFilled) return <ProfileIncompleteBanner />;

  return <CVListPageShell controller={controller} />;
};

export default CVListPage;
