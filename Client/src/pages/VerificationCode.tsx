import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthCard } from "../components/AuthCard";
import { AuthService } from "../api/AuthService";
import { TokenStorage } from "../utils/storage";

const VerificationCode = () => {
  const navigate = useNavigate();
  const [code, setCode] = useState("");
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Prefill email from TokenStorage (saved during sign-in / sign-up flow)
  useEffect(() => {
    const savedEmail =
      (typeof TokenStorage.getEmailForVerification === "function"
        ? TokenStorage.getEmailForVerification()
        : undefined) || "";
    setEmail(savedEmail);
  }, []);

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (code.length !== 6) {
      setError("Verification code must be 6 characters.");
      return;
    }

    if (!email) {
      setError("Email is missing.  Please go back and request a new code.");
      return;
    }

    setLoading(true);
    try {
      const response = await AuthService.verifyCode({
        email,
        verificationCode: code,
      });

      // Successful verification (200) -> token available
      if (response.status === 200 && response.token) {
        TokenStorage.saveToken(response.token);
        navigate("/chat");
        return;
      }

      // Show backend message when available
      setError(response.message || "Invalid verification code. Please try again.");
    } catch (err) {
      console.error("Verification failed:", err);
      setError("Verification failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setError(null);

    if (!email) {
      setError("Email is missing. Please go back and request a new code.");
      return;
    }

    setLoading(true);
    try {
      await AuthService.resendCode(email);
      console.log("Verification code resent!");
    } catch (err:  any) {
      console.error("Resend failed:", err);
      const backendMessage = err?. response?.data?.message;
      setError(backendMessage || "Failed to resend code. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthCard>
      <div className="flex flex-col gap-2 mb-8 pt-8">
        <p className="text-[#151514] text-4xl font-black tracking-[-0.033em]">
          Verification
        </p>
        <p className="text-[#797672]">
          Enter the 6-character code sent to your email.
        </p>
      </div>

      <form className="space-y-6" onSubmit={handleVerify}>
        <div className="flex flex-col">
          <label className="flex flex-col w-full">
            <p className="text-[#151514] font-medium pb-2">Code</p>
            <input
              type="text"
              placeholder="ABC123"
              value={code}
              onChange={(e) =>
                setCode(
                  e.target.value
                    .toUpperCase()
                    .replace(/[^A-Z0-9]/g, "")
                    .slice(0, 6)
                )
              }
              className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514] tracking-widest text-lg"
              maxLength={6}
              required
            />
          </label>
        </div>

        {/* show the email being verified so user can confirm it */}
        {email && (
          <p className="text-sm text-[#797672]">
            Verifying email: <span className="font-medium text-[#151514]">{email}</span>
          </p>
        )}

        {error && <p className="text-red-500 text-sm">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="flex w-full items-center justify-center rounded-full h-12 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition disabled:opacity-50"
        >
          {loading ? "Verifying..." : "Verify"}
        </button>
      </form>

      <p className="text-center text-[#797672] text-sm mt-6">
        Didn't receive code?{" "}
        <button
          type="button"
          onClick={handleResend}
          disabled={loading}
          className="text-[#B3AB9C] font-medium hover:text-[#a69d8f] transition-colors ml-1 bg-transparent p-0 border-0"
        >
          Resend
        </button>
      </p>
    </AuthCard>
  );
};

export default VerificationCode;
