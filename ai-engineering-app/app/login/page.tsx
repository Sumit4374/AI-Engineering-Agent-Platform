"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { ApiError } from "@/lib/api";

type Mode = "login" | "signup";

export default function LoginPage() {
  const [mode, setMode] = useState<Mode>("login");
  const [loginId, setLoginId] = useState("");
  const [userName, setUserName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { login, signup, isAuthenticated, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      router.replace("/");
    }
  }, [isAuthenticated, isLoading, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsSubmitting(true);
    try {
      if (mode === "login") {
        await login(loginId, password);
      } else {
        await signup(userName, email, password);
      }
      router.replace("/");
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message || "Authentication failed. Please check your credentials.");
      } else {
        setError("Network error — is the backend running at localhost:8080?");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#121414] flex items-center justify-center p-4">
      {/* Background grid pattern */}
      <div
        className="absolute inset-0 opacity-[0.03]"
        style={{
          backgroundImage:
            "linear-gradient(#6bd8cb 1px, transparent 1px), linear-gradient(90deg, #6bd8cb 1px, transparent 1px)",
          backgroundSize: "40px 40px",
        }}
      />

      <div className="relative w-full max-w-sm">
        {/* Brand */}
        <div className="flex items-center gap-3 mb-8 justify-center">
          <div className="w-10 h-10 rounded-[6px] bg-[#6bd8cb] flex items-center justify-center text-[#003732] shadow-lg shadow-[#6bd8cb]/20">
            <span className="material-symbols-outlined fill text-[22px]">terminal</span>
          </div>
          <div>
            <div className="font-ui-sans-bold text-[16px] text-[#6bd8cb] tracking-wide">
              AIEngine Console
            </div>
            <div className="font-mono-label text-[10px] text-[#879391]">
              Autonomous AI Engineering Platform
            </div>
          </div>
        </div>

        {/* Card */}
        <div className="bg-[#1a1c1c] border border-[#3d4947] rounded-[8px] shadow-2xl overflow-hidden">
          {/* Tab Header */}
          <div className="flex border-b border-[#3d4947]">
            {(["login", "signup"] as Mode[]).map((m) => (
              <button
                key={m}
                onClick={() => { setMode(m); setError(""); }}
                className={`flex-1 py-3 font-ui-sans-bold text-[13px] transition-colors ${
                  mode === m
                    ? "text-[#6bd8cb] border-b-2 border-[#6bd8cb] bg-[#121414]"
                    : "text-[#879391] hover:text-[#bcc9c6]"
                }`}
              >
                {m === "login" ? "Sign In" : "Create Account"}
              </button>
            ))}
          </div>

          <form onSubmit={handleSubmit} className="p-5 space-y-4">
            {mode === "signup" && (
              <div>
                <label className="block font-mono-label text-[11px] uppercase tracking-wider text-[#bcc9c6] mb-1.5">
                  Username
                </label>
                <input
                  type="text"
                  required
                  value={userName}
                  onChange={(e) => setUserName(e.target.value)}
                  placeholder="e.g. sumit"
                  className="w-full bg-[#121414] border border-[#3d4947] rounded-[4px] px-3 py-2 text-[13px] text-[#e2e2e2] placeholder-[#879391]/50 focus:outline-none focus:border-[#6bd8cb] transition-colors"
                />
              </div>
            )}

            <div>
              <label className="block font-mono-label text-[11px] uppercase tracking-wider text-[#bcc9c6] mb-1.5">
                {mode === "login" ? "Username or Email" : "Email"}
              </label>
              <input
                type={mode === "login" ? "text" : "email"}
                required
                value={mode === "login" ? loginId : email}
                onChange={(e) =>
                  mode === "login" ? setLoginId(e.target.value) : setEmail(e.target.value)
                }
                placeholder={mode === "login" ? "username or email" : "you@example.com"}
                className="w-full bg-[#121414] border border-[#3d4947] rounded-[4px] px-3 py-2 text-[13px] text-[#e2e2e2] placeholder-[#879391]/50 focus:outline-none focus:border-[#6bd8cb] transition-colors"
              />
            </div>

            <div>
              <label className="block font-mono-label text-[11px] uppercase tracking-wider text-[#bcc9c6] mb-1.5">
                Password
              </label>
              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder={mode === "signup" ? "min. 8 characters" : "••••••••"}
                  className="w-full bg-[#121414] border border-[#3d4947] rounded-[4px] px-3 py-2 pr-9 text-[13px] text-[#e2e2e2] placeholder-[#879391]/50 focus:outline-none focus:border-[#6bd8cb] transition-colors"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-2.5 top-1/2 -translate-y-1/2 text-[#879391] hover:text-[#bcc9c6]"
                >
                  <span className="material-symbols-outlined text-[16px]">
                    {showPassword ? "visibility_off" : "visibility"}
                  </span>
                </button>
              </div>
            </div>

            {error && (
              <div className="flex items-start gap-2 p-2.5 bg-[#93000a]/20 border border-[#ffb4ab]/30 rounded-[4px]">
                <span className="material-symbols-outlined text-[#ffb4ab] text-[15px] mt-0.5 shrink-0">
                  error
                </span>
                <p className="font-ui-sans-sm text-[#ffb4ab] text-[11px] leading-relaxed">
                  {error}
                </p>
              </div>
            )}

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full h-[38px] bg-[#6bd8cb] text-[#003732] font-ui-sans-bold text-[13px] rounded-[4px] hover:bg-[#89f5e7] active:scale-[0.98] transition-all flex items-center justify-center gap-2 shadow-lg shadow-[#6bd8cb]/10 disabled:opacity-60"
            >
              {isSubmitting ? (
                <>
                  <span className="material-symbols-outlined text-[16px] animate-spin">refresh</span>
                  <span>{mode === "login" ? "Signing in..." : "Creating account..."}</span>
                </>
              ) : (
                <>
                  <span className="material-symbols-outlined text-[16px]">
                    {mode === "login" ? "login" : "person_add"}
                  </span>
                  <span>{mode === "login" ? "Sign In" : "Create Account"}</span>
                </>
              )}
            </button>
          </form>

          <div className="px-5 pb-4 text-center">
            <p className="font-mono-label text-[10px] text-[#879391]">
              Backend: <span className="text-[#6bd8cb]">localhost:8080</span> · JWT Auth
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
