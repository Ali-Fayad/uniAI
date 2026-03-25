import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { AuthCard } from "../../../components/AuthCard";
import AuthHeading from "../../../components/auth/AuthHeading";
import { StaggerContainer, staggerItemVariants } from "../../../components/animations";
import { LuEye, LuEyeOff } from "react-icons/lu";
import { ImSpinner } from 'react-icons/im';
import { FaCheckCircle, FaTimesCircle } from 'react-icons/fa';
import { AiOutlineWarning } from 'react-icons/ai';
import { useSignUp } from "../../../hooks/useSignUp";
import { TEXT } from "../../../constants/static";
import { ROUTES } from "../../../router";

const SignUp = () => {
  const navigate = useNavigate();
  const {
    username,
    firstName,
    lastName,
    email,
    password,
    confirmPassword,
    isLoading,
    error,
    emailAvailabilityMessage,
    emailCheckStatus,
    canSubmit,
    showPassword,
    showConfirmPassword,
    setUsername,
    setFirstName,
    setLastName,
    setEmail,
    setPassword,
    setConfirmPassword,
    setShowPassword,
    setShowConfirmPassword,
    handleSignup,
  } = useSignUp();

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
            <motion.div variants={staggerItemVariants} className="relative group">
              <input
                type="email"
                placeholder="Email address"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                aria-describedby="email-check-status"
                className="form-input w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] pr-14 text-[var(--color-textPrimary)]"
              />

              {emailCheckStatus !== 'idle' && (
                <span className="absolute inset-y-0 right-4 flex items-center">
                  {emailCheckStatus === 'checking' && (
                    <span
                      role="status"
                      aria-label="Checking availability"
                      tabIndex={0}
                      className="text-[var(--color-primary)] animate-spin"
                    >
                      <ImSpinner size={18} />
                    </span>
                  )}
                  {emailCheckStatus === 'available' && (
                    <span
                      role="img"
                      aria-label="Email is available"
                      tabIndex={0}
                      className="text-green-700"
                    >
                      <FaCheckCircle size={18} />
                    </span>
                  )}
                  {(emailCheckStatus === 'unavailable' || emailCheckStatus === 'error') && (
                    <span
                      role="img"
                      aria-label="Email unavailable"
                      tabIndex={0}
                      className="text-red-700"
                    >
                      <FaTimesCircle size={18} />
                    </span>
                  )}
                  {emailCheckStatus === 'invalid' && (
                    <span
                      role="img"
                      aria-label="Invalid email format"
                      tabIndex={0}
                      className="text-amber-700"
                    >
                      <AiOutlineWarning size={19} />
                    </span>
                  )}
                </span>
              )}

              {emailAvailabilityMessage && emailCheckStatus !== 'idle' && (
                <div className="pointer-events-none absolute right-0 top-[calc(100%+6px)] z-20 whitespace-nowrap rounded-md bg-[var(--color-elevatedSurface)] border border-[var(--color-border)] px-2 py-1 text-xs text-[var(--color-textPrimary)] opacity-0 transition-opacity duration-150 group-hover:opacity-100 group-focus-within:opacity-100">
                  {emailCheckStatus === 'checking' ? 'Checking availability...' : emailAvailabilityMessage}
                </div>
              )}
            </motion.div>

            <motion.p
              id="email-check-status"
              variants={staggerItemVariants}
              aria-live="polite"
              className="sr-only"
            >
              {emailAvailabilityMessage}
            </motion.p>

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
                disabled={!canSubmit}
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
