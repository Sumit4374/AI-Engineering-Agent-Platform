"use client";

import { useState, useEffect } from "react";
import { usePathname } from "next/navigation";
import { SideNavBar } from "./navigation/SideNavBar";
import { TopAppBar } from "./navigation/TopAppBar";
import { DeployAgentModal } from "./modals/DeployAgentModal";
import { CommandPalette } from "./modals/CommandPalette";
import { AuthGuard } from "./AuthGuard";

const PUBLIC_ROUTES = ["/login"];

export function AppShell({ children }: { children: React.ReactNode }) {
  const [isDeployModalOpen, setIsDeployModalOpen] = useState(false);
  const [isCommandPaletteOpen, setIsCommandPaletteOpen] = useState(false);
  const pathname = usePathname();

  const isPublicRoute = PUBLIC_ROUTES.some((route) => pathname.startsWith(route));

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") {
        e.preventDefault();
        setIsCommandPaletteOpen((prev) => !prev);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  // Public routes (login/signup) render without shell chrome
  if (isPublicRoute) {
    return <>{children}</>;
  }

  return (
    <AuthGuard>
      <div className="flex min-h-screen bg-[#121414] text-[#e2e2e2] antialiased">
        {/* Side Navigation Bar */}
        <SideNavBar />

        {/* Main Content Workspace Container */}
        <div className="flex-1 flex flex-col ml-[240px] max-w-[calc(100%-240px)] min-h-screen">
          {/* Top Header */}
          <TopAppBar
            onOpenDeployModal={() => setIsDeployModalOpen(true)}
            onOpenCommandPalette={() => setIsCommandPaletteOpen(true)}
          />

          {/* Dynamic Page View */}
          <div className="flex-1 flex flex-col overflow-hidden">
            {children}
          </div>
        </div>

        {/* Modals & Dialogs */}
        <DeployAgentModal
          isOpen={isDeployModalOpen}
          onClose={() => setIsDeployModalOpen(false)}
        />

        <CommandPalette
          isOpen={isCommandPaletteOpen}
          onClose={() => setIsCommandPaletteOpen(false)}
          onOpenDeployModal={() => setIsDeployModalOpen(true)}
        />
      </div>
    </AuthGuard>
  );
}
