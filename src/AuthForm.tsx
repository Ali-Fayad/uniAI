import React, { useState } from "react";

const AuthApp = () => {
  const [showForm, setShowForm] = useState(false);
  const [loadingProvider, setLoadingProvider] = useState<"google" | "github" | null>(null);

  // 1️⃣ Email/Password Form
  if (showForm) {
    return (
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
            <div className="flex flex-col">
              <label className="flex flex-col w-full">
                <p className="text-[#151514] text-base font-medium leading-normal pb-2">
                  Email Address
                </p>
                <input
                  type="email"
                  placeholder="Enter your email"
                  className="form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-xl text-[#151514] focus:outline-none focus:ring-2 focus:ring-custom-primary/50 border border-custom-secondary/50 bg-white h-14 p-[15px] text-base font-normal"
                />
              </label>
            </div>

            <div className="flex flex-col">
              <label className="flex flex-col w-full">
                <div className="flex justify-between items-center pb-2">
                  <p className="text-[#151514] text-base font-medium leading-normal">
                    Password
                  </p>
                  <a
                    href="#"
                    className="text-[#797672] text-sm font-normal leading-normal underline hover:text-[#151514] transition-colors"
                  >
                    Forgot Password?
                  </a>
                </div>
                <input
                  type="password"
                  placeholder="Enter your password"
                  className="form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-xl text-[#151514] focus:outline-none focus:ring-2 focus:ring-custom-primary/50 border border-custom-secondary/50 bg-white h-14 p-[15px] text-base font-normal"
                />
              </label>
            </div>

            <div>
              <button className="flex w-full items-center justify-center rounded-full h-12 px-5 bg-custom-primary text-[#151514] text-base font-bold hover:bg-[#a69d8f] transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-custom-primary">
                Sign In
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  }

  // 2️⃣ Loading State for Social Login
  if (loadingProvider) {
    const providerName = loadingProvider === "google" ? "Google" : "GitHub";
    const spinnerColor =
      loadingProvider === "google" ? "border-red-500" : "border-gray-800";

    return (
      <div className="col-span-1 lg:col-span-2 flex flex-col items-center justify-center p-6 sm:p-12">
        <div className="w-full max-w-md bg-white/50 backdrop-blur-sm p-8 sm:p-10 rounded-3xl shadow-lg border border-white/20 flex flex-col items-center justify-center">
          <span className={`animate-spin w-12 h-12 border-4 ${spinnerColor} border-t-transparent rounded-full mb-6`}></span>
          <p className="text-[#151514] text-xl font-bold">
            Redirecting with {providerName}...
          </p>
          <p className="text-[#797672] text-sm mt-2 text-center">
            Please wait while we log you in.
          </p>
        </div>
      </div>
    );
  }

  // 3️⃣ Default Auth Buttons Page
  return (
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
              onClick={() => setShowForm(true)}
              className="flex w-full cursor-pointer items-center justify-center overflow-hidden rounded-full h-12 px-5 bg-custom-primary text-[#151514] text-base font-bold hover:bg-[#a69d8f] transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-custom-primary"
            >
              Sign In
            </button>
          </div>
        </form>

        {/* Divider */}
        <div className="relative my-8">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-custom-secondary/50"></div>
          </div>
          <div className="relative flex justify-center text-sm">
            <span className="bg-white/50 px-2 text-sm text-[#797672]">or</span>
          </div>
        </div>

        {/* Social Buttons */}
        <div className="space-y-4">
          <button
            type="button"
            onClick={() => setLoadingProvider("google")}
            className="flex w-full items-center justify-center gap-3 rounded-full border border-custom-secondary/50 bg-white h-12 px-5 text-[#151514] hover:bg-gray-50 transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-custom-secondary"
          >
            Google
          </button>

          <button
            type="button"
            onClick={() => setLoadingProvider("github")}
            className="flex w-full items-center justify-center gap-3 rounded-full border border-custom-secondary/50 bg-white h-12 px-5 text-[#151514] hover:bg-gray-50 transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-custom-secondary"
          >
            GitHub
          </button>
        </div>
      </div>
    </div>
  );
};

export default AuthApp;
