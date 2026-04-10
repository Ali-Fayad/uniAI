import { useParams } from 'react-router-dom';
import CVBuilderPageShell from './cvBuilder/CVBuilderPageShell';
import { useCVBuilderController } from './cvBuilder/useCVBuilderController';

const CVBuilderPage = () => {
  const { cvId } = useParams<{ cvId: string }>();
  const controller = useCVBuilderController(cvId ? Number(cvId) : null);

  return <CVBuilderPageShell controller={controller} />;
};

export default CVBuilderPage;
