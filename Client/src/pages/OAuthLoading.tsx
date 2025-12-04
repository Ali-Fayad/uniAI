import { useEffect } from 'react';
import { AuthCard } from '../components/AuthCard';

interface OAuthLoadingProps {
  provider: 'Google' | 'GitHub';
}

const OAuthLoading: React.FC<OAuthLoadingProps> = ({ provider }) => {
  
  useEffect(() => {
    // TODO: Handle OAuth callback logic here
    const handleOAuth = async () => {
      console.log(`Processing ${provider} login...`);
    }
    handleOAuth();
  }, [provider]);

  const spinnerColor = provider === "Google" ? "border-red-500" : "border-gray-800";

  return (
    <AuthCard>
      <div className="flex flex-col items-center justify-center py-10">
        <span className={`animate-spin w-12 h-12 border-4 ${spinnerColor} border-t-transparent rounded-full mb-6`}></span>

        <p className="text-[#151514] text-xl font-bold">
          Redirecting with {provider}...
        </p>
        <p className="text-[#797672] text-sm mt-2 text-center">
          Please wait while we log you in.
        </p>
      </div>
    </AuthCard>
  );
};

export default OAuthLoading;