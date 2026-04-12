import { useNavigate } from 'react-router-dom';

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
