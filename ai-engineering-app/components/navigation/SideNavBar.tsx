"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";

interface NavItem {
  name: string;
  href: string;
  icon: string;
  badge?: string;
}

const navItems: NavItem[] = [
  { name: "Chat", href: "/chat", icon: "chat" },
  { name: "Conversations", href: "/conversations", icon: "forum" },
  { name: "Documents", href: "/documents", icon: "description" },
  { name: "Agents", href: "/agents", icon: "smart_toy" },
  { name: "MCP Tools", href: "/mcp", icon: "build" },
];

export function SideNavBar() {
  const pathname = usePathname();
  const { user, logout } = useAuth();

  const isNavActive = (href: string) => {
    if (href === "/agents") {
      return pathname === "/agents" || (pathname.startsWith("/agents/") && pathname !== "/agents");
    }
    return pathname.startsWith(href);
  };

  return (
    <nav className="fixed left-0 top-0 h-full w-[240px] border-r border-[#3d4947] bg-[#121414] flex flex-col px-2 py-3 z-30 select-none">
      {/* Brand Header */}
      <div className="flex items-center gap-3 px-3 py-2 mb-6">
        <div className="w-8 h-8 rounded-[4px] bg-[#6bd8cb] flex items-center justify-center text-[#003732] shadow-sm">
          <span className="material-symbols-outlined fill text-[18px]">terminal</span>
        </div>
        <div>
          <div className="font-ui-sans-bold text-[14px] text-[#6bd8cb] tracking-wide">AIEngine</div>
          <div className="font-mono-label text-[11px] text-[#bcc9c6] opacity-70">v1.0.4-stable</div>
        </div>
      </div>

      {/* Main Navigation Links */}
      <div className="flex flex-col gap-1 flex-1">
        {navItems.map((item) => {
          const active = isNavActive(item.href);
          return (
            <Link
              key={item.name}
              href={item.href}
              className={`flex items-center gap-3 px-3 py-2 rounded-[4px] text-[13px] transition-colors duration-150 cursor-pointer ${
                active
                  ? "bg-[#282a2a] text-[#6bd8cb] font-semibold border-l-2 border-[#6bd8cb]"
                  : "text-[#bcc9c6] hover:bg-[#333535] hover:text-[#e2e2e2] font-normal"
              }`}
            >
              <span
                className={`material-symbols-outlined text-[18px] ${
                  active ? "fill text-[#6bd8cb]" : "text-[#bcc9c6]"
                }`}
              >
                {item.icon}
              </span>
              <span className="flex-1 truncate">{item.name}</span>
              {item.badge && (
                <span className="px-1.5 py-0.5 rounded-[2px] bg-[#29a195]/20 text-[#6bd8cb] font-mono-label text-[10px]">
                  {item.badge}
                </span>
              )}
            </Link>
          );
        })}
      </div>

      {/* Footer Navigation */}
      <div className="mt-auto pt-3 border-t border-[#3d4947]/60 flex flex-col gap-1">
        <Link
          href="/settings"
          className={`flex items-center gap-3 px-3 py-2 rounded-[4px] text-[13px] transition-colors duration-150 cursor-pointer ${
            pathname.startsWith("/settings")
              ? "bg-[#282a2a] text-[#6bd8cb] font-semibold"
              : "text-[#bcc9c6] hover:bg-[#333535] hover:text-[#e2e2e2]"
          }`}
        >
          <span className="material-symbols-outlined text-[18px]">settings</span>
          <span>Settings</span>
        </Link>

        {/* User Info + Logout */}
        {user && (
          <div className="mt-2 pt-2 border-t border-[#3d4947]/40">
            <div className="flex items-center gap-2.5 px-3 py-1.5">
              <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-[#003732] to-[#29a195] flex items-center justify-center text-[#e2e2e2] font-mono-label text-[10px] font-bold shrink-0">
                {user.userName.slice(0, 2).toUpperCase()}
              </div>
              <div className="flex-1 min-w-0">
                <div className="font-ui-sans-md text-[12px] text-[#e2e2e2] truncate">
                  {user.userName}
                </div>
                <div className="font-mono-label text-[10px] text-[#879391] truncate">
                  {user.email}
                </div>
              </div>
            </div>
            <button
              onClick={logout}
              className="w-full flex items-center gap-3 px-3 py-1.5 rounded-[4px] text-[12px] text-[#ffb4ab] hover:bg-[#93000a]/20 transition-colors mt-1"
            >
              <span className="material-symbols-outlined text-[16px]">logout</span>
              <span>Sign Out</span>
            </button>
          </div>
        )}
      </div>
    </nav>
  );
}
