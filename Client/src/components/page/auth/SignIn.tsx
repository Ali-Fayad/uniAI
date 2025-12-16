import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthCard } from "../../../components/AuthCard";
import { LuEye, LuEyeOff } from "react-icons/lu";
import { authService } from "../../../services/auth";
import { useAuth } from "../../../hooks/useAuth";
import type { SignInDto } from "../../../types/dto";

const SignIn = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      const data: SignInDto = { email, password };
      const response = await authService.signIn(data);

      // Store token and user data
      login(response.token);

      // Redirect to chat page
      navigate('/chat');
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { status?: number; data?: { message?: string } } };

        // Handle 202 status (verification required)
        if (axiosError.response?.status === 202) {
          navigate('/verify', { state: { email } });
          return;
        }

        setError(axiosError.response?.data?.message || 'Failed to sign in. Please check your credentials.');
      } else {
        setError('An unexpected error occurred.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-custom-light py-12 px-4">
      <AuthCard>
        <div className="flex flex-col gap-2 mb-8 pt-8">
          <p className="text-[#151514] text-4xl font-black tracking-[-0.033em]">
            Welcome Back
          </p>
          <p className="text-[#797672] text-base">
            Sign in to continue to your dashboard.
          </p>
        </div>

        <form className="space-y-6" onSubmit={handleLogin}>
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}
          
          {/* EMAIL FIELD */}
          <div className="flex flex-col">
            <label className="flex flex-col w-full pb-2">
              <p className="text-[#151514] font-medium pb-2">Email Address</p>
              <input
                type="email"
                placeholder="Enter your email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white h-14 px-[15px] text-[#151514]"
              />
            </label>
          </div>

          {/* PASSWORD FIELD */}
          <div className="flex flex-col w-full">

            {/* Label + Forgot Password */}
            <div className="flex justify-between items-center pb-2">
              <p className="text-[#151514] font-medium">Password</p>

              <button
                type="button"
                onClick={() => navigate("/forgot-password")}
                className="text-[#B3AB9C] text-sm font-medium hover:text-[#a69d8f] transition-colors bg-transparent p-0"
              >
                Forgot Password?
              </button>
            </div>

            {/* Input + Toggle Icon */}
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white h-14 px-[15px] text-[#151514] pr-14"
              />

              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-[#B3AB9C] hover:text-[#a69d8f] transition-colors bg-transparent p-0"
              >
                {showPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
              </button>
            </div>
          </div>

          {/* SIGN IN BUTTON */}
          <div>
            <button 
              type="submit"
              disabled={isLoading}
              className="flex w-full items-center justify-center rounded-full h-12 px-5 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Signing In...' : 'Sign In'}
            </button>
          </div>
        </form>

        <p className="text-center text-[#797672] text-sm mt-6">
          Don’t have an account?{" "}
          <button
            type="button"
            className="text-[#B3AB9C] font-medium hover:text-[#a69d8f] transition-colors ml-1 bg-transparent p-0"
            onClick={() => navigate("/signup")}
          >
            Create one
          </button>
        </p>
      </AuthCard>
    </div>
  );
};

export default SignIn;
