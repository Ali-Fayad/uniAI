import { useNavigate } from "react-router-dom";
import { AuthCard } from "../components/AuthCard";

const ForgotPassword = () => {
  const navigate = useNavigate();

  const handleForgotPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    console.log("Sending reset code...");
    navigate("/auth/verification");
  };

  return (
    <AuthCard>
      <div className="flex flex-col gap-2 mb-8 pt-8">
        <p className="text-[#151514] text-4xl font-black tracking-[-0.033em]">Reset Password</p>
        <p className="text-[#797672]">Enter your email to receive a reset code.</p>
      </div>

      <form className="space-y-6" onSubmit={handleForgotPassword}>
        <div className="flex flex-col">
          <label className="flex flex-col w-full">
            <p className="text-[#151514] font-medium pb-2">Email Address</p>
            <input
              type="email"
              placeholder="Enter your email"
              className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514]"
            />
          </label>
        </div>

        {/* Send Code button as text-only */}
<button
  type="submit"
  className="flex w-full items-center justify-center rounded-full h-12 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition"
>
  Send Code
</button>

      </form>

      <p className="text-center text-[#797672] text-sm mt-6">
        Remember your password?{" "}
        <button
          type="button"
          className="text-[#B3AB9C] font-medium hover:text-[#a69d8f] transition-colors ml-1 bg-transparent p-0 border-0"
          onClick={() => navigate("/auth/signin")}
        >
          Sign in
        </button>
      </p>
    </AuthCard>
  );
};

export default ForgotPassword;
