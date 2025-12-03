import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthCard } from "../components/AuthCard";
import { LuEye, LuEyeOff } from "react-icons/lu";

const SignUp = () => {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    console.log("Signing up...");
  };

  return (
    <AuthCard>
      <div className="flex flex-col gap-2 mb-8 pt-8">
        <p className="text-[#151514] text-4xl font-black tracking-[-0.033em]">
          Create Account
        </p>
        <p className="text-[#797672]">Join and save your chats securely.</p>
      </div>

      <form className="space-y-6" onSubmit={handleSignup}>
        {/* Name fields */}
        <div className="grid grid-cols-2 gap-4">
          <input
            type="text"
            placeholder="First Name"
            className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514]"
          />
          <input
            type="text"
            placeholder="Last Name"
            className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514]"
          />
        </div>

        {/* Email */}
        <input
          type="email"
          placeholder="Email Address"
          className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514]"
        />

        {/* Username */}
        <input
          type="text"
          placeholder="Username"
          className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514]"
        />

        {/* Password */}
        <div className="relative">
          <input
            type={showPassword ? "text" : "password"}
            placeholder="Password"
            className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514] pr-14"
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-4 top-1/2 -translate-y-1/2 text-[#B3AB9C] hover:text-[#a69d8f] bg-transparent p-0 border-0 transition-colors"
          >
            {showPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
          </button>
        </div>

        {/* Confirm Password */}
        <div className="relative">
          <input
            type={showConfirmPassword ? "text" : "password"}
            placeholder="Confirm Password"
            className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514] pr-14"
          />
          <button
            type="button"
            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            className="absolute right-4 top-1/2 -translate-y-1/2 text-[#B3AB9C] hover:text-[#a69d8f] bg-transparent p-0 border-0 transition-colors"
          >
            {showConfirmPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
          </button>
        </div>

        {/* Create Account button as plain text */}
        <button
          type="submit"
          className="flex w-full items-center justify-center rounded-full h-12 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition"
        >
          Create Account
        </button>
      </form>

      <p className="text-center text-[#797672] text-sm mt-6">
        Already have an account?{" "}
        <button
          className="text-[#B3AB9C] font-medium hover:text-[#a69d8f] transition-colors ml-1 bg-transparent p-0 border-0"
          onClick={() => navigate("/auth/signin")}
        >
          Sign in
        </button>
      </p>
    </AuthCard>
  );
};

export default SignUp;
