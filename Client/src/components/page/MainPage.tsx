import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useTheme } from "../../hooks/useTheme";
import LiquidEther from "../LiquidEther";
import TextType from "../common/TextType";
import { useScrollAnimation } from "@hooks/useScrollAnimation";

// useScrollAnimation moved to src/hooks/useScrollAnimation

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
    stroke={
      filled ? "var(--color-customPrimary)" : "var(--color-textMuted, #9CA3AF)"
    }
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
  const ref = useScrollAnimation(delay);

  return (
    <div
      ref={ref}
      className="bg-[var(--color-surface)] p-8 rounded-lg shadow-md border border-[var(--color-border)] text-center"
    >
      <h3 className="text-2xl font-bold text-[var(--color-textPrimary)] mb-4">
        {title}
      </h3>
      <p className="text-[var(--color-textSecondary)]">{children}</p>
    </div>
  );
};

const AnimatedField: React.FC<{
  children: React.ReactNode;
  delay?: number;
}> = ({ children, delay = 0 }) => {
  const ref = useScrollAnimation(delay);
  return <div ref={ref}>{children}</div>;
};

const Feedback: React.FC = () => {
  const [rating, setRating] = useState(0);
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // Check if user is authenticated before sending feedback
    if (!isAuthenticated) {
      navigate("/auth");
      return;
    }

    // TODO: Implement real API call for feedback
    console.log("Feedback submitted (placeholder)");
  };

  return (
    <div className="max-w-2xl mx-auto p-6">
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Email removed: feedback requires authentication, navigate to auth when unauthenticated */}

        {/* 2. Rating Field (New) */}
        <AnimatedField delay={100}>
          <label className="block text-sm font-medium leading-6 text-[var(--color-textPrimary)] mb-2">
            Rate Your Experience
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
          {/* Hidden input to submit the value with the form */}
          <input type="hidden" name="rating" value={rating} />
        </AnimatedField>

        {/* 3. Feedback Textarea - Delay shifted to 200 */}
        <AnimatedField delay={200}>
          <label
            className="block text-sm font-medium leading-6 text-[var(--color-textPrimary)]"
            htmlFor="feedback"
          >
            Your Feedback
          </label>
          <div className="mt-2">
            <textarea
              id="feedback"
              name="feedback"
              placeholder="Let us know how we can improve..."
              rows={4}
              className="block w-full rounded-md border-0 py-2.5 px-3.5 text-[var(--color-textPrimary)] bg-[var(--color-background)] shadow-sm ring-1 ring-inset ring-[var(--color-border)] placeholder:text-[var(--color-textSecondary)] focus:ring-2 focus:ring-inset focus:ring-[var(--color-primary)] sm:text-sm sm:leading-6"
            />
          </div>
        </AnimatedField>

        {/* 4. Submit Button - Delay shifted to 300 */}
        <AnimatedField delay={300}>
          <div className="flex justify-center">
            <button
              type="submit"
              className="cursor-pointer items-center justify-center overflow-hidden rounded-full h-12 px-8 bg-[var(--color-primary)] text-[var(--color-background)] text-base font-bold leading-normal tracking-[0.015em] hover:bg-[var(--color-primaryVariant)] transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[var(--color-primary)]"
            >
              <span className="truncate">Submit Feedback</span>
            </button>
          </div>
        </AnimatedField>
      </form>
    </div>
  );
};

const MainPage: React.FC = () => {
  const introTextRef = useScrollAnimation(0);
  const navigate = useNavigate();
  const { colors, themeName } = useTheme();
  const [typingStage, setTypingStage] = useState(0);

  // Map theme colors to LiquidEther palette
  // We use primary, secondary, and accent/variant to create a nice fluid mix
  const fluidColors = [
    colors.primary,
    colors.secondary,
    colors.primaryVariant || colors.accent,
    colors.info, // Add a splash of info color for depth
  ];

  return (
    <div>
      {/* Hero Section */}
      <section className="relative flex min-h-[700px] items-center overflow-hidden bg-[var(--color-surface)] py-12">
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
            // Force re-mount on theme change to ensure colors update cleanly if hot-swap isn't perfect
            key={themeName}
          />
        </div>

        {/* Content Overlay */}
        <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="flex flex-col items-center justify-center text-center max-w-4xl mx-auto">
            <div ref={introTextRef}>
              <h1 className="text-4xl font-extrabold tracking-tight text-[var(--color-textPrimary)] sm:text-5xl md:text-6xl lg:text-7xl mb-6">
                <TextType
                  text="Welcome to "
                  typingSpeed={50}
                  startOnVisible={true}
                  loop={false}
                  showCursor={typingStage === 0}
                  cursorCharacter="|"
                  onSentenceComplete={() => setTypingStage(1)}
                />
                <span className="text-[var(--color-primary)]">
                  {typingStage >= 1 && (
                    <TextType
                      text="uniAI"
                      typingSpeed={50}
                      startOnVisible={true}
                      loop={false}
                      showCursor={typingStage === 1}
                      cursorCharacter="|"
                      onSentenceComplete={() => setTypingStage(2)}
                    />
                  )}
                </span>
              </h1>

              {typingStage >= 2 && (
                <TextType
                  as="p"
                  className="mt-4 text-xl text-[var(--color-textSecondary)] max-w-2xl mx-auto mb-10"
                  text="Explore programs, admission requirements, tuition fees, deadlines, and campus life — all in one place. UniAI is an AI-powered assistant designed to guide high-school graduates through every step of their university journey in Lebanon."
                  typingSpeed={30}
                  startOnVisible={true}
                  loop={false}
                  showCursor={true}
                  cursorCharacter="|"
                />
              )}
              {/* Placeholder to prevent layout shift if needed, or just let it flow */}
              {typingStage < 2 && (
                <p className="mt-4 text-xl text-transparent max-w-2xl mx-auto mb-10 select-none">
                  Explore programs, admission requirements, tuition fees,
                  deadlines, and campus life — all in one place. UniAI is an
                  AI-powered assistant designed to guide high-school graduates
                  through every step of their university journey in Lebanon.
                </p>
              )}

              <div className="flex flex-col sm:flex-row gap-4 justify-center">
                <button
                  onClick={() => navigate("/chat")}
                  className="inline-flex items-center justify-center px-8 py-3 border border-transparent text-base font-medium rounded-full text-[var(--color-background)] bg-[var(--color-primary)] hover:bg-[var(--color-primaryVariant)] md:py-4 md:text-lg md:px-10 transition-all shadow-lg hover:shadow-xl transform hover:-translate-y-1"
                >
                  Get Started
                </button>
                <button
                  onClick={() => navigate("/about")}
                  className="inline-flex items-center justify-center px-8 py-3 border-2 border-[var(--color-primary)] text-base font-medium rounded-full text-[var(--color-primary)] bg-transparent hover:bg-[var(--color-surface)] md:py-4 md:text-lg md:px-10 transition-all"
                >
                  Learn More
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Content Container */}
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Try Now Button removed - 'Get Started' already navigates to chat */}

        {/* Cards Section */}
        <section className="my-12">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <Card title="Centralized University Information" delay={0}>
              Get accurate and up-to-date information from Lebanese universities
              in one place. UniAI gathers details about programs, faculties,
              admission conditions, tuition fees, and deadlines so you don’t
              have to search across multiple websites.
            </Card>
            <Card title="Personalized Academic Guidance" delay={0}>
              Ask questions in natural language and get clear, tailored answers.
              Whether you’re unsure which major fits your interests or need help
              comparing universities, UniAI provides personalized guidance based
              on your goals and preferences.
            </Card>
            <Card title="Smart Comparisons & Insights" delay={0}>
              Compare universities, majors, and costs side by side. UniAI turns
              complex academic information into simple insights, helping you
              make confident decisions about your future education.
            </Card>
          </div>
        </section>

        {/* Spacer & Intro Text */}
        <section className="mt-32 mb-8 text-center max-w-2xl mx-auto px-4">
          <div ref={introTextRef}>
            <h2 className="text-3xl font-bold text-[var(--color-textPrimary)] mb-4">
              We Value Your Input
            </h2>
            <p className="text-lg text-[var(--color-textSecondary)]">
              Your experience matters to us. Whether you have a suggestion, a
              question, or just want to say hello, we're here to listen. Help us
              shape the future of UniAI.
            </p>
          </div>
        </section>

        {/* Feedback Form */}
        <section>
          <Feedback />
        </section>
      </div>
    </div>
  );
};

export default MainPage;
