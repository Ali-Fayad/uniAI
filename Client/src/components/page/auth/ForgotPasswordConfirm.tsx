import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { AuthCard } from "../../../components/AuthCard";
import { LuEye, LuEyeOff } from "react-icons/lu";
import { authService } from "../../../services/auth";
import { useAuth } from "../../../hooks/useAuth";
import type { RequestPasswordDto } from "../../../types/dto";

const ForgotPasswordConfirm = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [code, setCode] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  
  // Get email from navigation state
  const email = (location.state as { email?: string })?.email || "";

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email) {
      setError("Email is required. Please start the reset process again.");
      return;
    }
    
    if (newPassword !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    
    setError("");
    setIsLoading(true);

    try {
      const data: RequestPasswordDto = { email, code, newPassword };
      const response = await authService.forgotPasswordConfirm(data);
      
      // Store token and user data
      login(response.token, response.user);
      
      // Redirect to chat page
      navigate('/chat');
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        setError(axiosError.response?.data?.message || 'Failed to reset password. Please try again.');
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
          Reset Password
        </p>
        <p className="text-[#797672]">
          Enter the code and your new password.
        </p>
      </div>

      <form className="space-y-6" onSubmit={handleResetPassword}>
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm">
            {error}
          </div>
        )}
        
        <div className="flex flex-col">
          <label className="flex flex-col w-full">
            <p className="text-[#151514] font-medium pb-2">Reset Code</p>
            <input
              type="text"
              placeholder="123456"
              value={code}
              onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
              required
              maxLength={6}
              className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514] tracking-widest"
            />
          </label>
        </div>

        <div className="relative">
          <label className="flex flex-col w-full">
            <p className="text-[#151514] font-medium pb-2">New Password</p>
            <input
              type={showPassword ? "text" : "password"}
              placeholder="Enter new password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              minLength={8}
              className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514] pr-14"
            />
          </label>
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-4 top-[42px] text-[#B3AB9C] hover:text-[#a69d8f] transition-colors bg-transparent p-0"
          >
            {showPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
          </button>
        </div>

        <div className="relative">
          <label className="flex flex-col w-full">
            <p className="text-[#151514] font-medium pb-2">Confirm Password</p>
            <input
              type={showConfirmPassword ? "text" : "password"}
              placeholder="Confirm new password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              minLength={8}
              className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514] pr-14"
            />
          </label>
          <button
            type="button"
            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            className="absolute right-4 top-[42px] text-[#B3AB9C] hover:text-[#a69d8f] transition-colors bg-transparent p-0"
          >
            {showConfirmPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
          </button>
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="flex w-full items-center justify-center rounded-full h-12 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isLoading ? 'Resetting...' : 'Reset Password'}
        </button>
      </form>

      <p className="text-center text-[#797672] text-sm mt-6">
        Remember your password?{" "}
        <button
          type="button"
          className="text-[#B3AB9C] font-medium hover:text-[#a69d8f] transition-colors ml-1 bg-transparent p-0 border-0"
          onClick={() => navigate("/signin")}
        >
          Sign in
        </button>
      </p>
    </AuthCard>
  );
};

export default ForgotPasswordConfirm;
