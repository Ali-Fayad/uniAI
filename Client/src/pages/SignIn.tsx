import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthCard } from "../components/AuthCard";
import { LuEye, LuEyeOff } from "react-icons/lu";
import { hashPassword } from "../utils/hash";
import type { SignInDto } from "../utils/auth";
import { AuthService } from "../api/AuthService";
import { TokenStorage } from "../utils/storage";

const SignIn = () => {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!email.trim() || !password) {
      setError("Email and password are required.");
      return;
    }

    const loginData: SignInDto = {
      email: email.trim().toLowerCase(),
      password: await hashPassword(password),
    };

    try {
      const response = await AuthService.signIn(loginData);

      // status 200 → token available → go to dashboard
      if (response.status === 200 && response.token) {
        TokenStorage.saveToken(response.token);
        navigate("/dashboard");
      }
      // status 202 → email not verified → go to verification page
      else if (response.status === 202) {
        // Save the email temporarily for verification
        TokenStorage.saveEmailForVerification(email.trim().toLowerCase());
        navigate("/auth/verify");
      }
      // any other status
      else {
        setError(response.message || "Login failed. Please try again.");
      }
    } catch (err) {
      console.error("Login failed", err);
      setError("An unexpected error occurred. Please try again.");
    }
  };

  return (
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
        {/* EMAIL FIELD */}
        <div className="flex flex-col">
          <label className="flex flex-col w-full pb-2">
            <p className="text-[#151514] font-medium pb-2">Email Address</p>
            <input
              type="email"
              placeholder="Enter your email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white h-14 px-[15px] text-[#151514]"
              required
            />
          </label>
        </div>

        {/* PASSWORD FIELD */}
        <div className="flex flex-col w-full">
          <div className="flex justify-between items-center pb-2">
            <p className="text-[#151514] font-medium">Password</p>
          </div>

          <div className="relative">
            <input
              type={showPassword ? "text" : "password"}
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white h-14 px-[15px] text-[#151514] pr-14"
              required
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

        {error && <p className="text-red-500 text-sm">{error}</p>}

        {/* SIGN IN BUTTON */}
        <div>
          <button className="flex w-full items-center justify-center rounded-full h-12 px-5 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition">
            Sign In
          </button>
        </div>
      </form>

      {/* Footer */}
      <p className="text-center text-[#797672] text-sm mt-6">
        Don’t have an account?{" "}
        <button
          className="text-[#B3AB9C] font-medium hover:text-[#a69d8f] transition-colors ml-1 bg-transparent p-0"
          onClick={() => navigate("/auth/signup")}
        >
          Create one
        </button>
      </p>
    </AuthCard>
  );
};

export default SignIn;
