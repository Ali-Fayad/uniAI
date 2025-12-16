import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { AuthCard } from "../../../components/AuthCard";
import { authService } from "../../../services/auth";
import { useAuth } from "../../../hooks/useAuth";
import type { VerifyDto } from "../../../types/dto";

const Verify = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { login } = useAuth();
  const [code, setCode] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  // Get email from navigation state
  const email = (location.state as { email?: string })?.email || "";

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!email) {
      setError("Email is required. Please start the signup process again.");
      return;
    }

    // validate code as a 6-character alphanumeric string (server uses codes like ABCD12)
    const trimmedCode = code.trim();
    if (!/^([A-Za-z0-9]){6}$/.test(trimmedCode)) {
      setError("Please enter the 6-character verification code.");
      return;
    }

    setError("");
    setIsLoading(true);

    try {
      // backend expects field name `verificationCode`
      const payload = { email, verificationCode: trimmedCode } as VerifyDto;
      const response = await authService.verify(payload);

      // Store token (AuthContext will extract user from token if not provided)
      login(response.token);
      // Redirect to chat after successful verification
      navigate('/chat');
    } catch (err: unknown) {
      if (err && typeof err === "object" && "response" in err) {
        const axiosError = err as {
          response?: { data?: { message?: string } };
        };
        setError(
          axiosError.response?.data?.message ||
            "Invalid verification code. Please try again."
        );
      } else {
        setError("An unexpected error occurred.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleResend = async () => {
    if (!email) return;

    try {
      // Resend verification by calling signup again (backend should handle this)
      console.log("Resending verification code to:", email);
      // TODO: Implement resend endpoint if available
    } catch (err) {
      console.error("Failed to resend code:", err);
    }
  };

  return (
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-custom-light py-12 px-4">
      <AuthCard>
        <div className="flex flex-col gap-2 mb-8 pt-8">
          <p className="text-[#151514] text-4xl font-black tracking-[-0.033em]">
            Verification
          </p>
          <p className="text-[#797672]">
            Enter the 6-digit code sent to {email || "your email"}.
          </p>
        </div>

        <form className="space-y-6" onSubmit={handleVerify}>
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          <div className="flex flex-col">
            <label className="flex flex-col w-full">
              <p className="text-[#151514] font-medium pb-2">Code</p>
              <input
                type="text"
                inputMode="numeric"
                pattern="[A-Za-z0-9]{6}"
                autoComplete="one-time-code"
                placeholder="123456"
                value={code}
                onChange={(e) =>
                  setCode(e.target.value.replace(/[^A-Za-z0-9]/g, "").slice(0, 6))
                }
                required
                maxLength={6}
                className="form-input w-full rounded-xl border border-custom-secondary/50 bg-white/50 backdrop-blur-sm h-14 px-[15px] text-[#151514] tracking-widest text-lg"
              />
            </label>
          </div>

          <button
            type="submit"
            disabled={isLoading || code.length !== 6}
            className="flex w-full items-center justify-center rounded-full h-12 bg-custom-primary text-[#151514] font-bold hover:bg-[#a69d8f] transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? "Verifying..." : "Verify"}
          </button>
        </form>

        <p className="text-center text-[#797672] text-sm mt-6">
          Didn't receive code?{" "}
          <button
            type="button"
            className="text-[#B3AB9C] font-medium hover:text-[#a69d8f] transition-colors ml-1 bg-transparent p-0 border-0"
            onClick={handleResend}
          >
            Resend
          </button>
        </p>
      </AuthCard>
    </div>
  );
};

export default Verify;
