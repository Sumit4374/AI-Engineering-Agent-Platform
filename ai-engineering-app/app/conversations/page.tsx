"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import * as api from "@/lib/api";

export default function ConversationsPage() {
  const [conversations, setConversations] = useState<api.ConversationDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterQuery, setFilterQuery] = useState("");

  useEffect(() => {
    api.listConversations()
      .then((data) => {
        setConversations(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this conversation and all its messages?")) return;
    try {
      await api.deleteConversation(id);
      setConversations((prev) => prev.filter((c) => c.id !== id));
    } catch (err) {
      console.error("Failed to delete:", err);
    }
  };

  const filteredConversations = conversations.filter(
    (c) => c.title.toLowerCase().includes(filterQuery.toLowerCase())
  );

  return (
    <main className="flex-1 overflow-y-auto p-6 flex flex-col gap-6 bg-[#121414]">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="font-ui-sans-bold text-[20px] text-[#e2e2e2] leading-tight">
            Conversations & Sessions
          </h1>
          <p className="font-ui-sans-md text-[13px] text-[#bcc9c6] mt-0.5">
            All past and active chat sessions with message history.
          </p>
        </div>
        <Link
          href="/chat"
          className="flex items-center gap-1.5 h-[32px] px-3.5 rounded-[4px] bg-[#6bd8cb] text-[#003732] hover:bg-[#89f5e7] font-ui-sans-bold text-[12px] transition-colors shadow-sm self-start sm:self-auto"
        >
          <span className="material-symbols-outlined text-[16px]">chat</span>
          <span>New Chat Session</span>
        </Link>
      </div>

      {/* Filter */}
      <div className="flex items-center bg-[#121414] border border-[#3d4947] p-3 rounded-[6px]">
        <div className="relative w-full sm:w-96">
          <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-[#879391] text-[16px]">
            search
          </span>
          <input
            type="text"
            value={filterQuery}
            onChange={(e) => setFilterQuery(e.target.value)}
            placeholder="Search by title..."
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
                <th className="py-2.5 px-4 font-medium w-2/5">Title</th>
                <th className="py-2.5 px-4 font-medium">Status</th>
                <th className="py-2.5 px-4 font-medium text-right">Created</th>
                <th className="py-2.5 px-4 font-medium text-right">Updated</th>
                <th className="py-2.5 px-4 font-medium w-20 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#3d4947]/70 font-ui-sans-md text-[13px] text-[#e2e2e2]">
              {loading ? (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-[#879391] text-[12px]">
                    <span className="material-symbols-outlined text-[16px] animate-spin align-middle mr-2">refresh</span>
                    Loading conversations...
                  </td>
                </tr>
              ) : filteredConversations.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-[#879391] text-[12px]">
                    No conversations found.
                  </td>
                </tr>
              ) : (
                filteredConversations.map((conv) => (
                  <tr key={conv.id} className="hover:bg-[#1a1c1c] transition-colors group">
                    <td className="py-3 px-4">
                      <Link
                        href="/chat"
                        className="font-medium text-[#e2e2e2] group-hover:text-[#6bd8cb] transition-colors block truncate max-w-md"
                      >
                        {conv.title}
                      </Link>
                    </td>
                    <td className="py-3 px-4">
                      {conv.status === "ACTIVE" ? (
                        <span className="inline-flex items-center gap-1.5 px-2 py-[2px] rounded-[2px] bg-[#6bd8cb]/10 text-[#6bd8cb] font-mono-label text-[10px] border border-[#6bd8cb]/20">
                          <span className="w-1.5 h-1.5 rounded-full bg-[#6bd8cb]" />
                          ACTIVE
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1.5 px-2 py-[2px] rounded-[2px] bg-[#1e2020] text-[#bcc9c6] font-mono-label text-[10px] border border-[#3d4947]">
                          ARCHIVED
                        </span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-right font-mono-data text-[12px] text-[#879391]">
                      {new Date(conv.createdAt).toLocaleDateString()}
                    </td>
                    <td className="py-3 px-4 text-right font-mono-data text-[12px] text-[#879391]">
                      {new Date(conv.updatedAt).toLocaleDateString()}
                    </td>
                    <td className="py-3 px-4 text-center flex items-center justify-center gap-1">
                      <Link
                        href="/chat"
                        className="text-[#879391] hover:text-[#6bd8cb] transition-colors p-1 rounded inline-block"
                        title="Open in chat"
                      >
                        <span className="material-symbols-outlined text-[16px]">chat</span>
                      </Link>
                      <button
                        onClick={() => handleDelete(conv.id)}
                        className="text-[#879391] hover:text-[#ffb4ab] transition-colors p-1 rounded"
                        title="Delete conversation"
                      >
                        <span className="material-symbols-outlined text-[16px]">delete</span>
                      </button>
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
