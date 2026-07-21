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
import AnimatedInput from "../../../components/common/AnimatedInput";

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
    usernameAvailabilityMessage,
    usernameCheckStatus,
    emailAvailabilityMessage,
    emailCheckStatus,
    fieldErrors,
    visibleErrors,
    passwordRules,
    markFieldTouched,
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
                role="alert"
                aria-live="assertive"
                className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm"
              >
                {error}
              </motion.div>
            )}
            {Object.keys(fieldErrors).length > 0 && error && (
              <p className="text-sm text-[var(--color-textSecondary)]" aria-live="polite">
                Please correct the highlighted fields before continuing.
              </p>
            )}

            {/* Username */}
            <motion.div variants={staggerItemVariants} className="relative group">
              <AnimatedInput
                type="text"
                label={TEXT.auth.signUp.usernamePlaceholder}
                id="signup-username"
                value={username}
                onChange={(e) => { markFieldTouched('username'); setUsername(e.target.value); }}
                onBlur={() => markFieldTouched('username')}
                error={visibleErrors.username}
                aria-invalid={Boolean(visibleErrors.username)}
                aria-describedby="signup-username-error username-check-status"
                required
                className="pr-14"
              >
                {usernameCheckStatus !== 'idle' && (
                  <span className="absolute right-4 top-1/2 -translate-y-1/2 flex items-center pointer-events-none">
                    {usernameCheckStatus === 'checking' && (
                      <span
                        role="status"
                        aria-label="Checking availability"
                        tabIndex={0}
                        className="text-[var(--color-primary)] animate-spin"
                      >
                        <ImSpinner size={18} />
                      </span>
                    )}
                    {usernameCheckStatus === 'available' && (
                      <span
                        role="img"
                        aria-label="Username is available"
                        tabIndex={0}
                        className="text-green-700"
                      >
                        <FaCheckCircle size={18} />
                      </span>
                    )}
                    {(usernameCheckStatus === 'unavailable' || usernameCheckStatus === 'error') && (
                      <span
                        role="img"
                        aria-label="Username unavailable"
                        tabIndex={0}
                        className="text-red-700"
                      >
                        <FaTimesCircle size={18} />
                      </span>
                    )}
                    {usernameCheckStatus === 'invalid' && (
                      <span
                        role="img"
                        aria-label="Invalid username format"
                        tabIndex={0}
                        className="text-amber-700"
                      >
                        <AiOutlineWarning size={19} />
                      </span>
                    )}
                  </span>
                )}

                {usernameAvailabilityMessage && usernameCheckStatus !== 'idle' && (
                  <div className="pointer-events-none absolute right-0 top-[calc(100%+6px)] z-20 whitespace-nowrap rounded-md bg-[var(--color-elevatedSurface)] border border-[var(--color-border)] px-2 py-1 text-xs text-[var(--color-textPrimary)] opacity-0 transition-opacity duration-150 group-hover:opacity-100 group-focus-within:opacity-100">
                    {usernameCheckStatus === 'checking' ? 'Checking availability...' : usernameAvailabilityMessage}
                  </div>
                )}
              </AnimatedInput>
            </motion.div>

            <motion.p
              id="username-check-status"
              variants={staggerItemVariants}
              aria-live="polite"
              className="mt-1 px-1 text-xs text-[var(--color-textSecondary)]"
            >
              {usernameAvailabilityMessage}
            </motion.p>

            {/* Name fields */}
            <motion.div variants={staggerItemVariants} className="grid grid-cols-2 gap-4">
              <AnimatedInput
                type="text"
                label={TEXT.auth.signUp.firstNamePlaceholder}
                id="signup-firstName"
                value={firstName}
                onChange={(e) => { markFieldTouched('firstName'); setFirstName(e.target.value); }}
                onBlur={() => markFieldTouched('firstName')}
                error={visibleErrors.firstName}
                aria-invalid={Boolean(visibleErrors.firstName)}
                aria-describedby="signup-firstName-error"
                required
              />
              <AnimatedInput
                type="text"
                label={TEXT.auth.signUp.lastNamePlaceholder}
                id="signup-lastName"
                value={lastName}
                onChange={(e) => { markFieldTouched('lastName'); setLastName(e.target.value); }}
                onBlur={() => markFieldTouched('lastName')}
                error={visibleErrors.lastName}
                aria-invalid={Boolean(visibleErrors.lastName)}
                aria-describedby="signup-lastName-error"
                required
              />
            </motion.div>

            {/* Email */}
            <motion.div variants={staggerItemVariants} className="relative group">
              <AnimatedInput
                type="email"
                label="Email address"
                id="signup-email"
                value={email}
                onChange={(e) => { markFieldTouched('email'); setEmail(e.target.value); }}
                onBlur={() => markFieldTouched('email')}
                error={visibleErrors.email}
                aria-invalid={Boolean(visibleErrors.email)}
                aria-describedby="signup-email-error email-check-status"
                required
                className="pr-14"
              >
                {emailCheckStatus !== 'idle' && (
                  <span className="absolute right-4 top-1/2 -translate-y-1/2 flex items-center pointer-events-none">
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
              </AnimatedInput>
            </motion.div>

            <motion.p
              id="email-check-status"
              variants={staggerItemVariants}
              aria-live="polite"
              className="mt-1 px-1 text-xs text-[var(--color-textSecondary)]"
            >
              {emailAvailabilityMessage}
            </motion.p>

            {/* Password */}
            <motion.div variants={staggerItemVariants} className="relative">
              <AnimatedInput
                type={showPassword ? "text" : "password"}
                label={TEXT.auth.signUp.passwordPlaceholder}
                id="signup-password"
                value={password}
                onChange={(e) => { markFieldTouched('password'); setPassword(e.target.value); }}
                onBlur={() => markFieldTouched('password')}
                error={visibleErrors.password}
                aria-invalid={Boolean(visibleErrors.password)}
                aria-describedby="signup-password-error signup-password-requirements"
                required
                minLength={8}
                className="pr-14"
              >
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-[var(--color-primaryVariant)] hover:text-[var(--color-primary)] bg-transparent p-0 border-0 transition-colors"
                >
                  {showPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
                </button>
                <ul id="signup-password-requirements" className="mt-2 space-y-1 px-1 text-xs" aria-live="polite">
                  {passwordRules.map((rule) => (
                    <li key={rule.label} className={rule.satisfied ? 'text-green-600' : 'text-[var(--color-textSecondary)]'}>
                      <span aria-hidden="true">{rule.satisfied ? '✓' : '○'}</span>{' '}{rule.label}
                    </li>
                  ))}
                </ul>
              </AnimatedInput>
            </motion.div>

            {/* Confirm Password */}
            <motion.div variants={staggerItemVariants} className="relative">
              <AnimatedInput
                type={showConfirmPassword ? "text" : "password"}
                label={TEXT.auth.signUp.confirmPasswordPlaceholder}
                id="signup-confirmPassword"
                value={confirmPassword}
                onChange={(e) => { markFieldTouched('confirmPassword'); setConfirmPassword(e.target.value); }}
                onBlur={() => markFieldTouched('confirmPassword')}
                error={visibleErrors.confirmPassword}
                aria-invalid={Boolean(visibleErrors.confirmPassword)}
                aria-describedby="signup-confirmPassword-error"
                required
                minLength={8}
                className="pr-14"
              >
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-[var(--color-primaryVariant)] hover:text-[var(--color-primary)] bg-transparent p-0 border-0 transition-colors"
                >
                  {showConfirmPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
                </button>
              </AnimatedInput>
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
                {isLoading ? 'Creating account…' : TEXT.auth.signUp.submitButton}
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
