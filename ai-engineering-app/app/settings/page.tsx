"use client";

import { useState, useEffect } from "react";
import * as api from "@/lib/api";

export default function SettingsPage() {
  const [activeTab, setActiveTab] = useState<"mcp" | "telemetry">("mcp");
  const [mcpInfo, setMcpInfo] = useState<api.McpServerInfo | null>(null);

  useEffect(() => {
    api.getMcpServerInfo()
      .then((info) => setMcpInfo(info))
      .catch(() => {});
  }, []);

  return (
    <main className="flex-1 overflow-y-auto p-6 flex flex-col gap-6 bg-[#121414]">
      {/* Header */}
      <div>
        <h1 className="font-ui-sans-bold text-[20px] text-[#e2e2e2] leading-tight">
          System Settings & Configuration
        </h1>
        <p className="font-ui-sans-md text-[13px] text-[#bcc9c6] mt-0.5">
          Configure AI model providers, MCP server connections, and system preferences.
        </p>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-2 border-b border-[#3d4947] pb-1">
        {[
          { id: "mcp" as const, label: "MCP Server", icon: "dns" },
          { id: "telemetry" as const, label: "Telemetry & Logs", icon: "analytics" },
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-[4px] text-[13px] font-ui-sans-md transition-colors ${
              activeTab === tab.id
                ? "bg-[#282a2a] text-[#6bd8cb] font-semibold"
                : "text-[#bcc9c6] hover:bg-[#1a1c1c] hover:text-[#e2e2e2]"
            }`}
          >
            <span className="material-symbols-outlined text-[16px]">{tab.icon}</span>
            <span>{tab.label}</span>
          </button>
        ))}
      </div>

      {/* Tab Content */}
      <div className="max-w-3xl">
        {activeTab === "mcp" && (
          <div className="bg-[#121414] border border-[#3d4947] rounded-[6px] p-5 space-y-4">
            <h3 className="font-ui-sans-bold text-[14px] text-[#e2e2e2] border-b border-[#3d4947] pb-2">
              MCP Server Status
            </h3>

            {mcpInfo ? (
              <div className="space-y-3">
                <div className="p-3 bg-[#1a1c1c] border border-[#3d4947] rounded-[4px]">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <span className="w-2 h-2 rounded-full bg-[#6bd8cb]" />
                      <span className="font-ui-sans-bold text-[13px] text-[#e2e2e2]">{mcpInfo.name}</span>
                    </div>
                    <span className="px-1.5 py-0.5 bg-[#6bd8cb]/10 text-[#6bd8cb] text-[10px] font-mono-label rounded">
                      v{mcpInfo.version}
                    </span>
                  </div>
                  <div className="font-mono-data text-[11px] text-[#bcc9c6] space-y-1">
                    <p>Protocol: <strong className="text-[#e2e2e2]">{mcpInfo.protocolVersion}</strong></p>
                    <p>Tools: <strong className="text-[#6bd8cb]">{mcpInfo.toolCount} registered</strong></p>
                    {mcpInfo.capabilities && mcpInfo.capabilities.length > 0 && (
                      <p>Capabilities: <strong className="text-[#e2e2e2]">{mcpInfo.capabilities.join(", ")}</strong></p>
                    )}
                  </div>
                </div>
              </div>
            ) : (
              <div className="py-6 text-center text-[#879391] text-[12px]">
                MCP server info not available.
              </div>
            )}
          </div>
        )}

        {activeTab === "telemetry" && (
          <div className="bg-[#121414] border border-[#3d4947] rounded-[6px] p-5 space-y-4">
            <h3 className="font-ui-sans-bold text-[14px] text-[#e2e2e2] border-b border-[#3d4947] pb-2">
              Health & Metrics
            </h3>
            <div className="space-y-3">
              <div className="p-3 bg-[#1a1c1c] border border-[#3d4947] rounded-[4px]">
                <div className="font-ui-sans-bold text-[12px] text-[#e2e2e2] mb-1">Health Endpoint</div>
                <code className="font-mono-data text-[11px] text-[#6bd8cb]">GET /actuator/health</code>
              </div>
              <div className="p-3 bg-[#1a1c1c] border border-[#3d4947] rounded-[4px]">
                <div className="font-ui-sans-bold text-[12px] text-[#e2e2e2] mb-1">Prometheus Metrics</div>
                <code className="font-mono-data text-[11px] text-[#6bd8cb]">GET /actuator/prometheus</code>
              </div>
              <div className="p-3 bg-[#1a1c1c] border border-[#3d4947] rounded-[4px]">
                <div className="font-ui-sans-bold text-[12px] text-[#e2e2e2] mb-1">Application Info</div>
                <code className="font-mono-data text-[11px] text-[#6bd8cb]">GET /actuator/info</code>
              </div>
            </div>
          </div>
        )}
      </div>
    </main>
  );
}
