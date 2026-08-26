"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import * as api from "@/lib/api";

export default function AgentExecutionDetailPage() {
  const params = useParams();
  const router = useRouter();
  const agentId = params.id as string;

  const [execution, setExecution] = useState<api.AgentExecutionDetailDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [expandedSteps, setExpandedSteps] = useState<Record<number, boolean>>({});

  useEffect(() => {
    if (!agentId) return;
    api.getExecution(agentId)
      .then((data) => {
        setExecution(data);
        setLoading(false);
        // Auto-expand running and last completed steps
        const expanded: Record<number, boolean> = {};
        data.steps?.forEach((s, i) => {
          if (s.status === "RUNNING" || s.status === "COMPLETED") expanded[i] = true;
        });
        setExpandedSteps(expanded);
      })
      .catch((err) => {
        setError(err instanceof api.ApiError ? err.message : "Failed to load execution");
        setLoading(false);
      });
  }, [agentId]);

  // Poll for updates if running
  useEffect(() => {
    if (!execution || execution.status !== "RUNNING") return;
    const interval = setInterval(async () => {
      try {
        const data = await api.getExecution(agentId);
        setExecution(data);
        if (data.status !== "RUNNING") clearInterval(interval);
      } catch {
        // ignore polling errors
      }
    }, 3000);
    return () => clearInterval(interval);
  }, [execution?.status, agentId]);

  const handleCancel = async () => {
    if (!confirm("Cancel this agent execution?")) return;
    try {
      await api.cancelExecution(agentId);
      const updated = await api.getExecution(agentId);
      setExecution(updated);
    } catch (err) {
      console.error("Failed to cancel:", err);
    }
  };

  const toggleStep = (idx: number) => {
    setExpandedSteps((prev) => ({ ...prev, [idx]: !prev[idx] }));
  };

  const tryParseJson = (s: string | null | undefined): object | null => {
    if (!s) return null;
    try { return JSON.parse(s); } catch { return null; }
  };

  if (loading) {
    return (
      <main className="flex-1 flex items-center justify-center bg-[#121414]">
        <div className="flex items-center gap-3 text-[#6bd8cb]">
          <span className="material-symbols-outlined text-[20px] animate-spin">refresh</span>
          <span className="font-mono-label text-[13px]">Loading execution trace...</span>
        </div>
      </main>
    );
  }

  if (error || !execution) {
    return (
      <main className="flex-1 flex flex-col items-center justify-center bg-[#121414] gap-4">
        <span className="material-symbols-outlined text-[36px] text-[#ffb4ab]">error</span>
        <p className="font-ui-sans-md text-[14px] text-[#ffb4ab]">{error || "Execution not found"}</p>
        <button
          onClick={() => router.push("/agents")}
          className="px-4 py-1.5 bg-[#1e2020] border border-[#3d4947] rounded-[4px] text-[12px] text-[#bcc9c6] hover:text-[#e2e2e2]"
        >
          ← Back to Agents
        </button>
      </main>
    );
  }

  const isRunning = execution.status === "RUNNING";
  const steps = execution.steps || [];

  return (
    <main className="flex-1 p-6 flex flex-col gap-6 overflow-y-auto bg-[#121414]">
      {/* Context Header */}
      <div className="flex flex-col gap-3 bg-[#121414] border border-[#3d4947] rounded-[6px] p-4 sm:p-5 shadow-sm">
        <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-4">
          <div className="flex flex-col gap-1.5">
            <div className="flex items-center gap-2">
              <span
                className={`w-2 h-2 rounded-full ${
                  isRunning ? "bg-[#c0c1ff] animate-pulse" : execution.status === "COMPLETED" ? "bg-[#6bd8cb]" : "bg-[#ffb4ab]"
                }`}
              />
              <span className="font-mono-label text-[11px] text-[#6bd8cb] uppercase tracking-wider">
                Status: {execution.status}
              </span>
            </div>
            <h1 className="font-ui-sans-bold text-[16px] sm:text-[18px] leading-[24px] text-[#e2e2e2] max-w-3xl">
              {execution.goal}
            </h1>
            <div className="font-mono-data text-[12px] text-[#bcc9c6] mt-1 flex flex-wrap items-center gap-x-3 gap-y-1">
              <span>ID: <strong className="text-[#e2e2e2]">{execution.id.slice(0, 12)}...</strong></span>
              <span className="text-[#3d4947]">|</span>
              <span>Iterations: <strong className="text-[#e2e2e2]">{execution.iterations}</strong></span>
              {execution.startedAt && (
                <>
                  <span className="text-[#3d4947]">|</span>
                  <span>Started: <strong className="text-[#e2e2e2]">{new Date(execution.startedAt).toLocaleString()}</strong></span>
                </>
              )}
            </div>
          </div>

          {/* Right Metrics & Controls */}
          <div className="flex flex-wrap items-center gap-4 lg:self-start">
            <div className="flex flex-col items-end gap-0.5 border-r border-[#3d4947] pr-4">
              <span className="font-ui-sans-sm text-[12px] text-[#bcc9c6]">Token Usage</span>
              <span className="font-mono-data text-[13px] text-[#e2e2e2] font-semibold">
                {execution.tokenUsage?.toLocaleString() || "—"}
              </span>
            </div>

            {isRunning && (
              <button
                onClick={handleCancel}
                className="h-[28px] px-2.5 bg-[#121414] border border-[#ffb4ab]/60 text-[#ffb4ab] rounded-[4px] font-ui-sans-bold text-[12px] hover:bg-[#93000a]/20 transition-colors flex items-center gap-1"
              >
                <span className="material-symbols-outlined text-[15px]">cancel</span>
                <span>Cancel Execution</span>
              </button>
            )}

            <button
              onClick={() => router.push("/agents")}
              className="h-[28px] px-2.5 bg-[#1e2020] border border-[#3d4947] text-[#bcc9c6] hover:text-[#e2e2e2] rounded-[4px] font-ui-sans-bold text-[12px] hover:bg-[#282a2a] transition-colors flex items-center gap-1"
            >
              <span className="material-symbols-outlined text-[15px]">arrow_back</span>
              <span>Back</span>
            </button>
          </div>
        </div>

        {/* Result */}
        {execution.result && (
          <div className="mt-2 p-3 bg-[#1a1c1c] border border-[#3d4947] rounded-[4px]">
            <div className="font-mono-label text-[10px] text-[#6bd8cb] uppercase tracking-wider mb-1">Result</div>
            <p className="font-ui-sans-md text-[13px] text-[#e2e2e2] whitespace-pre-wrap">{execution.result}</p>
          </div>
        )}

        {execution.error && (
          <div className="mt-2 p-3 bg-[#93000a]/10 border border-[#ffb4ab]/30 rounded-[4px]">
            <div className="font-mono-label text-[10px] text-[#ffb4ab] uppercase tracking-wider mb-1">Error</div>
            <p className="font-ui-sans-md text-[12px] text-[#ffb4ab] whitespace-pre-wrap">{execution.error}</p>
          </div>
        )}
      </div>

      {/* Execution Timeline Trace */}
      <div className="flex flex-col flex-1 bg-[#121414] border border-[#3d4947] rounded-[6px] p-5 sm:p-6 shadow-sm">
        <div className="flex items-center justify-between mb-6 pb-3 border-b border-[#3d4947]">
          <h2 className="font-ui-sans-bold text-[14px] text-[#e2e2e2] flex items-center gap-2">
            <span className="material-symbols-outlined text-[#6bd8cb] text-[18px]">history</span>
            <span>Execution Trace</span>
          </h2>
          <span className="font-mono-label text-[11px] text-[#bcc9c6]">
            {steps.length} Steps
          </span>
        </div>

        {steps.length === 0 ? (
          <div className="flex-1 flex items-center justify-center text-[#879391] text-[12px]">
            {isRunning ? (
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-[16px] animate-spin text-[#c0c1ff]">refresh</span>
                Agent is working... Steps will appear here.
              </div>
            ) : (
              "No execution steps recorded."
            )}
          </div>
        ) : (
          <div className="flex flex-col ml-3 relative pl-6 sm:pl-8 border-l border-[#3d4947] gap-6 pb-4">
            {steps.map((step, idx) => {
              const isCompleted = step.status === "COMPLETED";
              const isStepRunning = step.status === "RUNNING";
              const isPending = step.status === "PENDING";
              const isFailed = step.status === "FAILED";
              const isExpanded = expandedSteps[idx] || false;
              const inputJson = tryParseJson(step.inputArgs);
              const outputJson = tryParseJson(step.outputResult);

              return (
                <div key={step.id || idx} className="relative group">
                  {/* Node Bullet */}
                  <div
                    className={`absolute -left-[31px] sm:-left-[39px] top-1 w-4 h-4 rounded-full bg-[#121414] border flex items-center justify-center ${
                      isCompleted
                        ? "border-[#6bd8cb]"
                        : isStepRunning
                        ? "border-[#c0c1ff]"
                        : isFailed
                        ? "border-[#ffb4ab]"
                        : "border-[#3d4947]"
                    }`}
                  >
                    {isCompleted && <span className="w-2 h-2 rounded-full bg-[#6bd8cb]" />}
                    {isStepRunning && (
                      <span className="material-symbols-outlined text-[11px] text-[#c0c1ff] animate-spin">refresh</span>
                    )}
                    {isFailed && <span className="w-2 h-2 rounded-full bg-[#ffb4ab]" />}
                  </div>

                  {/* Header Row */}
                  <div
                    onClick={() => toggleStep(idx)}
                    className={`flex items-center justify-between cursor-pointer select-none py-0.5 ${
                      isPending ? "opacity-50" : "opacity-100"
                    }`}
                  >
                    <div className="flex items-center gap-2.5">
                      <span className="font-ui-sans-bold text-[13px] text-[#e2e2e2] group-hover:text-[#6bd8cb] transition-colors">
                        {step.stepName || `Step ${step.stepIndex}`}
                      </span>
                      {step.toolName && (
                        <span className="font-mono-label text-[10px] text-[#c0c1ff] bg-[#3131c0]/20 px-1.5 py-[1px] rounded-[2px]">
                          {step.toolName}
                        </span>
                      )}
                      {isCompleted && (
                        <span className="px-1.5 py-[2px] bg-[#6bd8cb]/10 text-[#6bd8cb] border border-[#6bd8cb]/20 rounded-[2px] font-mono-label text-[10px] uppercase">
                          Completed
                        </span>
                      )}
                      {isStepRunning && (
                        <span className="px-1.5 py-[2px] bg-[#3131c0]/20 text-[#c0c1ff] border border-[#c0c1ff]/30 rounded-[2px] font-mono-label text-[10px] uppercase animate-pulse">
                          Running
                        </span>
                      )}
                      {isFailed && (
                        <span className="px-1.5 py-[2px] bg-[#93000a]/20 text-[#ffb4ab] border border-[#ffb4ab]/30 rounded-[2px] font-mono-label text-[10px] uppercase">
                          Failed
                        </span>
                      )}
                      {isPending && (
                        <span className="px-1.5 py-[2px] bg-[#1e2020] text-[#bcc9c6] border border-[#3d4947] rounded-[2px] font-mono-label text-[10px] uppercase">
                          Pending
                        </span>
                      )}
                    </div>

                    <div className="flex items-center gap-2">
                      {step.durationMs > 0 && (
                        <span className="font-mono-data text-[12px] text-[#bcc9c6]">
                          {step.durationMs < 1000 ? `${step.durationMs}ms` : `${(step.durationMs / 1000).toFixed(1)}s`}
                        </span>
                      )}
                      <span className="material-symbols-outlined text-[16px] text-[#879391] group-hover:text-[#e2e2e2]">
                        {isExpanded ? "expand_less" : "expand_more"}
                      </span>
                    </div>
                  </div>

                  {/* Expanded Details */}
                  {isExpanded && (
                    <div className="mt-2.5 flex flex-col gap-2">
                      {(inputJson || step.inputArgs) && (
                        <div className="bg-[#1e2020] border border-[#3d4947] rounded-[4px] p-3">
                          <div className="font-mono-label text-[11px] text-[#bcc9c6] mb-1 flex items-center gap-1.5">
                            <span className="material-symbols-outlined text-[13px] text-[#6bd8cb]">input</span>
                            <span>inputArgs</span>
                          </div>
                          <pre className="font-code-block text-[12px] text-[#e2e2e2] overflow-x-auto p-2.5 bg-[#121414] border border-[#3d4947] rounded-[4px]">
                            <code>{inputJson ? JSON.stringify(inputJson, null, 2) : step.inputArgs}</code>
                          </pre>
                        </div>
                      )}

                      {(outputJson || step.outputResult) && (
                        <div className="bg-[#1e2020] border border-[#3d4947] rounded-[4px] p-3">
                          <div className="font-mono-label text-[11px] text-[#bcc9c6] mb-1 flex items-center gap-1.5">
                            <span className="material-symbols-outlined text-[13px] text-[#6bd8cb]">output</span>
                            <span>outputResult</span>
                          </div>
                          <pre className="font-code-block text-[12px] text-[#e2e2e2] overflow-x-auto p-2.5 bg-[#121414] border border-[#3d4947] rounded-[4px]">
                            <code>{outputJson ? JSON.stringify(outputJson, null, 2) : step.outputResult}</code>
                          </pre>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </main>
  );
}
