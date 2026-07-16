import { useLocation, useNavigate, useParams } from 'react-router-dom';
import CVBuilderPageShell from './cvBuilder/CVBuilderPageShell';
import { useCVBuilderController } from './cvBuilder/useCVBuilderController';
import ProfileIncompleteDialog from './cvBuilder/ProfileIncompleteDialog';
import LoadingSpinner from '../common/LoadingSpinner';
import { ROUTES } from '../../router';

const CVBuilderPage = () => {
  const { cvId } = useParams<{ cvId: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const controller = useCVBuilderController(cvId ? Number(cvId) : null);
  const isProfilePromptOpen = !controller.isLoading && !controller.isProfileComplete;

  const handleCompleteProfile = () => {
    const returnTo = `${location.pathname}${location.search}`;
    navigate(`${ROUTES.PERSONAL_INFO}?returnTo=${encodeURIComponent(returnTo)}`);
  };

  if (controller.isLoading) {
    return (
      <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-[var(--color-background)]">
        <LoadingSpinner text="Loading CV builder..." />
      </div>
    );
  }

  return (
    <>
      <ProfileIncompleteDialog
        open={isProfilePromptOpen}
        onConfirm={handleCompleteProfile}
      />
      {controller.isProfileComplete && (
        <CVBuilderPageShell controller={controller} />
      )}
    </>
  );
};

export default CVBuilderPage;
