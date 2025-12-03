import { useState } from "react";

type AuthView =
  | "default"
  | "signin"
  | "signup"
  | "google"
  | "github"
  | "forgotPassword"
  | "verificationCode";

/* -------------------------------------------------------
   SIGN IN FORM
------------------------------------------------------- */
export const renderSignInForm = (setView: (v: AuthView) => void) => (
  <div className="col-span-1 lg:col-span-2 flex flex-col items-center justify-center p-6 sm:p-12">
    <div className="w-full max-w-md bg-white/50 backdrop-blur-sm p-8 sm:p-10 rounded-3xl shadow-lg border border-white/20">

      {/* Heading */}
      <div className="flex flex-col gap-2 mb-8">
        <p className="text-[#151514] text-4xl font-black tracking-[-0.033em]">
          Welcome Back
        </p>
        <p className="text-[#797672] text-base">Sign in to continue to your dashboard.</p>
      </div>

      {/* Sign In Form */}
      <form className="space-y-6" onSubmit={(e) => e.preventDefault()}>
        <div className="flex flex-col">
          <label className="flex flex-col w-full">
            <p className="text-[#151514] font-medium pb-2">Email Address</p>
            <input
              type="email"
              placeholder="Enter your email"
              className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white h-14 px-[15px] text-[#151514]"
            />
          </label>
        </div>

        <div className="flex flex-col">
          <label className="flex flex-col w-full">
            <div className="flex justify-between items-center pb-2">
              <p className="text-[#151514] font-medium">Password</p>
              <button
                type="button"
                onClick={() => setView("forgotPassword")}
                className="text-[#797672] text-sm underline hover:text-[#151514]"
              >
                Forgot Password?
              </button>
            </div>
            <input
              type="password"
              placeholder="Enter your password"
              className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white h-14 px-[15px] text-[#151514]"
            />
          </label>
        </div>

        <div>
          <button className="flex w-full items-center justify-center rounded-full h-12 px-5 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition">
            Sign In
          </button>
        </div>
      </form>

      {/* Go to Sign Up */}
      <p className="text-center text-[#797672] text-sm mt-6">
        Don’t have an account?{" "}
        <button
          className="underline hover:text-[#151514]"
          onClick={() => setView("signup")}
        >
          Create one
        </button>
      </p>
    </div>
  </div>
);

/* -------------------------------------------------------
   SIGN UP FORM
------------------------------------------------------- */
export const renderSignUpForm = (setView: (v: AuthView) => void) => (
  <div className="col-span-1 lg:col-span-2 flex flex-col items-center justify-center p-6 sm:p-12">
    <div className="w-full max-w-md bg-white/50 backdrop-blur-sm p-8 sm:p-10 rounded-3xl shadow-lg border border-white/20">

      <div className="flex flex-col gap-2 mb-8">
        <p className="text-[#151514] text-4xl font-black tracking-[-0.033em]">Create Account</p>
        <p className="text-[#797672]">Join and save your chats securely.</p>
      </div>

      <form className="space-y-6" onSubmit={(e) => e.preventDefault()}>
        <div className="grid grid-cols-2 gap-4">
          <input type="text" placeholder="First Name" className="form-input rounded-xl border border-custom-secondary/50 bg-white h-14 px-[15px]" />
          <input type="text" placeholder="Last Name" className="form-input rounded-xl border border-custom-secondary/50 bg-white h-14 px-[15px]" />
        </div>

        <input type="email" placeholder="Email Address" className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white h-14 px-[15px]" />
        <input type="text" placeholder="Username" className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white h-14 px-[15px]" />
        <input type="password" placeholder="Password" className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white h-14 px-[15px]" />
        <input type="password" placeholder="Confirm Password" className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white h-14 px-[15px]" />

        <button className="flex w-full items-center justify-center rounded-full h-12 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition">
          Create Account
        </button>
      </form>

      <p className="text-center text-[#797672] text-sm mt-6">
        Already have an account?{" "}
        <button
          className="underline hover:text-[#151514]"
          onClick={() => setView("signin")}
        >
          Sign in
        </button>
      </p>
    </div>
  </div>
);

/* -------------------------------------------------------
   GOOGLE / GITHUB LOADING
------------------------------------------------------- */
export const renderOAuthLoading = (view: AuthView) => {
  const spinnerColor = view === "google" ? "border-red-500" : "border-gray-800";

  return (
    <div className="col-span-1 lg:col-span-2 flex flex-col items-center justify-center p-6 sm:p-12">
      <div className="w-full max-w-md bg-white/50 backdrop-blur-sm p-8 sm:p-10 rounded-3xl shadow-lg border border-white/20 flex flex-col items-center">

        <span className={`animate-spin w-12 h-12 border-4 ${spinnerColor} border-t-transparent rounded-full mb-6`}></span>

        <p className="text-[#151514] text-xl font-bold">
          Redirecting with {view}...
        </p>
        <p className="text-[#797672] text-sm mt-2 text-center">
          Please wait while we log you in.
        </p>
      </div>
    </div>
  );
};

/* -------------------------------------------------------
   DEFAULT BUTTONS (original)
------------------------------------------------------- */
export const renderDefaultView = (setView: (v: AuthView) => void) => (
  <div className="col-span-1 lg:col-span-2 flex flex-col items-center justify-center p-6 sm:p-12">
    <div className="w-full max-w-md bg-white/50 backdrop-blur-sm p-8 sm:p-10 rounded-3xl shadow-lg border border-white/20">

      {/* Heading */}
      <div className="flex flex-col gap-2 mb-8">
        <p className="text-[#151514] text-4xl font-black tracking-[-0.033em]">
          Welcome Back
        </p>
        <p className="text-[#797672]">Sign in to continue to your dashboard.</p>
      </div>

      {/* Sign In Button */}
      <button
        type="button"
        onClick={() => setView("signin")}
        className="flex w-full items-center justify-center rounded-full h-12 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition"
      >
        Sign In
      </button>

      {/* Divider */}
      <div className="relative my-8">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-custom-secondary/50"></div>
        </div>
        <div className="relative flex justify-center text-sm">
          <span className="bg-white/50 px-2 text-[#797672]">or</span>
        </div>
      </div>

      {/* Sign Up Button */}
      <button
        type="button"
        onClick={() => setView("signup")}
        className="flex w-full items-center justify-center rounded-full h-12 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition"
      >
        Sign Up
      </button>

      {/* Divider */}
      <div className="relative my-8">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-custom-secondary/50"></div>
        </div>
        <div className="relative flex justify-center text-sm">
          <span className="bg-white/50 px-2 text-[#797672]">or</span>
        </div>
      </div>

      {/* Social Buttons */}
      <div className="space-y-4">
        <button
          onClick={() => setView("google")}
          className="flex w-full items-center justify-center rounded-full border border-custom-secondary/50 bg-white h-12 hover:bg-gray-50"
        >
          Google
        </button>

        <button
          onClick={() => setView("github")}
          className="flex w-full items-center justify-center rounded-full border border-custom-secondary/50 bg-white h-12 hover:bg-gray-50"
        >
          GitHub
        </button>
      </div>
    </div>
  </div>
);

/* -------------------------------------------------------
   MAIN COMPONENT
------------------------------------------------------- */
const AuthApp = () => {
  const [View, setView] = useState<AuthView>("default");

  switch (View) {
    case "signin":
      return renderSignInForm(setView);
    case "signup":
      return renderSignUpForm(setView);
    case "google":
    case "github":
      return renderOAuthLoading(View);
    default:
      return renderDefaultView(setView);
  }
};

export default AuthApp;
