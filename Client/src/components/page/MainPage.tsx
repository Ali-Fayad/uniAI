import React, { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

// Reusable hook for scroll animations that reverse when scrolling up
const useScrollAnimation = (delay = 0) => {
  const ref = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    // Initial state
    el.style.opacity = "0";
    el.style.transform = "translateY(20px)";
    el.style.transition = "opacity 500ms ease, transform 500ms ease";

    const obs = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            // Animate in
            setTimeout(() => {
              el.style.opacity = "1";
              el.style.transform = "translateY(0)";
            }, delay);
          } else {
            // Reverse animation (hide) when scrolling up/away
            el.style.opacity = "0";
            el.style.transform = "translateY(20px)";
          }
        });
      },
      { threshold: 0.15 }
    );

    obs.observe(el);
    return () => obs.disconnect();
  }, [delay]);

  return ref;
};

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
  const ref = useScrollAnimation(delay);

  return (
    <div
      ref={ref}
      className="bg-[var(--color-surface)] p-8 rounded-lg shadow-md border border-[var(--color-border)] text-center"
    >
      <h3 className="text-2xl font-bold text-[var(--color-textPrimary)] mb-4">{title}</h3>
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
      navigate('/auth');
      return;
    }

    // TODO: Implement real API call for feedback
    console.log('Feedback submitted (placeholder)');
  };

  return (
    <div className="max-w-2xl mx-auto p-6">
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 1. Email Field */}
        <AnimatedField delay={0}>
          <label
            className="block text-sm font-medium leading-6 text-[#151514]"
            htmlFor="email"
          >
            Email
          </label>
          <div className="mt-2">
            <input
              id="email"
              name="email"
              placeholder="you@example.com"
              type="email"
              className="block w-full rounded-md border-0 py-2.5 px-3.5 text-[var(--color-textPrimary)] bg-[var(--color-background)] shadow-sm ring-1 ring-inset ring-[var(--color-border)] placeholder:text-[var(--color-textSecondary)] focus:ring-2 focus:ring-inset focus:ring-[var(--color-primary)] sm:text-sm sm:leading-6"
            />
          </div>
        </AnimatedField>

        {/* 2. Rating Field (New) */}
        <AnimatedField delay={100}>
          <label className="block text-sm font-medium leading-6 text-[#151514] mb-2">
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

  return (
    <div>
      {/* Hero Section */}
      <section className="relative flex min-h-[700px] items-center overflow-hidden bg-[var(--color-surface)] py-12">
        <div
          className="absolute inset-0 bg-[url('https://images.unsplash.com/photo-1523050854058-8df90110c9f1?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3wzNjAzNTV8MHwxfHNlYXJjaHw1fHx1bml2ZXJzaXR5fGVufDB8fHx8MTcxNzU4OTIyNnww&ixlib=rb-4.0.3&q=80&w=1080')] bg-cover bg-center opacity-10"
        ></div>
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="relative z-10 grid grid-cols-1 md:grid-cols-2 gap-8 items-center">
            <div className="text-center md:text-left">
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-black tracking-tight text-[var(--color-textPrimary)]/90">
                Welcome to uniAI
              </h1>
              <p className="mt-4 text-lg md:text-xl text-[var(--color-textPrimary)]/70">
                Your unified platform for next-generation artificial intelligence.
                Streamline your workflow, boost creativity, and unlock new
                possibilities with our intuitive toolset.
              </p>
            </div>
            <div className="flex justify-center items-center">
              <img
                alt="Graduation cap illustration"
                className="h-64 w-64 md:h-80 md:w-80 object-cover rounded-full border-4 border-white shadow-lg"
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuDchELEmNB0CNKcnCkxUDv36OZb4QPhsSkxcj8BV0hiuobbsrHMK8IgOsN8DRL8AfUDGJxo4ZYwbfLjxln1nPQ4RinsoGIggV86zitWGy0HyVT-_inFeJdrvMNOyjSg7UsFwQ32cz83Y3a7F-aDaSIl46JoAOt1KpBpHDkSd4i39JjAMIaF9iaxXI0dlgSbCDnwVdFRI7jfnV9hpA_F2qlppD8ezA240E9nPyH1iyOa-LraGLm5I9QhqeZU6xN7HY-Uf2gJooV6jjs"
              />
            </div>
          </div>
        </div>
      </section>

      {/* Content Container */}
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Try Now Button */}
        <section className="text-center mb-16">
          <button
            onClick={() => navigate('/chat')}
            className="inline-flex cursor-pointer items-center justify-center overflow-hidden rounded-full h-14 px-8 bg-[var(--color-primary)] text-[var(--color-background)] text-lg font-bold leading-normal tracking-[0.015em] hover:bg-[var(--color-primaryVariant)] transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[var(--color-primary)]"
          >
            <span className="truncate">Try Now</span>
          </button>
        </section>

      {/* Cards Section */}
      <section className="my-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <Card title="Powerful Integration" delay={0}>
            Seamlessly connect with your favorite tools and platforms. UniAI
            works with your existing ecosystem to enhance productivity without
            disruption.
          </Card>
          <Card title="Creative Assistance" delay={0}>
            Break through creative blocks with AI-powered suggestions, content
            generation, and idea exploration. Elevate your creative projects to
            new heights.
          </Card>
          <Card title="Data-driven Insights" delay={0}>
            Transform complex data into clear, actionable insights. Make smarter
            decisions with our advanced analytics and visualization
            capabilities.
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
