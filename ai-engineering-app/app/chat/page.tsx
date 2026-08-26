"use client";

import { useState, useEffect, useRef, useCallback } from "react";
import * as api from "@/lib/api";

interface DisplayMessage {
  id: string;
  role: "USER" | "ASSISTANT";
  content: string;
  timestamp: string;
  isStreaming?: boolean;
}

export default function ChatConsolePage() {
  const [conversations, setConversations] = useState<api.ConversationDTO[]>([]);
  const [activeConvId, setActiveConvId] = useState<string | null>(null);
  const [searchFilter, setSearchFilter] = useState("");
  const [inputMessage, setInputMessage] = useState("");
  const [isProcessing, setIsProcessing] = useState(false);
  const [messages, setMessages] = useState<DisplayMessage[]>([]);
  const [loadingConvs, setLoadingConvs] = useState(true);
  const [loadingMsgs, setLoadingMsgs] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Load conversations
  useEffect(() => {
    api.listConversations()
      .then((convs) => {
        setConversations(convs);
        setLoadingConvs(false);
      })
      .catch(() => setLoadingConvs(false));
  }, []);

  // Load messages when active conversation changes
  const loadMessages = useCallback(async (convId: string) => {
    setLoadingMsgs(true);
    try {
      const conv = await api.getConversation(convId);
      setMessages(
        (conv.messages || []).map((m) => ({
          id: m.id,
          role: m.role === "USER" ? "USER" : "ASSISTANT",
          content: m.content,
          timestamp: new Date(m.createdAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
        }))
      );
    } catch {
      setMessages([]);
    }
    setLoadingMsgs(false);
  }, []);

  useEffect(() => {
    if (activeConvId) loadMessages(activeConvId);
    else setMessages([]);
  }, [activeConvId, loadMessages]);

  // Scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const handleNewConversation = async () => {
    try {
      const conv = await api.createConversation("New Chat");
      setConversations((prev) => [conv, ...prev]);
      setActiveConvId(conv.id);
      setMessages([]);
    } catch (err) {
      console.error("Failed to create conversation:", err);
    }
  };

  const handleSendMessage = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!inputMessage.trim() || isProcessing) return;

    const userText = inputMessage.trim();
    setInputMessage("");

    // Add user message to display immediately
    const userMsg: DisplayMessage = {
      id: `user-${Date.now()}`,
      role: "USER",
      content: userText,
      timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
    };
    setMessages((prev) => [...prev, userMsg]);
    setIsProcessing(true);

    let convId = activeConvId;
    if (!convId) {
      try {
        const title = userText.length > 35 ? userText.slice(0, 35) + "..." : userText;
        const conv = await api.createConversation(title);
        setConversations((prev) => [conv, ...prev]);
        convId = conv.id;
        setActiveConvId(conv.id);
      } catch (err) {
        console.warn("Could not create conversation upfront, proceeding with direct chat:", err);
      }
    }

    // Add streaming assistant placeholder
    const assistantId = `assistant-${Date.now()}`;
    setMessages((prev) => [
      ...prev,
      { id: assistantId, role: "ASSISTANT", content: "", timestamp: "", isStreaming: true },
    ]);

    let accumulated = "";

    try {
      // Stream response
      await api.streamMessage(userText, convId || undefined, (token) => {
        accumulated += token;
        setMessages((prev) =>
          prev.map((m) =>
            m.id === assistantId ? { ...m, content: accumulated } : m
          )
        );
      });

      // Mark streaming done
      setMessages((prev) =>
        prev.map((m) =>
          m.id === assistantId
            ? {
                ...m,
                isStreaming: false,
                timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
              }
            : m
        )
      );

      // Refresh conversations list to update title/timestamps
      api.listConversations().then(setConversations).catch(() => {});
    } catch (streamErr) {
      console.warn("Streaming failed, falling back to standard chat request:", streamErr);
      try {
        const res = await api.sendMessage(userText, convId || undefined);
        setMessages((prev) =>
          prev.map((m) =>
            m.id === assistantId
              ? {
                  ...m,
                  content: res.response,
                  isStreaming: false,
                  timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
                }
              : m
          )
        );
        if (res.conversationId && (!convId || convId !== res.conversationId)) {
          setActiveConvId(res.conversationId);
        }
        api.listConversations().then(setConversations).catch(() => {});
      } catch (chatErr) {
        const errMsg =
          chatErr instanceof api.ApiError
            ? chatErr.message
            : streamErr instanceof api.ApiError
            ? streamErr.message
            : "Failed to communicate with AI backend. Please verify your backend server is running and authenticated.";

        setMessages((prev) =>
          prev.map((m) =>
            m.id === assistantId
              ? {
                  ...m,
                  content: `Error: ${errMsg}`,
                  isStreaming: false,
                  timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
                }
              : m
          )
        );
      }
    } finally {
      setIsProcessing(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const handleDeleteConversation = async (id: string) => {
    if (!confirm("Delete this conversation?")) return;
    try {
      await api.deleteConversation(id);
      setConversations((prev) => prev.filter((c) => c.id !== id));
      if (activeConvId === id) {
        setActiveConvId(null);
        setMessages([]);
      }
    } catch (err) {
      console.error("Failed to delete conversation:", err);
    }
  };

  const filteredConversations = conversations.filter(
    (c) => c.title.toLowerCase().includes(searchFilter.toLowerCase())
  );

  const groupedConversations = () => {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today.getTime() - 86400000);

    const groups: { label: string; items: api.ConversationDTO[] }[] = [
      { label: "Today", items: [] },
      { label: "Yesterday", items: [] },
      { label: "Earlier", items: [] },
    ];

    for (const conv of filteredConversations) {
      const d = new Date(conv.createdAt);
      if (d >= today) groups[0].items.push(conv);
      else if (d >= yesterday) groups[1].items.push(conv);
      else groups[2].items.push(conv);
    }

    return groups.filter((g) => g.items.length > 0);
  };

  return (
    <main className="flex flex-1 overflow-hidden h-[calc(100vh-48px)] bg-[#121414]">
      {/* Left Rail: Conversation History */}
      <aside className="w-[280px] border-r border-[#3d4947] flex flex-col bg-[#0c0f0e] shrink-0">
        <div className="p-3 border-b border-[#3d4947] flex items-center justify-between gap-2">
          <div className="relative flex-1">
            <span className="material-symbols-outlined absolute left-2 top-1/2 -translate-y-1/2 text-[#879391] text-[15px]">
              search
            </span>
            <input
              type="text"
              value={searchFilter}
              onChange={(e) => setSearchFilter(e.target.value)}
              placeholder="Search conversations..."
              className="w-full h-8 bg-[#121414] border border-[#3d4947] rounded-[4px] pl-7 pr-2 text-[12px] font-ui-sans-sm text-[#e2e2e2] focus:outline-none focus:border-[#6bd8cb] placeholder-[#879391]/60"
            />
          </div>
          <button
            onClick={handleNewConversation}
            className="w-8 h-8 rounded-[4px] bg-[#1a1c1c] border border-[#3d4947] text-[#bcc9c6] hover:text-[#6bd8cb] hover:border-[#6bd8cb] flex items-center justify-center transition-colors"
            title="New Conversation"
          >
            <span className="material-symbols-outlined text-[16px]">add</span>
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-2 flex flex-col gap-1">
          {loadingConvs ? (
            <div className="flex items-center justify-center py-8 text-[#879391]">
              <span className="material-symbols-outlined text-[16px] animate-spin mr-2">refresh</span>
              <span className="text-[11px]">Loading...</span>
            </div>
          ) : filteredConversations.length === 0 ? (
            <div className="py-8 text-center text-[#879391] text-[11px]">
              No conversations yet. Start a new one!
            </div>
          ) : (
            groupedConversations().map((group) => (
              <div key={group.label} className="mb-2">
                <div className="text-[10px] font-mono-label uppercase tracking-wider text-[#879391] px-2 py-1">
                  {group.label}
                </div>
                <div className="space-y-1">
                  {group.items.map((item) => {
                    const isActive = item.id === activeConvId;
                    return (
                      <div key={item.id} className="relative group/conv">
                        <button
                          onClick={() => setActiveConvId(item.id)}
                          className={`w-full text-left p-2.5 rounded-[4px] border transition-colors cursor-pointer ${
                            isActive
                              ? "bg-[#1e2020] border-[#3d4947] text-[#e2e2e2]"
                              : "border-transparent text-[#bcc9c6] hover:bg-[#1a1c1c]"
                          }`}
                        >
                          <div
                            className={`font-ui-sans-md text-[12px] truncate ${
                              isActive ? "text-[#6bd8cb] font-semibold" : "text-[#e2e2e2]"
                            }`}
                          >
                            {item.title}
                          </div>
                          <div className="font-ui-sans-sm text-[11px] text-[#879391] truncate mt-0.5">
                            {new Date(item.createdAt).toLocaleDateString()}
                          </div>
                        </button>
                        <button
                          onClick={(e) => { e.stopPropagation(); handleDeleteConversation(item.id); }}
                          className="absolute top-2 right-2 opacity-0 group-hover/conv:opacity-100 text-[#879391] hover:text-[#ffb4ab] transition-all p-0.5"
                          title="Delete"
                        >
                          <span className="material-symbols-outlined text-[14px]">close</span>
                        </button>
                      </div>
                    );
                  })}
                </div>
              </div>
            ))
          )}
        </div>
      </aside>

      {/* Main Message Thread */}
      <section className="flex-1 flex flex-col bg-[#121414] relative min-w-0">
        {/* Messages Area */}
        <div className="flex-1 overflow-y-auto p-4 sm:p-6 flex flex-col gap-4">
          {loadingMsgs ? (
            <div className="flex-1 flex items-center justify-center text-[#879391]">
              <span className="material-symbols-outlined text-[16px] animate-spin mr-2">refresh</span>
              <span className="text-[12px]">Loading messages...</span>
            </div>
          ) : messages.length === 0 ? (
            <div className="flex-1 flex flex-col items-center justify-center text-center p-8 max-w-md mx-auto text-[#879391]">
              <div className="w-12 h-12 rounded-[6px] bg-[#1e2020] border border-[#3d4947] flex items-center justify-center text-[#6bd8cb] mb-3">
                <span className="material-symbols-outlined text-[24px]">terminal</span>
              </div>
              <h3 className="font-ui-sans-bold text-[15px] text-[#e2e2e2] mb-1">
                AI Chat Console
              </h3>
              <p className="text-[12px] leading-relaxed">
                Send instructions, ask questions, or run AI-powered analysis. Messages are streamed in real-time.
              </p>
              <div className="mt-4 flex flex-wrap gap-2 justify-center">
                {[
                  "Explain microservices architecture",
                  "Review my database schema",
                  "Summarize latest trends in AI",
                ].map((prompt) => (
                  <button
                    key={prompt}
                    onClick={() => setInputMessage(prompt)}
                    className="px-2.5 py-1 bg-[#1a1c1c] border border-[#3d4947] hover:border-[#6bd8cb] text-[#bcc9c6] hover:text-[#e2e2e2] text-[11px] rounded-[4px] transition-colors"
                  >
                    {prompt}
                  </button>
                ))}
              </div>
            </div>
          ) : (
            <>
              {messages.map((msg) => (
                <div key={msg.id} className="max-w-3xl mx-auto w-full">
                  {msg.role === "USER" && (
                    <div className="flex gap-3">
                      <div className="w-7 h-7 rounded-full bg-[#282a2a] flex-shrink-0 flex items-center justify-center border border-[#3d4947]">
                        <span className="material-symbols-outlined text-[15px] text-[#e2e2e2]">person</span>
                      </div>
                      <div className="flex-1 pt-0.5">
                        <div className="font-ui-sans-md text-[13px] text-[#e2e2e2] bg-[#1a1c1c] border border-[#3d4947] p-3 rounded-[6px]">
                          {msg.content}
                        </div>
                      </div>
                    </div>
                  )}

                  {msg.role === "ASSISTANT" && (
                    <div className="flex gap-3">
                      <div className="w-7 h-7 rounded-[4px] bg-[#6bd8cb] flex-shrink-0 flex items-center justify-center text-[#003732] shadow-sm">
                        <span className="material-symbols-outlined fill text-[16px]">terminal</span>
                      </div>
                      <div className="flex-1 pt-0.5">
                        <div className="font-ui-sans-md text-[13px] text-[#e2e2e2] leading-relaxed whitespace-pre-wrap">
                          {msg.content}
                          {msg.isStreaming && (
                            <span className="inline-block w-1.5 h-4 bg-[#6bd8cb] ml-0.5 animate-pulse" />
                          )}
                        </div>
                        {msg.timestamp && (
                          <span className="font-mono-label text-[10px] text-[#879391] mt-1 block">
                            {msg.timestamp}
                          </span>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              ))}
              <div ref={messagesEndRef} />
            </>
          )}

          {isProcessing && messages[messages.length - 1]?.isStreaming && (
            <div className="max-w-3xl mx-auto w-full flex items-center gap-2 pl-10 text-[#6bd8cb] font-mono-label text-[11px]">
              <span className="material-symbols-outlined text-[14px] animate-spin">refresh</span>
              <span>Streaming response...</span>
            </div>
          )}
        </div>

        {/* Input Area */}
        <div className="p-4 bg-[#121414] border-t border-[#3d4947] shrink-0">
          <div className="max-w-3xl mx-auto relative">
            <textarea
              rows={2}
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Type a message..."
              className="w-full bg-[#0c0f0e] border border-[#3d4947] rounded-[6px] p-3 pr-20 text-[13px] font-ui-sans-md text-[#e2e2e2] focus:outline-none focus:border-[#6bd8cb] resize-none placeholder-[#879391]/60"
            />
            <div className="absolute bottom-3 right-3 flex items-center gap-1.5">
              <button
                type="button"
                onClick={() => handleSendMessage()}
                disabled={!inputMessage.trim() || isProcessing}
                className="w-7 h-7 bg-[#6bd8cb] hover:bg-[#89f5e7] disabled:opacity-40 rounded-[4px] text-[#003732] transition-colors flex items-center justify-center font-bold"
                title="Send Message"
              >
                <span className="material-symbols-outlined text-[16px]">send</span>
              </button>
            </div>
            <div className="mt-1.5 flex items-center justify-end text-[11px] text-[#879391]">
              <span className="font-mono-label opacity-70">
                Return to send, Shift+Return for new line
              </span>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}
