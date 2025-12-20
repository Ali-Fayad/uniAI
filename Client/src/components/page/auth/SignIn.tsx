import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthCard } from "../../../components/AuthCard";
import { LuEye, LuEyeOff } from "react-icons/lu";
import { authService } from "../../../services/auth";
import { sha256Hex } from "../../../utils/hash";
import validation from "../../../utils/validation";
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
      // Trim and lowercase email per rules
      const formattedEmail = validation.toLower(email);

      // Validate inputs client-side per validation_rules.MD
      if (!validation.isValidEmail(formattedEmail)) {
        setError("Please enter a valid email address.");
        setIsLoading(false);
        return;
      }

      if (!validation.isValidRawPassword(password)) {
        setError(
          "Password must be at least 8 characters, include one uppercase letter and one number."
        );
        setIsLoading(false);
        return;
      }

      // Hash password with SHA-256 before submitting
      const hashed = await sha256Hex(password);

      const data: SignInDto = {
        email: formattedEmail,
        password: hashed,
      } as any;
      const response = await authService.signIn(data);

      // Store token and user data
      login(response.token);

      // Redirect to chat page
      navigate("/chat");
    } catch (err: unknown) {
      const anyErr = err as any;

      // Handle 2FA and verification flows by status if provided
      const status = anyErr?.status || anyErr?.response?.status;
      if (status === 401) {
        navigate("/2fa/verify", { state: { email } });
        setIsLoading(false);
        return;
      }

      if (status === 202) {
        navigate("/verify", { state: { email } });
        setIsLoading(false);
        return;
      }

      // Prefer server-provided message
      const serverMessage =
        anyErr?.message ||
        anyErr?.response?.data ||
        anyErr?.response?.data?.message;
      setError(
        serverMessage || "Failed to sign in. Please check your credentials."
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-[var(--color-background)] py-12 px-4">
      <AuthCard>
        <div className="flex flex-col gap-2 mb-8 pt-8">
          <p className="text-[var(--color-textPrimary)] text-4xl font-black tracking-[-0.033em]">
            Welcome Back
          </p>
          <p className="text-[var(--color-textSecondary)] text-base">
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
              <p className="text-[var(--color-textPrimary)] font-medium pb-2">
                Email Address
              </p>
              <input
                type="email"
                placeholder="Enter your email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)]"
              />
            </label>
          </div>

          {/* PASSWORD FIELD */}
          <div className="flex flex-col w-full">
            {/* Label + Forgot Password */}
            <div className="flex justify-between items-center pb-2">
              <p className="text-[var(--color-textPrimary)] font-medium">
                Password
              </p>

              <button
                type="button"
                onClick={() => navigate("/forgot-password")}
                className="text-[var(--color-primaryVariant)] text-sm font-medium hover:text-[var(--color-primary)] transition-colors bg-transparent p-0"
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
                className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)] pr-14"
              />

              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-[var(--color-primaryVariant)] hover:text-[var(--color-primary)] transition-colors bg-transparent p-0"
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
              className="flex w-full items-center justify-center rounded-full h-12 px-5 bg-[var(--color-primary)] text-[var(--color-background)] font-bold hover:bg-[var(--color-primaryVariant)] transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? "Signing In..." : "Sign In"}
            </button>
          </div>
        </form>

        <p className="text-center text-[var(--color-textSecondary)] text-sm mt-6">
          Don’t have an account?{" "}
          <button
            type="button"
            className="text-[var(--color-primaryVariant)] font-medium hover:text-[var(--color-primary)] transition-colors ml-1 bg-transparent p-0"
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
