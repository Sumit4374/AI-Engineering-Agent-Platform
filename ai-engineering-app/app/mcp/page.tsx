"use client";

import { useState, useEffect } from "react";
import * as api from "@/lib/api";

export default function McpInspectorPage() {
  const [serverInfo, setServerInfo] = useState<api.McpServerInfo | null>(null);
  const [tools, setTools] = useState<api.McpToolDefinition[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchFilter, setSearchFilter] = useState("");
  const [selectedTool, setSelectedTool] = useState<api.McpToolDefinition | null>(null);
  const [payloadInput, setPayloadInput] = useState("{}");
  const [executionOutput, setExecutionOutput] = useState<api.McpToolCallResult | null>(null);
  const [isRunning, setIsRunning] = useState(false);
  const [connectionError, setConnectionError] = useState("");

  const buildSamplePayload = (tool: api.McpToolDefinition) => {
    const schema = tool.inputSchema;
    if (!schema || !schema.properties) return "{}";
    const sample: Record<string, unknown> = {};
    const props = schema.properties as Record<string, { type?: string; description?: string; default?: unknown }>;
    for (const [key, val] of Object.entries(props)) {
      if (val.default !== undefined) sample[key] = val.default;
      else if (val.type === "string") sample[key] = "";
      else if (val.type === "integer" || val.type === "number") sample[key] = 0;
      else if (val.type === "boolean") sample[key] = false;
      else sample[key] = null;
    }
    return JSON.stringify(sample, null, 2);
  };

  useEffect(() => {
    const loadData = async () => {
      try {
        const [info, toolList] = await Promise.all([
          api.getMcpServerInfo().catch(() => null),
          api.listMcpTools().catch(() => [] as api.McpToolDefinition[]),
        ]);
        setServerInfo(info);
        setTools(toolList);
        if (toolList.length > 0) {
          setSelectedTool(toolList[0]);
          setPayloadInput(buildSamplePayload(toolList[0]));
        }
      } catch (err) {
        setConnectionError(err instanceof api.ApiError ? err.message : "Failed to connect to MCP server");
      }
      setLoading(false);
    };
    loadData();
  }, []);

  const buildSamplePayload = (tool: api.McpToolDefinition) => {
    const schema = tool.inputSchema;
    if (!schema || !schema.properties) return "{}";
    const sample: Record<string, unknown> = {};
    const props = schema.properties as Record<string, { type?: string; description?: string; default?: unknown }>;
    for (const [key, val] of Object.entries(props)) {
      if (val.default !== undefined) sample[key] = val.default;
      else if (val.type === "string") sample[key] = "";
      else if (val.type === "integer" || val.type === "number") sample[key] = 0;
      else if (val.type === "boolean") sample[key] = false;
      else sample[key] = null;
    }
    return JSON.stringify(sample, null, 2);
  };

  const handleSelectTool = (tool: api.McpToolDefinition) => {
    setSelectedTool(tool);
    setPayloadInput(buildSamplePayload(tool));
    setExecutionOutput(null);
  };

  const handleExecuteTool = async () => {
    if (!selectedTool) return;
    setIsRunning(true);
    try {
      let args: Record<string, unknown>;
      try {
        args = JSON.parse(payloadInput);
      } catch {
        alert("Invalid JSON in input parameters");
        setIsRunning(false);
        return;
      }
      const result = await api.executeMcpTool(selectedTool.name, args);
      setExecutionOutput(result);
    } catch (err) {
      setExecutionOutput({
        toolName: selectedTool.name,
        content: "",
        isError: true,
        errorMessage: err instanceof api.ApiError ? err.message : "Execution failed",
      });
    }
    setIsRunning(false);
  };

  const filteredTools = tools.filter(
    (t) =>
      t.name.toLowerCase().includes(searchFilter.toLowerCase()) ||
      (t.category || "").toLowerCase().includes(searchFilter.toLowerCase()) ||
      t.description.toLowerCase().includes(searchFilter.toLowerCase())
  );

  if (loading) {
    return (
      <main className="flex-1 flex items-center justify-center bg-[#121414]">
        <div className="flex items-center gap-3 text-[#6bd8cb]">
          <span className="material-symbols-outlined text-[20px] animate-spin">refresh</span>
          <span className="font-mono-label text-[13px]">Connecting to MCP server...</span>
        </div>
      </main>
    );
  }

  return (
    <main className="flex-1 overflow-y-auto p-6 flex flex-col gap-6 bg-[#121414]">
      {/* Server Info Strip */}
      <section className="flex flex-wrap items-center justify-between gap-4 border border-[#3d4947] bg-[#121414] px-4 py-3 rounded-[6px] shadow-sm">
        <div className="flex flex-wrap items-center gap-4 sm:gap-6">
          <div className="flex items-center gap-2">
            <span className="material-symbols-outlined text-[#6bd8cb] text-[18px]">dns</span>
            <span className="font-mono-label text-[11px] text-[#bcc9c6] uppercase tracking-wider">Server</span>
            <span className="font-mono-data text-[12px] text-[#e2e2e2] font-semibold">
              {serverInfo?.name || "MCP Server"}
            </span>
          </div>
          <div className="h-4 w-px bg-[#3d4947] hidden sm:block" />
          <div className="flex items-center gap-2">
            <span className="font-mono-label text-[11px] text-[#bcc9c6] uppercase tracking-wider">Version</span>
            <span className="font-mono-data text-[12px] text-[#c0c1ff]">
              {serverInfo?.version || "—"}
            </span>
          </div>
          <div className="h-4 w-px bg-[#3d4947] hidden sm:block" />
          <div className="flex items-center gap-2">
            <span className="font-mono-label text-[11px] text-[#bcc9c6] uppercase tracking-wider">Tools</span>
            <span className="font-mono-data text-[12px] text-[#6bd8cb]">
              {serverInfo?.toolCount ?? tools.length} Registered
            </span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {connectionError ? (
            <>
              <span className="w-2 h-2 rounded-full bg-[#ffb4ab]" />
              <span className="font-mono-data text-[12px] text-[#ffb4ab] font-medium">Disconnected</span>
            </>
          ) : (
            <>
              <span className="w-2 h-2 rounded-full bg-[#6bd8cb] animate-pulse" />
              <span className="font-mono-data text-[12px] text-[#6bd8cb] font-medium">Connected</span>
            </>
          )}
        </div>
      </section>

      {/* Main Workspace Split */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 min-h-[500px]">
        {/* Left: Registered Tools */}
        <section className="lg:col-span-7 flex flex-col border border-[#3d4947] bg-[#121414] rounded-[6px] shadow-sm overflow-hidden">
          <header className="p-3 border-b border-[#3d4947] flex items-center justify-between bg-[#1a1c1c]">
            <h2 className="font-ui-sans-bold text-[13px] text-[#e2e2e2] flex items-center gap-2">
              <span className="material-symbols-outlined text-[#6bd8cb] text-[16px]">list_alt</span>
              <span>Registered Capabilities</span>
            </h2>
            <div className="relative w-48">
              <span className="material-symbols-outlined absolute left-2 top-1/2 -translate-y-1/2 text-[#879391] text-[14px]">search</span>
              <input
                type="text"
                value={searchFilter}
                onChange={(e) => setSearchFilter(e.target.value)}
                placeholder="Filter tools..."
                className="w-full bg-[#121414] border border-[#3d4947] rounded-[4px] pl-6 pr-2 py-1 font-mono-data text-[11px] text-[#e2e2e2] placeholder-[#879391]/60 focus:outline-none focus:border-[#6bd8cb]"
              />
            </div>
          </header>

          <div className="flex-1 overflow-x-auto">
            <table className="w-full text-left border-collapse whitespace-nowrap">
              <thead>
                <tr className="border-b border-[#3d4947] bg-[#121414] text-[#bcc9c6] font-mono-label text-[10px] uppercase tracking-wider">
                  <th className="py-2 px-3 font-medium">Tool Name</th>
                  <th className="py-2 px-3 font-medium">Category</th>
                  <th className="py-2 px-3 font-medium">Description</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#3d4947]/70 font-ui-sans-md text-[12px] text-[#e2e2e2]">
                {filteredTools.length === 0 ? (
                  <tr>
                    <td colSpan={3} className="py-8 text-center text-[#879391] text-[11px]">
                      {tools.length === 0 ? "No MCP tools registered" : "No tools matching filter"}
                    </td>
                  </tr>
                ) : (
                  filteredTools.map((tool) => {
                    const isSelected = selectedTool?.name === tool.name;
                    return (
                      <tr
                        key={tool.name}
                        onClick={() => handleSelectTool(tool)}
                        className={`cursor-pointer transition-colors ${
                          isSelected ? "bg-[#1e2020] border-l-2 border-[#6bd8cb]" : "hover:bg-[#1a1c1c]"
                        }`}
                      >
                        <td className="py-2.5 px-3">
                          <div className="flex items-center gap-2">
                            <span className={`material-symbols-outlined text-[15px] ${isSelected ? "text-[#6bd8cb]" : "text-[#879391]"}`}>
                              build
                            </span>
                            <span className={`font-mono-data text-[12px] ${isSelected ? "text-[#6bd8cb] font-semibold" : "text-[#e2e2e2]"}`}>
                              {tool.name}
                            </span>
                          </div>
                        </td>
                        <td className="py-2.5 px-3 text-[#bcc9c6] text-[11px]">{tool.category || "—"}</td>
                        <td className="py-2.5 px-3 text-[#879391] text-[11px] truncate max-w-[200px]">{tool.description}</td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </section>

        {/* Right: Tool Inspector */}
        <section className="lg:col-span-5 flex flex-col border border-[#3d4947] bg-[#121414] rounded-[6px] shadow-sm overflow-hidden">
          <header className="p-3 border-b border-[#3d4947] flex items-center justify-between bg-[#1a1c1c]">
            <div className="flex items-center gap-2">
              <span className="material-symbols-outlined text-[#6bd8cb] text-[16px]">terminal</span>
              <h2 className="font-ui-sans-bold text-[13px] text-[#e2e2e2]">
                Tool Inspector: <span className="text-[#6bd8cb] font-mono">{selectedTool?.name || "—"}</span>
              </h2>
            </div>
            <button
              onClick={handleExecuteTool}
              disabled={isRunning || !selectedTool}
              className="h-[26px] px-3 bg-[#6bd8cb] text-[#003732] hover:bg-[#89f5e7] disabled:opacity-50 rounded-[4px] font-ui-sans-bold text-[11px] flex items-center gap-1.5 shadow-sm transition-all"
            >
              {isRunning ? (
                <>
                  <span className="material-symbols-outlined text-[13px] animate-spin">sync</span>
                  <span>Executing...</span>
                </>
              ) : (
                <>
                  <span className="material-symbols-outlined text-[13px]">play_arrow</span>
                  <span>Execute Tool</span>
                </>
              )}
            </button>
          </header>

          <div className="p-4 space-y-4 flex-1 flex flex-col overflow-y-auto">
            {selectedTool && (
              <p className="font-ui-sans-md text-[12px] text-[#bcc9c6] bg-[#1a1c1c] border border-[#3d4947] p-2.5 rounded-[4px]">
                {selectedTool.description}
              </p>
            )}

            {/* Input Payload Editor */}
            <div>
              <div className="flex items-center justify-between mb-1">
                <span className="font-mono-label text-[10px] uppercase tracking-wider text-[#bcc9c6]">
                  Input Parameters (JSON)
                </span>
                {selectedTool && (
                  <button
                    onClick={() => setPayloadInput(buildSamplePayload(selectedTool))}
                    className="font-mono-label text-[10px] text-[#6bd8cb] hover:underline"
                  >
                    Reset
                  </button>
                )}
              </div>
              <textarea
                rows={5}
                value={payloadInput}
                onChange={(e) => setPayloadInput(e.target.value)}
                className="w-full bg-[#0c0f0e] border border-[#3d4947] rounded-[4px] p-2.5 font-code-block text-[12px] text-[#e2e2e2] focus:outline-none focus:border-[#6bd8cb] resize-none"
              />
            </div>

            {/* Output */}
            <div className="flex-1 flex flex-col">
              <div className="flex items-center justify-between mb-1">
                <span className="font-mono-label text-[10px] uppercase tracking-wider text-[#bcc9c6]">
                  Execution Response
                </span>
                {executionOutput && (
                  <span className={`font-mono-label text-[10px] ${executionOutput.isError ? "text-[#ffb4ab]" : "text-[#6bd8cb]"}`}>
                    {executionOutput.isError ? "ERROR" : "200 OK"}
                  </span>
                )}
              </div>
              <pre className="flex-1 min-h-[140px] bg-[#0c0f0e] border border-[#3d4947] rounded-[4px] p-2.5 font-code-block text-[12px] text-[#6bd8cb] overflow-auto">
                <code>
                  {executionOutput
                    ? executionOutput.isError
                      ? executionOutput.errorMessage || "Execution failed"
                      : executionOutput.content || JSON.stringify(executionOutput, null, 2)
                    : "// Execute a tool to see results"}
                </code>
              </pre>
            </div>
          </div>
        </section>
      </div>
    </main>
  );
}
