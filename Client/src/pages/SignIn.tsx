import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthCard } from "../components/AuthCard";
import { LuEye, LuEyeOff } from "react-icons/lu";

const SignIn = () => {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    console.log("Logging in...");
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
              onClick={() => navigate("/auth/forgot-password")}
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
          <button className="flex w-full items-center justify-center rounded-full h-12 px-5 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition">
            Sign In
          </button>
        </div>
      </form>

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
