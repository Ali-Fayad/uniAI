import { useLocation } from 'react-router-dom';
import PersonalInfoPageShell from './personalInfo/PersonalInfoPageShell';
import { usePersonalInfoController } from './personalInfo/usePersonalInfoController';

type PersonalInfoLocationState = {
  fromOnboarding?: boolean;
};

const PersonalInfoPage = () => {
  const location = useLocation();
  const fromOnboarding = Boolean((location.state as PersonalInfoLocationState | null)?.fromOnboarding);

  const controller = usePersonalInfoController({ fromOnboarding });

  return <PersonalInfoPageShell controller={controller} />;
};

export default PersonalInfoPage;
