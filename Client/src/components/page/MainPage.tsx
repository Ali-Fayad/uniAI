import React from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { useTheme } from "../../hooks/useTheme";
import { useScrollAnimation } from "../../hooks/useScrollAnimation";
import { useFeedback } from "../../hooks/useFeedback";
import { TEXT } from "../../constants/static";
import { ROUTES } from "../../router";
import LiquidEther from "../LiquidEther";
import { PageTransition, ScaleIn, SlideIn, StaggerContainer, staggerItemVariants, FadeIn } from "../animations";

// Simple Star Icon Component
const StarIcon: React.FC<{ filled: boolean; onClick: () => void }> = ({
  filled,
  onClick,
}) => (
  <svg
    onClick={onClick}
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill={filled ? "var(--color-customPrimary)" : "none"}
    stroke={filled ? "var(--color-customPrimary)" : "var(--color-textMuted, #9CA3AF)"}
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="w-8 h-8 cursor-pointer transition-colors duration-200 hover:scale-110 active:scale-95"
  >
    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
  </svg>
);

const Card: React.FC<{
  title: string;
  children: React.ReactNode;
  delay?: number;
}> = ({ title, children, delay = 0 }) => {
  // Use scroll animation for cards as they might be below fold
  const ref = useScrollAnimation({ delay, threshold: 0.15 });

  return (
    <div
      ref={ref}
      className="bg-[var(--color-surface)] p-8 rounded-lg shadow-md border border-[var(--color-border)] text-center h-full flex flex-col justify-center"
    >
      <h3 className="text-2xl font-bold text-[var(--color-textPrimary)] mb-4">{title}</h3>
      <p className="text-[var(--color-textSecondary)]">{children}</p>
    </div>
  );
};

/**
 * AnimatedField Component
 * Wrapper for scroll animations with fade-in effect
 */
const AnimatedField: React.FC<{
  children: React.ReactNode;
  delay?: number;
}> = ({ children, delay = 0 }) => {
  const ref = useScrollAnimation({ delay, threshold: 0.15 });
  return <div ref={ref}>{children}</div>;
};

/**
 * Feedback Component
 * Feedback form section with rating and comment
 */
const Feedback: React.FC = () => {
  const { rating, email, comment, setRating, setEmail, setComment, handleSubmit } = useFeedback();

  return (
    <div className="max-w-2xl mx-auto p-6">
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 1. Email Field */}
        <AnimatedField delay={0}>
          <label
            className="block text-sm font-medium leading-6 text-[#151514]"
            htmlFor="email"
          >
            {TEXT.main.feedback.emailLabel}
          </label>
          <div className="mt-2">
            <input
              id="email"
              name="email"
              placeholder={TEXT.main.feedback.emailPlaceholder}
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="block w-full rounded-md border-0 py-2.5 px-3.5 text-[var(--color-textPrimary)] bg-[var(--color-background)] shadow-sm ring-1 ring-inset ring-[var(--color-border)] placeholder:text-[var(--color-textSecondary)] focus:ring-2 focus:ring-inset focus:ring-[var(--color-primary)] sm:text-sm sm:leading-6"
            />
          </div>
        </AnimatedField>

        {/* 2. Rating Field */}
        <AnimatedField delay={100}>
          <label className="block text-sm font-medium leading-6 text-[#151514] mb-2">
            {TEXT.main.feedback.ratingLabel}
          </label>
          <div className="flex flex-col items-center space-y-2">
            <div className="flex items-center justify-center space-x-2">
              {[1, 2, 3, 4, 5].map((star) => (
                <StarIcon
                  key={star}
                  filled={star <= rating}
                  onClick={() => setRating(star)}
                />
              ))}
            </div>

            <span className="text-sm text-gray-500 font-medium">
              {rating > 0 ? `${rating} / 5` : ""}
            </span>
          </div>
          <input type="hidden" name="rating" value={rating} />
        </AnimatedField>

        {/* 3. Feedback Textarea */}
        <AnimatedField delay={200}>
          <label
            className="block text-sm font-medium leading-6 text-[var(--color-textPrimary)]"
            htmlFor="feedback"
          >
            {TEXT.main.feedback.feedbackLabel}
          </label>
          <div className="mt-2">
            <textarea
              id="feedback"
              name="feedback"
              placeholder={TEXT.main.feedback.feedbackPlaceholder}
              rows={4}
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              className="block w-full rounded-md border-0 py-2.5 px-3.5 text-[var(--color-textPrimary)] bg-[var(--color-background)] shadow-sm ring-1 ring-inset ring-[var(--color-border)] placeholder:text-[var(--color-textSecondary)] focus:ring-2 focus:ring-inset focus:ring-[var(--color-primary)] sm:text-sm sm:leading-6"
            />
          </div>
        </AnimatedField>

        {/* 4. Submit Button */}
        <AnimatedField delay={300}>
          <div className="flex justify-center">
            <button
              type="submit"
              className="cursor-pointer items-center justify-center overflow-hidden rounded-full h-12 px-8 bg-[var(--color-primary)] text-[var(--color-background)] text-base font-bold leading-normal tracking-[0.015em] hover:bg-[var(--color-primaryVariant)] transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[var(--color-primary)]"
            >
              <span className="truncate">{TEXT.main.feedback.submitButton}</span>
            </button>
          </div>
        </AnimatedField>
      </form>
    </div>
  );
};

/**
 * MainPage Component
 * 
 * Responsibilities:
 * - Render main landing page layout
 * - Compose sections (hero, cards, feedback)
 * 
 * Business logic is extracted to hooks.
 */
const MainPage: React.FC = () => {
  const navigate = useNavigate();
  const { colors, themeName } = useTheme();

  // Map theme colors to LiquidEther palette
  const fluidColors = [
    colors.primary,
    colors.secondary,
    colors.primaryVariant || colors.accent,
    colors.info
  ];

  return (
    <PageTransition>
      <StaggerContainer staggerDelay={0.15} initialDelay={0.1}>
        {/* Hero Section */}
        <ScaleIn delay={0} className="relative flex min-h-[700px] items-center overflow-hidden bg-[var(--color-surface)] py-12">
          {/* Liquid Background */}
          <div className="absolute inset-0 z-0">
            <LiquidEther
              colors={fluidColors}
              isViscous={true}
              viscous={20}
              mouseForce={30}
              cursorSize={80}
              autoDemo={true}
              autoSpeed={0.3}
              key={themeName}
            />
          </div>

          {/* Content Overlay */}
          <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
            <div className="flex flex-col items-center justify-center text-center max-w-4xl mx-auto">
              <StaggerContainer staggerDelay={0.12} initialDelay={0.2}>
                <motion.h1 variants={staggerItemVariants} className="text-4xl font-extrabold tracking-tight text-[var(--color-textPrimary)] sm:text-5xl md:text-6xl lg:text-7xl mb-6">
                  {TEXT.main.hero.welcome} <span className="text-[var(--color-primary)]">{TEXT.main.hero.appName}</span>
                </motion.h1>
                <motion.p variants={staggerItemVariants} className="mt-4 text-xl text-[var(--color-textSecondary)] max-w-2xl mx-auto mb-10">
                  {TEXT.main.hero.subtitle}
                </motion.p>
                <motion.div variants={staggerItemVariants} className="flex flex-col sm:flex-row gap-4 justify-center">
                  <button
                    onClick={() => navigate(ROUTES.CHAT)}
                    className="inline-flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-full text-[var(--color-background)] bg-[var(--color-primary)] hover:bg-[var(--color-primaryVariant)] md:py-4 md:text-lg md:px-10 transition-all shadow-lg hover:shadow-xl transform hover:-translate-y-1"
                  >
                    {TEXT.main.hero.getStarted}
                  </button>
                  <button
                    onClick={() => navigate(ROUTES.MAP)}
                    className="inline-flex items-center justify-center px-8 py-3 border-2 border-[var(--color-primary)] text-base font-medium rounded-full text-[var(--color-primary)] bg-transparent hover:bg-[var(--color-surface)] md:py-4 md:text-lg md:px-10 transition-all"
                  >
                    {TEXT.main.hero.maps}
                  </button>
                </motion.div>
              </StaggerContainer>
            </div>
          </div>
        </ScaleIn>

        {/* Content Container */}
        <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-12">
          {/* Try Now Button */}
          <SlideIn direction="up" delay={0.3} className="text-center mb-16">
            <button
              onClick={() => navigate(ROUTES.CHAT)}
              className="inline-flex cursor-pointer items-center justify-center overflow-hidden rounded-full h-14 px-8 bg-[var(--color-primary)] text-[var(--color-background)] text-lg font-bold leading-normal tracking-[0.015em] hover:bg-[var(--color-primaryVariant)] transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[var(--color-primary)]"
            >
              <span className="truncate">{TEXT.main.tryNow}</span>
            </button>
          </SlideIn>

          {/* Cards Section */}
          <SlideIn direction="left" delay={0.4} className="my-12">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              <Card title={TEXT.main.cards.powerfulIntegration.title} delay={0}>
                {TEXT.main.cards.powerfulIntegration.description}
              </Card>
              <Card title={TEXT.main.cards.creativeAssistance.title} delay={100}>
                {TEXT.main.cards.creativeAssistance.description}
              </Card>
              <Card title={TEXT.main.cards.dataInsights.title} delay={200}>
                {TEXT.main.cards.dataInsights.description}
              </Card>
            </div>
          </SlideIn>

          {/* Spacer & Intro Text */}
          <FadeIn delay={0.6} className="mt-32 mb-8 text-center max-w-2xl mx-auto px-4">
            <h2 className="text-3xl font-bold text-[var(--color-textPrimary)] mb-4">
              {TEXT.main.feedback.heading}
            </h2>
            <p className="text-lg text-[var(--color-textSecondary)]">
              {TEXT.main.feedback.subheading}
            </p>
          </FadeIn>

          {/* Feedback Form */}
          <SlideIn direction="up" delay={0.7}>
            <Feedback />
          </SlideIn>
        </div>
      </StaggerContainer>
    </PageTransition>
  );
};

export default MainPage;
