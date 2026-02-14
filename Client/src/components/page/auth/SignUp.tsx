import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../hooks/useAuth";
import { AuthCard } from "../../../components/AuthCard";
import { LuEye, LuEyeOff } from "react-icons/lu";
import { authService } from "../../../services/auth";
import { TEXT } from "../../../constants/static";
import { ROUTES } from "../../../router";
import type { SignUpDto } from "../../../types/dto";

const SignUp = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [username, setUsername] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    // Validate password match
    if (password !== confirmPassword) {
      setError(TEXT.auth.signUp.errors.passwordMismatch);
      return;
    }

    setIsLoading(true);

    try {
      const data: SignUpDto = { username, firstName, lastName, email, password };
      const response = await authService.signUp(data);

      // If server returned a token, log in and redirect to chat
      if (response?.token) {
        login(response.token);
        navigate(ROUTES.CHAT);
        return;
      }

      // Otherwise (e.g. verification required), navigate to verification page
      navigate(ROUTES.VERIFY, { state: { email } });
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { status?: number; data?: { message?: string } } };

        // Handle 202 status (verification required)
        if (axiosError.response?.status === 202) {
          navigate(ROUTES.VERIFY, { state: { email } });
          return;
        }

        setError(axiosError.response?.data?.message || TEXT.auth.signUp.errors.signUpFailed);
      } else {
        setError(TEXT.common.error);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-[var(--color-background)] py-12 px-4">
      <AuthCard>
        <div className="flex flex-col gap-2 mb-8 pt-8">
          <p className="text-[var(--color-textPrimary)] text-4xl font-black tracking-[-0.033em]">
            {TEXT.auth.signUp.title}
          </p>
          <p className="text-[var(--color-textSecondary)]">{TEXT.auth.signUp.subtitle}</p>
        </div>

        <form className="space-y-6" onSubmit={handleSignup}>
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          {/* Username */}
          <input
            type="text"
            placeholder={TEXT.auth.signUp.usernamePlaceholder}
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)]"
          />

          {/* Name fields */}
          <div className="grid grid-cols-2 gap-4">
            <input
              type="text"
              placeholder={TEXT.auth.signUp.firstNamePlaceholder}
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              required
              className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)]"
            />
            <input
              type="text"
              placeholder={TEXT.auth.signUp.lastNamePlaceholder}
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required
              className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)]"
            />
          </div>

          {/* Email */}
          <input
            type="email"
            placeholder={TEXT.auth.signUp.emailPlaceholder}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)]"
          />

          {/* Password */}
          <div className="relative">
            <input
              type={showPassword ? "text" : "password"}
              placeholder={TEXT.auth.signUp.passwordPlaceholder}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={8}
              className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)] pr-14"
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-4 top-1/2 -translate-y-1/2 text-[var(--color-primaryVariant)] hover:text-[var(--color-primary)] bg-transparent p-0 border-0 transition-colors"
            >
              {showPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
            </button>
          </div>

          {/* Confirm Password */}
          <div className="relative">
            <input
              type={showConfirmPassword ? "text" : "password"}
              placeholder={TEXT.auth.signUp.confirmPasswordPlaceholder}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              minLength={8}
              className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)] pr-14"
            />
            <button
              type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              className="absolute right-4 top-1/2 -translate-y-1/2 text-[var(--color-primaryVariant)] hover:text-[var(--color-primary)] bg-transparent p-0 border-0 transition-colors"
            >
              {showConfirmPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
            </button>
          </div>

          {/* Create Account button as plain text */}
          <button
            type="submit"
            disabled={isLoading}
            className="flex w-full items-center justify-center rounded-full h-12 bg-[var(--color-primary)] text-[var(--color-background)] font-bold hover:bg-[var(--color-primaryVariant)] transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? TEXT.auth.signUp.submitButtonLoading : TEXT.auth.signUp.submitButton}
          </button>
        </form>

        <p className="text-center text-[var(--color-textSecondary)] text-sm mt-6">
          {TEXT.auth.signUp.haveAccount}{" "}
          <button
            type="button"
            className="text-[var(--color-primaryVariant)] font-medium hover:text-[var(--color-primary)] transition-colors ml-1 bg-transparent p-0 border-0"
            onClick={() => navigate(ROUTES.SIGN_IN)}
          >
            {TEXT.auth.signUp.signInLink}
          </button>
        </p>
      </AuthCard>
    </div>
  );
};

export default SignUp;
