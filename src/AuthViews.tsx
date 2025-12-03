// AuthViews.tsx
import React from "react";

type AuthViewProps = {
  onSignInClick: () => void;
};

export const DefaultButtons = ({ onSignInClick }: AuthViewProps) => (
  <div className="col-span-1 lg:col-span-2 flex flex-col items-center justify-center p-6 sm:p-12">
    <div className="w-full max-w-md bg-white/50 backdrop-blur-sm p-8 sm:p-10 rounded-3xl shadow-lg border border-white/20">
      {/* Heading */}
      <div className="flex flex-col gap-2 mb-8">
        <p className="text-[#151514] text-4xl font-black leading-tight tracking-[-0.033em]">
          Welcome Back
        </p>
        <p className="text-[#797672] text-base font-normal leading-normal">
          Sign in to continue to your dashboard.
        </p>
      </div>

      {/* Sign In Button */}
      <form className="space-y-6">
        <div>
          <button
            type="button"
            onClick={onSignInClick}
            className="flex w-full cursor-pointer items-center justify-center overflow-hidden rounded-full h-12 px-5 bg-custom-primary text-[#151514] text-base font-bold hover:bg-[#a69d8f] transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-custom-primary"
          >
            Sign In
          </button>
        </div>
      </form>
    </div>
  </div>
);

export const SignInForm = () => (
  <div className="col-span-1 lg:col-span-2 flex flex-col items-center justify-center p-6 sm:p-12">
    <div className="w-full max-w-md bg-white/50 backdrop-blur-sm p-8 sm:p-10 rounded-3xl shadow-lg border border-white/20">
      {/* Heading */}
      <div className="flex flex-col gap-2 mb-8">
        <p className="text-[#151514] text-4xl font-black leading-tight tracking-[-0.033em]">
          Welcome Back
        </p>
        <p className="text-[#797672] text-base font-normal leading-normal">
          Sign in to continue to your dashboard.
        </p>
      </div>

      {/* Email/Password Form */}
      <form className="space-y-6" onSubmit={(e) => e.preventDefault()}>
        {/* ...inputs... */}
      </form>
    </div>
  </div>
);
