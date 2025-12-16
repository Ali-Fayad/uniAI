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
    fill={filled ? "#B6AE9F" : "none"} // custom-primary color when filled
    stroke={filled ? "#B6AE9F" : "#9CA3AF"} // gray-400 when empty
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
      className="bg-white/80 backdrop-blur-sm p-8 rounded-lg shadow-md border border-white/30 text-center"
    >
      <h3 className="text-2xl font-bold text-[#151514] mb-4">{title}</h3>
      <p className="text-[#797672]">{children}</p>
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
              className="block w-full rounded-md border-0 py-2.5 px-3.5 text-[#151514] bg-white/50 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-custom-primary sm:text-sm sm:leading-6"
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
            className="block text-sm font-medium leading-6 text-[#151514]"
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
              className="block w-full rounded-md border-0 py-2.5 px-3.5 text-[#151514] bg-white/50 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-custom-primary sm:text-sm sm:leading-6"
            />
          </div>
        </AnimatedField>

        {/* 4. Submit Button - Delay shifted to 300 */}
        <AnimatedField delay={300}>
          <div className="flex justify-center">
            <button
              type="submit"
              className="cursor-pointer items-center justify-center overflow-hidden rounded-full h-12 px-8 bg-custom-primary text-[#151514] text-base font-bold leading-normal tracking-[0.015em] hover:bg-[#a69d8f] transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-custom-primary"
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
      {/* Try Now Button */}
      <section className="text-center mb-16">
        <button
          onClick={() => navigate('/chat')}
          className="inline-flex cursor-pointer items-center justify-center overflow-hidden rounded-full h-14 px-8 bg-custom-primary text-[#151514] text-lg font-bold leading-normal tracking-[0.015em] hover:bg-[#a69d8f] transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-custom-primary"
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
          <h2 className="text-3xl font-bold text-[#151514] mb-4">
            We Value Your Input
          </h2>
          <p className="text-lg text-[#797672]">
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
  );
};

export default MainPage;
