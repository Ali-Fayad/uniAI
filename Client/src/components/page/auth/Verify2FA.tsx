import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { AuthCard } from "../../../components/AuthCard";
import { authService } from "../../../services/auth";
import { useAuth } from "../../../hooks/useAuth";
import type { VerifyDto } from "../../../types/dto";

const Verify2FA = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [code, setCode] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  
  // Get email from navigation state
  const email = (location.state as { email?: string })?.email || "";

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email) {
      setError("Email is required. Please sign in again.");
      return;
    }
    
    setError("");
    setIsLoading(true);

    try {
      const data: VerifyDto = { email, code };
      const response = await authService.verify2FA(data);
      
      // Store token and user data
      login(response.token, response.user);
      
      // Redirect to chat page
      navigate('/chat');
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        setError(axiosError.response?.data?.message || 'Invalid 2FA code. Please try again.');
      } else {
        setError('An unexpected error occurred.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthCard>
      <div className="flex flex-col gap-2 mb-8 pt-8">
        <p className="text-[#151514] text-4xl font-black tracking-[-0.033em]">
          Two-Factor Authentication
        </p>
        <p className="text-[#797672]">
          Enter the 6-digit code from your authenticator app.
        </p>
      </div>

      <form className="space-y-6" onSubmit={handleVerify}>
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm">
            {error}
          </div>
        )}
        
        <div className="flex flex-col">
          <label className="flex flex-col w-full">
            <p className="text-[#151514] font-medium pb-2">Code</p>
            <input
              type="text"
              placeholder="123456"
              value={code}
              onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
              required
              maxLength={6}
              className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514] tracking-widest text-lg"
            />
          </label>
        </div>

        <button
          type="submit"
          disabled={isLoading || code.length !== 6}
          className="flex w-full items-center justify-center rounded-full h-12 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isLoading ? 'Verifying...' : 'Verify'}
        </button>
      </form>

      <p className="text-center text-[#797672] text-sm mt-6">
        <button
          type="button"
          className="text-[#B3AB9C] font-medium hover:text-[#a69d8f] transition-colors bg-transparent p-0 border-0"
          onClick={() => navigate('/signin')}
        >
          Back to Sign In
        </button>
      </p>
    </AuthCard>
  );
};

export default Verify2FA;
