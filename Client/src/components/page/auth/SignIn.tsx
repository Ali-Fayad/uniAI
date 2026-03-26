import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { AuthCard } from "../../../components/AuthCard";
import AuthHeading from "../../../components/auth/AuthHeading";
import { StaggerContainer, staggerItemVariants } from "../../../components/animations";
import { LuEye, LuEyeOff } from "react-icons/lu";
import { useSignIn } from "../../../hooks/useSignIn";
import { TEXT } from "../../../constants/static";
import { ROUTES } from "../../../router";

import AnimatedInput from "../../../components/common/AnimatedInput";
import { Link } from "react-router-dom";

const SignIn = () => {
  const navigate = useNavigate();
  const {
    email,
    password,
    isLoading,
    error,
    showPassword,
    setEmail,
    setPassword,
    setShowPassword,
    handleLogin,
  } = useSignIn();

  return (
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-[var(--color-background)] py-12 px-4">
      <AuthCard>
        <div className="pt-8">
          <AuthHeading 
            title={TEXT.auth.signIn.title}
            subtitle={TEXT.auth.signIn.subtitle}
          />
        </div>

        <StaggerContainer staggerDelay={0.08} initialDelay={0.1}>
          <form className="space-y-6" onSubmit={handleLogin}>
            {error && (
              <motion.div 
                variants={staggerItemVariants}
                className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm"
              >
                {error}
              </motion.div>
            )}

            {/* EMAIL FIELD */}
            <motion.div variants={staggerItemVariants} className="flex flex-col">
              <AnimatedInput
                type="email"
                label={TEXT.auth.signIn.emailLabel}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </motion.div>

            {/* PASSWORD FIELD */}
            <motion.div variants={staggerItemVariants} className="flex flex-col w-full">
              {/* Forgot Password Link */}
              <div className="flex justify-end pb-2">
                <button
                  type="button"
                  onClick={() => navigate(ROUTES.FORGOT_PASSWORD)}
                  className="text-[var(--color-primaryVariant)] text-sm font-medium hover:text-[var(--color-primary)] transition-colors bg-transparent p-0"
                >
                  {TEXT.auth.signIn.forgotPassword}
                </button>
              </div>

              {/* Input + Toggle Icon */}
              <AnimatedInput
                type={showPassword ? "text" : "password"}
                label={TEXT.auth.signIn.passwordLabel}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="pr-14"
              >
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-[var(--color-primaryVariant)] hover:text-[var(--color-primary)] transition-colors bg-transparent p-0"
                >
                  {showPassword ? <LuEyeOff size={22} /> : <LuEye size={22} />}
                </button>
              </AnimatedInput>
            </motion.div>

            {/* SIGN IN BUTTON */}
            <motion.div variants={staggerItemVariants}>
              <motion.button
                type="submit"
                disabled={isLoading}
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                className="flex w-full items-center justify-center rounded-full h-12 px-5 bg-[var(--color-primary)] text-[var(--color-background)] font-bold hover:bg-[var(--color-primaryVariant)] transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? TEXT.auth.signIn.submitButtonLoading : TEXT.auth.signIn.submitButton}
              </motion.button>
            </motion.div>
          </form>

          <motion.p 
            variants={staggerItemVariants}
            className="text-center text-[var(--color-textSecondary)] text-sm mt-6"
          >
            {TEXT.auth.signIn.noAccount}{" "}
            <button
              type="button"
              className="text-[var(--color-primaryVariant)] font-medium hover:text-[var(--color-primary)] transition-colors ml-1 bg-transparent p-0"
              onClick={() => navigate(ROUTES.SIGN_UP)}
            >
              {TEXT.auth.signIn.signUpLink}
            </button>
          </motion.p>
        </StaggerContainer>
      </AuthCard>
    </div>
  );
};

export default SignIn;
