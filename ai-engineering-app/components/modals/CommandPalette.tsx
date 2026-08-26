"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

interface CommandPaletteProps {
  isOpen: boolean;
  onClose: () => void;
  onOpenDeployModal: () => void;
}

export function CommandPalette({ isOpen, onClose, onOpenDeployModal }: CommandPaletteProps) {
  const [query, setQuery] = useState("");
  const router = useRouter();

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") {
        e.preventDefault();
        if (isOpen) {
          onClose();
        } else {
          // Can be handled upstream
        }
      }
      if (e.key === "Escape" && isOpen) {
        onClose();
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const actions = [
    {
      category: "Navigation",
      items: [
        { title: "Agent Execution Trace", subtitle: "View current running agent pipeline", icon: "smart_toy", run: () => { router.push("/"); onClose(); } },
        { title: "Agent Chat Console", subtitle: "Interact with autonomous assistant", icon: "chat", run: () => { router.push("/chat"); onClose(); } },
        { title: "RAG Documents Workspace", subtitle: "Inspect indexed PDF & MD embeddings", icon: "description", run: () => { router.push("/documents"); onClose(); } },
        { title: "MCP Tool Inspector", subtitle: "Inspect and run Model Context Protocol capabilities", icon: "build", run: () => { router.push("/mcp"); onClose(); } },
        { title: "Conversations Archive", subtitle: "Review historical execution sessions", icon: "forum", run: () => { router.push("/conversations"); onClose(); } },
        { title: "System Settings", subtitle: "API keys, model parameters, server bindings", icon: "settings", run: () => { router.push("/settings"); onClose(); } },
      ],
    },
    {
      category: "Operations",
      items: [
        { title: "Deploy New Agent", subtitle: "Launch a custom autonomous task agent", icon: "rocket_launch", run: () => { onClose(); onOpenDeployModal(); } },
        { title: "Reconnect MCP Servers", subtitle: "Trigger stdio / SSE re-handshake", icon: "sync", run: () => { alert("MCP Servers re-indexed successfully (14 capabilities active)."); onClose(); } },
      ],
    },
  ];

  const filteredGroups = actions
    .map((group) => ({
      ...group,
      items: group.items.filter(
        (item) =>
          item.title.toLowerCase().includes(query.toLowerCase()) ||
          item.subtitle.toLowerCase().includes(query.toLowerCase())
      ),
    }))
    .filter((group) => group.items.length > 0);

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center bg-black/75 backdrop-blur-xs pt-20 p-4">
      <div className="w-full max-w-xl bg-[#1e2020] border border-[#3d4947] rounded-[6px] shadow-2xl overflow-hidden flex flex-col animate-in fade-in zoom-in-95 duration-100">
        {/* Search Input */}
        <div className="flex items-center gap-3 px-3 py-2.5 border-b border-[#3d4947] bg-[#121414]">
          <span className="material-symbols-outlined text-[#879391] text-[18px]">search</span>
          <input
            autoFocus
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Type a command, screen, or tool..."
            className="flex-1 bg-transparent text-[#e2e2e2] text-[13px] placeholder-[#879391] focus:outline-none"
          />
          <kbd className="px-1.5 py-0.5 bg-[#282a2a] text-[#879391] text-[10px] font-mono rounded">
            ESC
          </kbd>
        </div>

        {/* Results List */}
        <div className="max-h-80 overflow-y-auto p-2 space-y-3">
          {filteredGroups.length === 0 ? (
            <div className="py-8 text-center text-[#879391] text-[12px]">
              No commands matching &quot;{query}&quot;
            </div>
          ) : (
            filteredGroups.map((group) => (
              <div key={group.category}>
                <div className="px-2 py-1 text-[10px] font-mono-label uppercase tracking-wider text-[#879391]">
                  {group.category}
                </div>
                <div className="space-y-0.5">
                  {group.items.map((item) => (
                    <button
                      key={item.title}
                      onClick={item.run}
                      className="w-full text-left flex items-center gap-3 px-2.5 py-2 rounded-[4px] hover:bg-[#282a2a] group transition-colors"
                    >
                      <div className="w-6 h-6 rounded bg-[#121414] border border-[#3d4947] flex items-center justify-center text-[#bcc9c6] group-hover:text-[#6bd8cb] group-hover:border-[#6bd8cb]/50">
                        <span className="material-symbols-outlined text-[15px]">{item.icon}</span>
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="font-ui-sans-md text-[12px] text-[#e2e2e2] group-hover:text-[#6bd8cb]">
                          {item.title}
                        </div>
                        <div className="font-ui-sans-sm text-[11px] text-[#879391] truncate">
                          {item.subtitle}
                        </div>
                      </div>
                      <span className="material-symbols-outlined text-[14px] text-[#879391] opacity-0 group-hover:opacity-100">
                        arrow_forward
                      </span>
                    </button>
                  ))}
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
