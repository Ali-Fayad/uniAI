import CVListPageShell from './cvList/CVListPageShell';
import { useCVListController } from './cvList/useCVListController';

const CVListPage = () => {
  const controller = useCVListController();

  return <CVListPageShell controller={controller} />;
};

export default CVListPage;
