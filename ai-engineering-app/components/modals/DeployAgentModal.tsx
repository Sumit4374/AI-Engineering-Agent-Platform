"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import * as api from "@/lib/api";

interface DeployAgentModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function DeployAgentModal({ isOpen, onClose }: DeployAgentModalProps) {
  const router = useRouter();
  const [goal, setGoal] = useState("");
  const [maxIterations, setMaxIterations] = useState("10");
  const [allowRag, setAllowRag] = useState(true);
  const [isDeploying, setIsDeploying] = useState(false);
  const [error, setError] = useState("");

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsDeploying(true);
    try {
      const result = await api.executeAgent(
        goal,
        maxIterations ? parseInt(maxIterations) : undefined,
        allowRag
      );
      setIsDeploying(false);
      onClose();
      setGoal("");
      // Navigate to the execution detail
      router.push(`/agents/${result.id}`);
    } catch (err) {
      setError(err instanceof api.ApiError ? err.message : "Failed to deploy agent");
      setIsDeploying(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-xs p-4 animate-in fade-in duration-150">
      <div className="w-full max-w-lg bg-[#1a1c1c] border border-[#3d4947] rounded-[6px] shadow-2xl overflow-hidden flex flex-col">
        {/* Modal Header */}
        <div className="px-4 py-3 border-b border-[#3d4947] flex items-center justify-between bg-[#121414]">
          <div className="flex items-center gap-2">
            <span className="material-symbols-outlined text-[#6bd8cb] text-[18px]">smart_toy</span>
            <h3 className="font-ui-sans-bold text-[14px] text-[#e2e2e2]">Deploy Autonomous Agent</h3>
          </div>
          <button
            onClick={onClose}
            className="text-[#bcc9c6] hover:text-[#e2e2e2] transition-colors"
          >
            <span className="material-symbols-outlined text-[18px]">close</span>
          </button>
        </div>

        {/* Modal Body */}
        <form onSubmit={handleSubmit} className="p-4 space-y-4 text-[13px]">
          <div>
            <label className="block font-mono-label text-[11px] uppercase tracking-wider text-[#bcc9c6] mb-1">
              Agent Goal / Objective
            </label>
            <textarea
              rows={3}
              required
              value={goal}
              onChange={(e) => setGoal(e.target.value)}
              maxLength={2000}
              placeholder="Describe what the agent should accomplish, e.g., Analyze database performance and suggest index optimizations..."
              className="w-full bg-[#121414] border border-[#3d4947] rounded-[4px] p-2.5 text-[#e2e2e2] font-ui-sans-md text-[12px] placeholder-[#879391]/60 focus:outline-none focus:border-[#6bd8cb] resize-none"
            />
            <div className="text-right font-mono-label text-[10px] text-[#879391] mt-0.5">
              {goal.length}/2000
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-mono-label text-[11px] uppercase tracking-wider text-[#bcc9c6] mb-1">
                Max Iterations
              </label>
              <input
                type="number"
                min="1"
                max="50"
                value={maxIterations}
                onChange={(e) => setMaxIterations(e.target.value)}
                className="w-full bg-[#121414] border border-[#3d4947] rounded-[4px] px-3 py-1.5 text-[#e2e2e2] font-mono-data text-[12px] focus:outline-none focus:border-[#6bd8cb]"
              />
            </div>

            <div className="flex items-end">
              <label className="flex items-center gap-2 cursor-pointer pb-1.5">
                <input
                  type="checkbox"
                  checked={allowRag}
                  onChange={(e) => setAllowRag(e.target.checked)}
                  className="accent-[#6bd8cb] rounded"
                />
                <span className="font-ui-sans-md text-[12px] text-[#bcc9c6]">
                  Allow RAG context
                </span>
              </label>
            </div>
          </div>

          {error && (
            <div className="flex items-start gap-2 p-2.5 bg-[#93000a]/20 border border-[#ffb4ab]/30 rounded-[4px]">
              <span className="material-symbols-outlined text-[#ffb4ab] text-[15px] mt-0.5 shrink-0">error</span>
              <p className="font-ui-sans-sm text-[#ffb4ab] text-[11px]">{error}</p>
            </div>
          )}

          {/* Modal Actions */}
          <div className="flex items-center justify-end gap-2 pt-2 border-t border-[#3d4947]">
            <button
              type="button"
              onClick={onClose}
              className="px-3 py-1.5 rounded-[4px] border border-[#3d4947] text-[#bcc9c6] hover:bg-[#282a2a] hover:text-[#e2e2e2] text-[12px] font-medium"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isDeploying || !goal.trim()}
              className="px-4 py-1.5 rounded-[4px] bg-[#6bd8cb] text-[#003732] hover:bg-[#89f5e7] text-[12px] font-ui-sans-bold flex items-center gap-1.5 shadow-sm disabled:opacity-50"
            >
              {isDeploying ? (
                <>
                  <span className="material-symbols-outlined text-[14px] animate-spin">sync</span>
                  <span>Deploying...</span>
                </>
              ) : (
                <>
                  <span className="material-symbols-outlined text-[14px]">rocket_launch</span>
                  <span>Execute Agent</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
