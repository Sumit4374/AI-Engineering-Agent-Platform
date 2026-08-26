"use client";

import { useState, useEffect } from "react";
import * as api from "@/lib/api";

export default function SettingsPage() {
  const [activeTab, setActiveTab] = useState<"models" | "mcp" | "telemetry">("models");
  const [providers, setProviders] = useState<api.ModelProviderInfo[]>([]);
  const [loadingProviders, setLoadingProviders] = useState(true);
  const [switching, setSwitching] = useState("");
  const [mcpInfo, setMcpInfo] = useState<api.McpServerInfo | null>(null);

  useEffect(() => {
    api.getProviders()
      .then((data) => {
        setProviders(data);
        setLoadingProviders(false);
      })
      .catch(() => setLoadingProviders(false));

    api.getMcpServerInfo()
      .then((info) => setMcpInfo(info))
      .catch(() => {});
  }, []);

  const handleSwitchProvider = async (providerType: string) => {
    setSwitching(providerType);
    try {
      await api.switchProvider(providerType);
      // Refresh
      const updated = await api.getProviders();
      setProviders(updated);
    } catch (err) {
      alert(`Failed to switch provider: ${err instanceof api.ApiError ? err.message : "Unknown error"}`);
    }
    setSwitching("");
  };

  const getProviderIcon = (type: string) => {
    switch (type) {
      case "OPENAI": return "psychology";
      case "NVIDIA_NIM": return "memory";
      case "OLLAMA": return "developer_board";
      case "ANTHROPIC": return "smart_toy";
      case "MOCK": return "science";
      default: return "psychology";
    }
  };

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
          { id: "models" as const, label: "Model Providers", icon: "psychology" },
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
        {activeTab === "models" && (
          <div className="space-y-4">
            <h3 className="font-ui-sans-bold text-[14px] text-[#e2e2e2]">
              Active Model Provider
            </h3>
            <p className="font-ui-sans-sm text-[12px] text-[#bcc9c6]">
              Select which AI model provider to use for all chat, code review, and agent operations.
            </p>

            {loadingProviders ? (
              <div className="flex items-center gap-2 py-8 text-[#879391]">
                <span className="material-symbols-outlined text-[16px] animate-spin">refresh</span>
                <span className="text-[12px]">Loading providers...</span>
              </div>
            ) : providers.length === 0 ? (
              <div className="py-8 text-center text-[#879391] text-[12px] border border-[#3d4947] rounded-[6px] bg-[#1a1c1c]">
                No providers configured on the backend.
              </div>
            ) : (
              <div className="grid gap-3">
                {providers.map((provider) => (
                  <div
                    key={provider.type}
                    className={`flex items-center justify-between p-4 rounded-[6px] border transition-colors ${
                      provider.active
                        ? "bg-[#1e2020] border-[#6bd8cb]/40"
                        : "bg-[#121414] border-[#3d4947] hover:border-[#3d4947]/80"
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <div
                        className={`w-9 h-9 rounded-[4px] flex items-center justify-center ${
                          provider.active
                            ? "bg-[#6bd8cb]/10 text-[#6bd8cb]"
                            : "bg-[#1e2020] text-[#879391]"
                        }`}
                      >
                        <span className="material-symbols-outlined text-[20px]">
                          {getProviderIcon(provider.type)}
                        </span>
                      </div>
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="font-ui-sans-bold text-[13px] text-[#e2e2e2]">
                            {provider.name}
                          </span>
                          {provider.active && (
                            <span className="px-1.5 py-0.5 bg-[#6bd8cb]/10 text-[#6bd8cb] font-mono-label text-[9px] rounded-[2px] border border-[#6bd8cb]/20">
                              ACTIVE
                            </span>
                          )}
                        </div>
                        <div className="font-mono-data text-[11px] text-[#879391] mt-0.5">
                          Default model: {provider.defaultModel}
                        </div>
                      </div>
                    </div>

                    {!provider.active && (
                      <button
                        onClick={() => handleSwitchProvider(provider.type)}
                        disabled={switching === provider.type}
                        className="h-[28px] px-3 bg-[#1e2020] border border-[#3d4947] text-[#bcc9c6] hover:text-[#e2e2e2] hover:bg-[#282a2a] rounded-[4px] font-ui-sans-bold text-[11px] transition-colors flex items-center gap-1.5 disabled:opacity-50"
                      >
                        {switching === provider.type ? (
                          <>
                            <span className="material-symbols-outlined text-[13px] animate-spin">sync</span>
                            <span>Switching...</span>
                          </>
                        ) : (
                          <>
                            <span className="material-symbols-outlined text-[13px]">swap_horiz</span>
                            <span>Activate</span>
                          </>
                        )}
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

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
