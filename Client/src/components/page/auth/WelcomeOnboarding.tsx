import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { AuthCard } from "../../../components/AuthCard";
import { StaggerContainer, staggerItemVariants } from "../../../components/animations";
import { useAuth } from "../../../hooks/useAuth";
import { userService } from "../../../services/user";
import { TEXT } from "../../../constants/static";
import { ROUTES } from "../../../router";
import LoadingSpinner from "../../../components/common/LoadingSpinner";

type WelcomeLocationState = {
  redirectTo?: string;
};

const WelcomeOnboarding = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const [isCheckingProfile, setIsCheckingProfile] = useState(true);

  const redirectTo = useMemo(() => {
    const state = (location.state as WelcomeLocationState | null) ?? null;
    return state?.redirectTo || ROUTES.CHAT;
  }, [location.state]);

  useEffect(() => {
    const ensureNeedsOnboarding = async () => {
      try {
        const exists = await userService.hasPersonalInfo();
        if (exists) {
          navigate(redirectTo, { replace: true });
          return;
        }
      } catch {
        // If check fails, keep user on welcome screen to avoid blocking onboarding.
      } finally {
        setIsCheckingProfile(false);
      }
    };

    void ensureNeedsOnboarding();
  }, [navigate, redirectTo]);

  const firstName = user?.firstName?.trim() || "there";

  const handleContinue = () => {
    navigate(ROUTES.PERSONAL_INFO, { replace: true });
  };

  const handleRemindLater = () => {
    navigate(redirectTo, { replace: true });
  };

  if (isCheckingProfile) {
    return (
      <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-[var(--color-background)] py-12 px-4">
        <LoadingSpinner text={TEXT.auth.welcome.checkingProfile} />
      </div>
    );
  }

  return (
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-[var(--color-background)] py-12 px-4">
      <AuthCard>
        <StaggerContainer staggerDelay={0.08} initialDelay={0.1}>
          <motion.div variants={staggerItemVariants} className="flex flex-col gap-2 mb-6 pt-8">
            <p className="text-[var(--color-textPrimary)] text-4xl font-black tracking-[-0.033em]">
              {TEXT.auth.welcome.title}, {firstName}
            </p>
            <p className="text-[var(--color-textSecondary)]">
              {TEXT.auth.welcome.subtitle}
            </p>
          </motion.div>

          <motion.p
            variants={staggerItemVariants}
            className="text-[var(--color-textSecondary)] leading-relaxed mb-8"
          >
            {TEXT.auth.welcome.description}
          </motion.p>

          <motion.div variants={staggerItemVariants} className="space-y-3">
            <motion.button
              type="button"
              onClick={handleContinue}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="flex w-full items-center justify-center rounded-full h-12 px-5 bg-[var(--color-primary)] text-[var(--color-background)] font-bold hover:bg-[var(--color-primaryVariant)] transition"
            >
              {TEXT.auth.welcome.continueButton}
            </motion.button>

            <motion.button
              type="button"
              onClick={handleRemindLater}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="flex w-full items-center justify-center rounded-full h-12 px-5 border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-textPrimary)] font-medium hover:bg-[var(--color-elevatedSurface)] transition"
            >
              {TEXT.auth.welcome.remindLaterButton}
            </motion.button>
          </motion.div>
        </StaggerContainer>
      </AuthCard>
    </div>
  );
};

export default WelcomeOnboarding;
