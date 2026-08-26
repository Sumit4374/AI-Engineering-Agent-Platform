"use client";

import { usePathname } from "next/navigation";
import { useState } from "react";
import { useAuth } from "@/contexts/AuthContext";

interface TopAppBarProps {
  onOpenDeployModal: () => void;
  onOpenCommandPalette: () => void;
}

export function TopAppBar({ onOpenDeployModal, onOpenCommandPalette }: TopAppBarProps) {
  const pathname = usePathname();
  const { user } = useAuth();
  const [showNotifications, setShowNotifications] = useState(false);

  const getBreadcrumb = () => {
    if (pathname === "/agents" || pathname.startsWith("/agents/")) {
      return { parent: "Agents", current: pathname.includes("/") && pathname !== "/agents" ? "Execution Detail" : "All Executions" };
    }
    if (pathname.startsWith("/chat")) {
      return { parent: "Console", current: "Interactive Chat" };
    }
    if (pathname.startsWith("/documents")) {
      return { parent: "Knowledge Base", current: "Documents Workspace" };
    }
    if (pathname.startsWith("/mcp")) {
      return { parent: "Integration", current: "MCP Tool Inspector" };
    }
    if (pathname.startsWith("/conversations")) {
      return { parent: "History", current: "All Conversations" };
    }
    if (pathname.startsWith("/settings")) {
      return { parent: "System", current: "Settings & Configuration" };
    }
    return { parent: "Console", current: "Overview" };
  };

  const breadcrumb = getBreadcrumb();

  return (
    <header className="sticky top-0 w-full h-[48px] bg-[#121414] border-b border-[#3d4947] flex justify-between items-center px-4 z-20 select-none">
      {/* Left Context / Breadcrumbs */}
      <div className="flex items-center gap-3">
        <span className="font-ui-sans-bold text-[13px] text-[#6bd8cb]">AIEngine Console</span>
        <div className="h-3.5 w-px bg-[#3d4947]" />
        <div className="flex items-center text-[#bcc9c6] gap-1.5 text-[12px] font-ui-sans-sm">
          <span className="text-[#879391]">{breadcrumb.parent}</span>
          <span className="material-symbols-outlined text-[14px] text-[#879391]">chevron_right</span>
          <span className="text-[#e2e2e2] font-medium">{breadcrumb.current}</span>
        </div>
      </div>

      {/* Right Actions */}
      <div className="flex items-center gap-3">
        {/* Search trigger */}
        <button
          onClick={onOpenCommandPalette}
          className="h-[28px] px-2.5 bg-[#1a1c1c] border border-[#3d4947] rounded-[4px] flex items-center justify-center gap-2 text-[#bcc9c6] font-ui-sans-sm text-[12px] hover:bg-[#282a2a] hover:text-[#e2e2e2] transition-colors"
          title="Search commands and resources (Cmd+K)"
        >
          <span className="material-symbols-outlined text-[15px]">search</span>
          <span className="hidden sm:inline">Search...</span>
          <kbd className="hidden sm:inline-block px-1 py-0.2 bg-[#333535] text-[#879391] text-[10px] font-mono rounded">
            ⌘K
          </kbd>
        </button>

        {/* Notification bell */}
        <div className="flex items-center gap-1 text-[#bcc9c6]">
          <div className="relative">
            <button
              onClick={() => setShowNotifications(!showNotifications)}
              className="w-7 h-7 flex items-center justify-center rounded hover:bg-[#282a2a] hover:text-[#6bd8cb] transition-colors relative"
              title="Notifications"
            >
              <span className="material-symbols-outlined text-[18px]">notifications</span>
            </button>

            {showNotifications && (
              <div className="absolute right-0 mt-2 w-72 bg-[#1e2020] border border-[#3d4947] rounded-[6px] shadow-xl p-3 z-50 text-[12px]">
                <div className="font-ui-sans-bold text-[#e2e2e2] mb-2 flex items-center justify-between border-b border-[#3d4947] pb-1.5">
                  <span>System Activity</span>
                  <span className="font-mono-label text-[10px] text-[#6bd8cb]">LIVE</span>
                </div>
                <div className="py-4 text-center text-[#879391] text-[11px]">
                  No recent notifications
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Primary CTA */}
        <button
          onClick={onOpenDeployModal}
          className="h-[28px] px-3 bg-[#6bd8cb] text-[#003732] font-ui-sans-bold text-[12px] rounded-[4px] hover:bg-[#89f5e7] active:scale-95 transition-all flex items-center gap-1.5 shadow-sm"
        >
          <span className="material-symbols-outlined text-[15px]">play_arrow</span>
          <span>Deploy Agent</span>
        </button>

        {/* User Profile Avatar */}
        <div
          className="w-7 h-7 rounded-full overflow-hidden border border-[#3d4947] bg-[#1e2020] flex items-center justify-center cursor-pointer hover:border-[#6bd8cb] transition-colors"
          title={user ? `Operator: ${user.userName}` : "Operator"}
        >
          <div className="w-full h-full bg-gradient-to-tr from-[#003732] to-[#29a195] flex items-center justify-center text-[#e2e2e2] font-mono-label text-[11px] font-bold">
            {user ? user.userName.slice(0, 2).toUpperCase() : "OP"}
          </div>
        </div>
      </div>
    </header>
  );
}
