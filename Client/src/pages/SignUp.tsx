import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthCard } from "../components/AuthCard";
import { LuEye, LuEyeOff } from "react-icons/lu";
import { hashPassword } from "../utils/hash";
import type { SignUpDto } from "../utils/auth";
import { AuthService } from "../api/AuthService";
import { TokenStorage } from "../utils/storage";

const SignUp = () => {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!firstName || !lastName || !username || !email || !password || !confirmPassword) {
      setError("All fields are required");
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    setLoading(true);
    try {
      const dto: SignUpDto = {
        firstName:  firstName.trim(),
        lastName: lastName.trim(),
        username: username.trim(),
        email: email.trim().toLowerCase(),
        password: await hashPassword(password),
      };

      const response = await AuthService.signUp(dto);

      // Successful signup with token -> navigate to chat
      if (response. status === 200 && response.token) {
        TokenStorage.saveToken(response.token);
        navigate("/chat");
        return;
      }

      // Needs verification (202) -> save email and navigate to verification page
      if (response.status === 202) {
        TokenStorage.saveEmailForVerification(dto.email);
        navigate("/auth/verification");
        return;
      }

      // Any other response -> show backend message if present
      setError(response.message || "Signup failed. Please try again.");
    } catch (err) {
      console.error("Signup failed:", err);
      setError("An unexpected error occurred. Please try again.");
    } finally {
      setLoading(false);
    }
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
        <div className="grid grid-cols-2 gap-4">
          <input
            type="text"
            placeholder="First Name"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514]"
            required
          />
          <input
            type="text"
            placeholder="Last Name"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514]"
            required
          />afayad123
        </div>

        <input
          type="email"
          placeholder="Email Address"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514]"
          required
        />

        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514]"
          required
        />

        <div className="relative">
          <input
            type={showPassword ? "text" : "password"}
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target. value)}
            className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514] pr-14"
            required
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-4 top-1/2 -translate-y-1/2 text-[#B3AB9C] hover: text-[#a69d8f] bg-transparent p-0 border-0 transition-colors"
          >
            {showPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
          </button>
        </div>

        <div className="relative">
          <input
            type={showConfirmPassword ? "text" :  "password"}
            placeholder="Confirm Password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e. target.value)}
            className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514] pr-14"
            required
          />
          <button
            type="button"
            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            className="absolute right-4 top-1/2 -translate-y-1/2 text-[#B3AB9C] hover:text-[#a69d8f] bg-transparent p-0 border-0 transition-colors"
          >
            {showConfirmPassword ? <LuEyeOff size={22} /> :  <LuEye size={22} />}
          </button>
        </div>

        {error && <p className="text-red-500 text-sm">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="flex w-full items-center justify-center rounded-full h-12 bg-custom-primary text-[#151514] font-bold hover: bg-[#a69d8f] transition disabled:opacity-50"
        >
          {loading ? "Creating..." : "Create Account"}
        </button>
      </form>

      <p className="text-center text-[#797672] text-sm mt-6">
        Already have an account?{" "}
        <button
          className="text-[#B3AB9C] font-medium hover: text-[#a69d8f] transition-colors ml-1 bg-transparent p-0 border-0"
          onClick={() => navigate("/auth/signin")}
        >
          Sign in
        </button>
      </p>
    </AuthCard>
  );
};

export default SignUp;
