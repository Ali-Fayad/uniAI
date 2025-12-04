import { AuthCard } from "../components/AuthCard";

const VerificationCode = () => {
  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    console.log("Verifying code... (Staying on page for demo)");
  };

  return (
    <AuthCard>
      <div className="flex flex-col gap-2 mb-8 pt-8">
        <p className="text-[#151514] text-4xl font-black tracking-[-0.033em]">
          Verification
        </p>
        <p className="text-[#797672]">
          Enter the 6-digit code sent to your email.
        </p>
      </div>

      <form className="space-y-6" onSubmit={handleVerify}>
        <div className="flex flex-col">
          <label className="flex flex-col w-full">
            <p className="text-[#151514] font-medium pb-2">Code</p>
            <input
              type="text"
              placeholder="123456"
              className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514] tracking-widest text-lg"
              maxLength={6}
            />
          </label>
        </div>

        {/* Verify button as plain text or background (you can pick) */}
        <button
          type="submit"
          className="flex w-full items-center justify-center rounded-full h-12 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition"
        >
          Verify
        </button>
      </form>

      <p className="text-center text-[#797672] text-sm mt-6">
        Didn't receive code?{" "}
        <button
          type="button"
          className="text-[#B3AB9C] font-medium hover:text-[#a69d8f] transition-colors ml-1 bg-transparent p-0 border-0"
          onClick={() => console.log("Resend code")}
        >
          Resend
        </button>
      </p>
    </AuthCard>
  );
};

export default VerificationCode;
