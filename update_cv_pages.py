import re

## 1. Create useProfileCompleteCheck.ts
hook_code = """import { useState, useEffect } from 'react';
import { cvService } from '../services/cv';

export const useProfileCompleteCheck = () => {
  const [isChecking, setIsChecking] = useState(true);
  const [isFilled, setIsFilled] = useState(false);

  useEffect(() => {
    cvService.getPersonalInfoStatus()
      .then(res => {
        setIsFilled(res.isFilled);
        setIsChecking(false);
      })
      .catch(() => {
        setIsFilled(false);
        setIsChecking(false);
      });
  }, []);

  return { isChecking, isFilled };
};
"""
with open("/home/afayad2/uniAI/Client/src/hooks/useProfileCompleteCheck.ts", "w") as f:
    f.write(hook_code)

## 2. Create ProfileIncompleteBanner.tsx
banner_code = """import { useNavigate } from 'react-router-dom';

export const ProfileIncompleteBanner = () => {
  const navigate = useNavigate();
  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/50 z-50">
      <div className="bg-[var(--color-surface)] rounded-xl shadow-lg p-8 max-w-sm w-full mx-4 text-center">
        <h2 className="text-xl font-semibold mb-4 text-[var(--color-textPrimary)]">Profile Incomplete</h2>
        <p className="text-[var(--color-textSecondary)] mb-6">
          Please complete your personal information before accessing CV Builder
        </p>
        <button
          onClick={() => navigate('/personal-info')}
          className="w-full bg-[var(--color-primary)] hover:bg-[var(--color-primaryHover)] text-white font-medium py-2 px-4 rounded-lg"
        >
          Go to Personal Info
        </button>
      </div>
    </div>
  );
};
"""
with open("/home/afayad2/uniAI/Client/src/components/common/ProfileIncompleteBanner.tsx", "w") as f:
    f.write(banner_code)

## 3. Update CVBuilderPage.tsx
with open("/home/afayad2/uniAI/Client/src/components/page/CVBuilderPage.tsx", "r") as f:
    cvbuilder = f.read()

cvbuilder = "import { useProfileCompleteCheck } from '../../hooks/useProfileCompleteCheck';\nimport { ProfileIncompleteBanner } from '../common/ProfileIncompleteBanner';\n" + cvbuilder

# Add hook call
cvbuilder = cvbuilder.replace("const controller = useCVBuilderController", "const { isChecking, isFilled } = useProfileCompleteCheck();\n  const controller = useCVBuilderController")

# Replace return
cvbuilder = cvbuilder.replace("return <CVBuilderPageShell", 
"""if (isChecking) return null;
  if (!isFilled) return <ProfileIncompleteBanner />;
  return <CVBuilderPageShell""")

with open("/home/afayad2/uniAI/Client/src/components/page/CVBuilderPage.tsx", "w") as f:
    f.write(cvbuilder)

## 4. Update CVListPage.tsx
with open("/home/afayad2/uniAI/Client/src/components/page/CVListPage.tsx", "r") as f:
    cvlist = f.read()

cvlist = "import { useProfileCompleteCheck } from '../../hooks/useProfileCompleteCheck';\nimport { ProfileIncompleteBanner } from '../common/ProfileIncompleteBanner';\n" + cvlist

cvlist = cvlist.replace("const CVListPage = () => {", "const CVListPage = () => {\n  const { isChecking, isFilled } = useProfileCompleteCheck();")

cvlist = cvlist.replace("return <CVListPageShell", 
"""if (isChecking) return null;
  if (!isFilled) return <ProfileIncompleteBanner />;
  return <CVListPageShell""")

# Wait, maybe it returns just <div... or <CVList...
import re
cvlist = re.sub(r'return\s*<', 'if (isChecking) return null;\n  if (!isFilled) return <ProfileIncompleteBanner />;\n  return <', cvlist, count=1)

with open("/home/afayad2/uniAI/Client/src/components/page/CVListPage.tsx", "w") as f:
    f.write(cvlist)

print("Done")
