import { useNavigate } from 'react-router-dom';
import { AuthCard } from '../components/AuthCard';

const DefaultView = () => {
  const navigate = useNavigate();

  return (
    <AuthCard>
      <div className="flex flex-col gap-2 mb-8 pt-4">
        <p className="text-[#151514] text-4xl font-black tracking-[-0.033em]">
          Welcome Back
        </p>
        <p className="text-[#797672]">Sign in to continue to your dashboard.</p>
      </div>

      <button
        type="button"
        onClick={() => navigate('/auth/signin')}
        className="flex w-full items-center justify-center rounded-full h-12 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition"
      >
        Sign In
      </button>

      <div className="relative my-8">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-custom-secondary/50"></div>
        </div>
        <div className="relative flex justify-center text-sm">
          <span className="bg-white/50 px-2 text-[#797672]">or</span>
        </div>
      </div>

      <button
        type="button"
        onClick={() => navigate('/auth/signup')}
        className="flex w-full items-center justify-center rounded-full h-12 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition"
      >
        Sign Up
      </button>

      <div className="relative my-8">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-custom-secondary/50"></div>
        </div>
        <div className="relative flex justify-center text-sm">
          <span className="bg-white/50 px-2 text-[#797672]">or</span>
        </div>
      </div>

      <div className="space-y-4">
        {/* Added explicit text color text-[#151514] to ensure visibility */}
        <button
          onClick={() => navigate('/auth/google')}
          className="flex w-full items-center justify-center rounded-full border border-custom-secondary/50 bg-white h-12 text-[#151514] font-medium hover:bg-gray-50 transition"
        >
          Google
        </button>

        <button
          onClick={() => navigate('/auth/github')}
          className="flex w-full items-center justify-center rounded-full border border-custom-secondary/50 bg-white h-12 text-[#151514] font-medium hover:bg-gray-50 transition"
        >
          GitHub
        </button>
      </div>
    </AuthCard>
  );
};

export default DefaultView;