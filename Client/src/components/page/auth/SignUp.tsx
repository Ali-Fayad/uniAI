import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { useAuth } from "../../../hooks/useAuth";
import { AuthCard } from "../../../components/AuthCard";
import AuthHeading from "../../../components/auth/AuthHeading";
import { StaggerContainer, staggerItemVariants } from "../../../components/animations";
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
        <div className="pt-8">
          <AuthHeading 
            title={TEXT.auth.signUp.title}
            subtitle={TEXT.auth.signUp.subtitle}
          />
        </div>

        <StaggerContainer staggerDelay={0.06} initialDelay={0.1}>
          <form className="space-y-6" onSubmit={handleSignup}>
            {error && (
              <motion.div 
                variants={staggerItemVariants}
                className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm"
              >
                {error}
              </motion.div>
            )}

            {/* Username */}
            <motion.input
              variants={staggerItemVariants}
              type="text"
              placeholder={TEXT.auth.signUp.usernamePlaceholder}
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)]"
            />

            {/* Name fields */}
            <motion.div variants={staggerItemVariants} className="grid grid-cols-2 gap-4">
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
            </motion.div>

            {/* Email */}
            <motion.input
              variants={staggerItemVariants}
              type="email"
              placeholder={TEXT.auth.signUp.emailPlaceholder}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)]"
            />

            {/* Password */}
            <motion.div variants={staggerItemVariants} className="relative">
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
            </motion.div>

            {/* Confirm Password */}
            <motion.div variants={staggerItemVariants} className="relative">
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
            </motion.div>

            {/* Create Account button */}
            <motion.div variants={staggerItemVariants}>
              <motion.button
                type="submit"
                disabled={isLoading}
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                className="flex w-full items-center justify-center rounded-full h-12 bg-[var(--color-primary)] text-[var(--color-background)] font-bold hover:bg-[var(--color-primaryVariant)] transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? TEXT.auth.signUp.submitButtonLoading : TEXT.auth.signUp.submitButton}
              </motion.button>
            </motion.div>
          </form>

          <motion.p 
            variants={staggerItemVariants}
            className="text-center text-[var(--color-textSecondary)] text-sm mt-6"
          >
            {TEXT.auth.signUp.haveAccount}{" "}
            <button
              type="button"
              className="text-[var(--color-primaryVariant)] font-medium hover:text-[var(--color-primary)] transition-colors ml-1 bg-transparent p-0 border-0"
              onClick={() => navigate(ROUTES.SIGN_IN)}
            >
              {TEXT.auth.signUp.signInLink}
            </button>
          </motion.p>
        </StaggerContainer>
      </AuthCard>
    </div>
  );
};

export default SignUp;
