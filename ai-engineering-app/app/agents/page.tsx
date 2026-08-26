"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import * as api from "@/lib/api";

export default function AgentsListPage() {
  const [executions, setExecutions] = useState<api.AgentExecutionDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterQuery, setFilterQuery] = useState("");

  useEffect(() => {
    api.listExecutions()
      .then((data) => {
        setExecutions(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  const handleCancel = async (id: string) => {
    if (!confirm("Cancel this agent execution?")) return;
    try {
      const updated = await api.cancelExecution(id);
      setExecutions((prev) => prev.map((e) => (e.id === id ? { ...e, status: updated.status } : e)));
    } catch (err) {
      console.error("Failed to cancel:", err);
    }
  };

  const filteredExecutions = executions.filter(
    (e) => e.goal.toLowerCase().includes(filterQuery.toLowerCase())
  );

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "RUNNING":
        return (
          <span className="inline-flex items-center gap-1.5 px-2 py-[2px] rounded-[2px] bg-[#c0c1ff]/10 text-[#c0c1ff] font-mono-label text-[10px] border border-[#c0c1ff]/20">
            <span className="material-symbols-outlined text-[11px] animate-spin">sync</span>
            RUNNING
          </span>
        );
      case "COMPLETED":
        return (
          <span className="inline-flex items-center gap-1.5 px-2 py-[2px] rounded-[2px] bg-[#6bd8cb]/10 text-[#6bd8cb] font-mono-label text-[10px] border border-[#6bd8cb]/20">
            <span className="w-1.5 h-1.5 rounded-full bg-[#6bd8cb]" />
            COMPLETED
          </span>
        );
      case "FAILED":
        return (
          <span className="inline-flex items-center gap-1.5 px-2 py-[2px] rounded-[2px] bg-[#93000a]/20 text-[#ffb4ab] font-mono-label text-[10px] border border-[#ffb4ab]/30">
            <span className="w-1.5 h-1.5 rounded-full bg-[#ffb4ab]" />
            FAILED
          </span>
        );
      case "CANCELLED":
        return (
          <span className="inline-flex items-center gap-1.5 px-2 py-[2px] rounded-[2px] bg-[#1e2020] text-[#bcc9c6] font-mono-label text-[10px] border border-[#3d4947]">
            CANCELLED
          </span>
        );
      default:
        return <span className="font-mono-label text-[10px] text-[#879391]">{status}</span>;
    }
  };

  return (
    <main className="flex-1 overflow-y-auto p-6 flex flex-col gap-6 bg-[#121414]">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="font-ui-sans-bold text-[20px] text-[#e2e2e2] leading-tight">
            Agent Executions
          </h1>
          <p className="font-ui-sans-md text-[13px] text-[#bcc9c6] mt-0.5">
            Monitor and manage autonomous agent task executions.
          </p>
        </div>
      </div>

      {/* Filter */}
      <div className="flex items-center bg-[#121414] border border-[#3d4947] p-3 rounded-[6px]">
        <div className="relative w-full sm:w-96">
          <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-[#879391] text-[16px]">search</span>
          <input
            type="text"
            value={filterQuery}
            onChange={(e) => setFilterQuery(e.target.value)}
            placeholder="Search by goal..."
            className="w-full h-8 bg-[#1a1c1c] border border-[#3d4947] rounded-[4px] pl-8 pr-3 text-[12px] font-ui-sans-md text-[#e2e2e2] placeholder-[#879391]/60 focus:outline-none focus:border-[#6bd8cb]"
          />
        </div>
      </div>

      {/* Table */}
      <div className="border border-[#3d4947] rounded-[6px] bg-[#121414] overflow-hidden flex flex-col shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse whitespace-nowrap">
            <thead>
              <tr className="border-b border-[#3d4947] bg-[#1a1c1c] text-[#bcc9c6] font-mono-label text-[11px] uppercase tracking-wider">
                <th className="py-2.5 px-4 font-medium w-2/5">Goal</th>
                <th className="py-2.5 px-4 font-medium">Status</th>
                <th className="py-2.5 px-4 font-medium text-right">Iterations</th>
                <th className="py-2.5 px-4 font-medium text-right">Tokens</th>
                <th className="py-2.5 px-4 font-medium text-right">Started</th>
                <th className="py-2.5 px-4 font-medium w-20 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#3d4947]/70 font-ui-sans-md text-[13px] text-[#e2e2e2]">
              {loading ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-[#879391] text-[12px]">
                    <span className="material-symbols-outlined text-[16px] animate-spin align-middle mr-2">refresh</span>
                    Loading executions...
                  </td>
                </tr>
              ) : filteredExecutions.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-[#879391] text-[12px]">
                    No agent executions found. Deploy an agent to get started.
                  </td>
                </tr>
              ) : (
                filteredExecutions.map((exec) => (
                  <tr key={exec.id} className="hover:bg-[#1a1c1c] transition-colors group">
                    <td className="py-3 px-4">
                      <Link
                        href={`/agents/${exec.id}`}
                        className="font-medium text-[#e2e2e2] group-hover:text-[#6bd8cb] transition-colors block truncate max-w-md"
                      >
                        {exec.goal}
                      </Link>
                    </td>
                    <td className="py-3 px-4">{getStatusBadge(exec.status)}</td>
                    <td className="py-3 px-4 text-right font-mono-data text-[12px] text-[#bcc9c6]">
                      {exec.iterations}
                    </td>
                    <td className="py-3 px-4 text-right font-mono-data text-[12px] text-[#bcc9c6]">
                      {exec.tokenUsage?.toLocaleString() || "—"}
                    </td>
                    <td className="py-3 px-4 text-right font-mono-data text-[12px] text-[#879391]">
                      {exec.startedAt ? new Date(exec.startedAt).toLocaleString() : "—"}
                    </td>
                    <td className="py-3 px-4 text-center flex items-center justify-center gap-1">
                      <Link
                        href={`/agents/${exec.id}`}
                        className="text-[#879391] hover:text-[#6bd8cb] transition-colors p-1 rounded"
                        title="View execution trace"
                      >
                        <span className="material-symbols-outlined text-[16px]">visibility</span>
                      </Link>
                      {exec.status === "RUNNING" && (
                        <button
                          onClick={() => handleCancel(exec.id)}
                          className="text-[#879391] hover:text-[#ffb4ab] transition-colors p-1 rounded"
                          title="Cancel execution"
                        >
                          <span className="material-symbols-outlined text-[16px]">cancel</span>
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </main>
  );
}
