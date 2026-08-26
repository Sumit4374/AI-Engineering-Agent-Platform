"use client";

import { useState, useEffect, useRef } from "react";
import * as api from "@/lib/api";

export default function DocumentsPage() {
  const [docs, setDocs] = useState<api.DocumentDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
  const [isPasteModalOpen, setIsPasteModalOpen] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [pasting, setPasting] = useState(false);

  // Form states
  const [pasteTitle, setPasteTitle] = useState("");
  const [pasteContent, setPasteContent] = useState("");
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    api.listDocuments()
      .then((data) => {
        setDocs(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  const filteredDocs = docs.filter((doc) => {
    const matchesSearch =
      doc.fileName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      doc.documentType.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === "ALL" || doc.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const getDocIcon = (type: string) => {
    switch (type) {
      case "PDF":
        return { icon: "picture_as_pdf", color: "text-[#ffb4ab]" };
      case "MARKDOWN":
        return { icon: "description", color: "text-[#c0c1ff]" };
      case "CODE":
        return { icon: "code", color: "text-[#6bd8cb]" };
      default:
        return { icon: "article", color: "text-[#bcc9c6]" };
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const handleUploadFile = async (file: File) => {
    setUploading(true);
    try {
      await api.uploadDocument(file);
      // Refresh the list
      const updated = await api.listDocuments();
      setDocs(updated);
      setIsUploadModalOpen(false);
    } catch (err) {
      alert(`Upload failed: ${err instanceof api.ApiError ? err.message : "Unknown error"}`);
    }
    setUploading(false);
  };

  const handleAddPastedDoc = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!pasteTitle.trim() || !pasteContent.trim()) return;
    setPasting(true);
    try {
      await api.ingestText(pasteTitle, pasteContent);
      const updated = await api.listDocuments();
      setDocs(updated);
      setPasteTitle("");
      setPasteContent("");
      setIsPasteModalOpen(false);
    } catch (err) {
      alert(`Ingest failed: ${err instanceof api.ApiError ? err.message : "Unknown error"}`);
    }
    setPasting(false);
  };

  const handleDeleteDoc = async (id: string) => {
    if (!confirm("Delete this document from the vector store?")) return;
    try {
      await api.deleteDocument(id);
      setDocs((prev) => prev.filter((d) => d.id !== id));
    } catch (err) {
      console.error("Failed to delete document:", err);
    }
  };

  return (
    <main className="flex-1 overflow-y-auto p-6 flex flex-col gap-6 bg-[#121414]">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="font-ui-sans-bold text-[20px] text-[#e2e2e2] leading-tight">
            Documents Workspace
          </h1>
          <p className="font-ui-sans-md text-[13px] text-[#bcc9c6] mt-0.5">
            Manage and process reference materials for autonomous agent knowledge bases and vector stores.
          </p>
        </div>
        <div className="flex items-center gap-2.5">
          <button
            onClick={() => setIsPasteModalOpen(true)}
            className="flex items-center gap-1.5 h-[32px] px-3 border border-[#3d4947] rounded-[4px] bg-[#1a1c1c] hover:bg-[#282a2a] text-[#e2e2e2] font-ui-sans-bold text-[12px] transition-colors"
          >
            <span className="material-symbols-outlined text-[16px]">content_paste</span>
            <span>Paste Text</span>
          </button>
          <button
            onClick={() => setIsUploadModalOpen(true)}
            className="flex items-center gap-1.5 h-[32px] px-3.5 rounded-[4px] bg-[#6bd8cb] text-[#003732] hover:bg-[#89f5e7] font-ui-sans-bold text-[12px] transition-colors shadow-sm"
          >
            <span className="material-symbols-outlined text-[16px]">upload_file</span>
            <span>Upload File</span>
          </button>
        </div>
      </div>

      {/* Filter / Search Bar */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-3 bg-[#121414] border border-[#3d4947] p-3 rounded-[6px]">
        <div className="relative w-full sm:w-80">
          <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-[#879391] text-[16px]">
            search
          </span>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by filename or type..."
            className="w-full h-8 bg-[#1a1c1c] border border-[#3d4947] rounded-[4px] pl-8 pr-3 text-[12px] font-ui-sans-md text-[#e2e2e2] placeholder-[#879391]/60 focus:outline-none focus:border-[#6bd8cb]"
          />
        </div>
        <div className="flex items-center gap-1.5 self-start sm:self-auto">
          {["ALL", "READY", "PROCESSING", "PENDING", "FAILED"].map((status) => (
            <button
              key={status}
              onClick={() => setStatusFilter(status)}
              className={`px-2.5 py-1 rounded-[4px] text-[11px] font-mono-label transition-colors ${
                statusFilter === status
                  ? "bg-[#6bd8cb] text-[#003732] font-semibold"
                  : "bg-[#1a1c1c] text-[#bcc9c6] hover:bg-[#282a2a]"
              }`}
            >
              {status}
            </button>
          ))}
        </div>
      </div>

      {/* Data Table */}
      <div className="border border-[#3d4947] rounded-[6px] bg-[#121414] overflow-hidden flex flex-col shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse whitespace-nowrap">
            <thead>
              <tr className="border-b border-[#3d4947] bg-[#1a1c1c] text-[#bcc9c6] font-mono-label text-[11px] uppercase tracking-wider">
                <th className="py-2.5 px-4 font-medium w-1/3">Filename</th>
                <th className="py-2.5 px-4 font-medium">Type</th>
                <th className="py-2.5 px-4 font-medium">Status</th>
                <th className="py-2.5 px-4 font-medium text-right">Chunks</th>
                <th className="py-2.5 px-4 font-medium text-right">Size</th>
                <th className="py-2.5 px-4 font-medium text-right">Created Date</th>
                <th className="py-2.5 px-4 font-medium w-12 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#3d4947]/70 font-ui-sans-md text-[13px] text-[#e2e2e2]">
              {loading ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-[#879391] text-[12px]">
                    <span className="material-symbols-outlined text-[16px] animate-spin align-middle mr-2">refresh</span>
                    Loading documents...
                  </td>
                </tr>
              ) : filteredDocs.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-[#879391] text-[12px]">
                    No documents found matching the filter.
                  </td>
                </tr>
              ) : (
                filteredDocs.map((doc) => {
                  const { icon, color } = getDocIcon(doc.documentType);
                  return (
                    <tr key={doc.id} className="hover:bg-[#1a1c1c] transition-colors group">
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-2.5">
                          <span className={`material-symbols-outlined text-[18px] ${color}`}>{icon}</span>
                          <span className="font-mono-data text-[12px] text-[#e2e2e2] group-hover:text-[#6bd8cb] transition-colors">
                            {doc.fileName}
                          </span>
                        </div>
                      </td>
                      <td className="py-3 px-4 text-[#bcc9c6] font-mono-label text-[11px]">{doc.documentType}</td>
                      <td className="py-3 px-4">
                        {doc.status === "READY" && (
                          <span className="inline-flex items-center gap-1.5 px-2 py-[2px] rounded-[2px] bg-[#6bd8cb]/10 text-[#6bd8cb] font-mono-label text-[10px] border border-[#6bd8cb]/20">
                            <span className="w-1.5 h-1.5 rounded-full bg-[#6bd8cb]" />
                            READY
                          </span>
                        )}
                        {doc.status === "PROCESSING" && (
                          <span className="inline-flex items-center gap-1.5 px-2 py-[2px] rounded-[2px] bg-[#c0c1ff]/10 text-[#c0c1ff] font-mono-label text-[10px] border border-[#c0c1ff]/20">
                            <span className="material-symbols-outlined text-[12px] animate-spin">sync</span>
                            PROCESSING
                          </span>
                        )}
                        {doc.status === "PENDING" && (
                          <span className="inline-flex items-center gap-1.5 px-2 py-[2px] rounded-[2px] bg-[#1e2020] text-[#bcc9c6] font-mono-label text-[10px] border border-[#3d4947]">
                            PENDING
                          </span>
                        )}
                        {doc.status === "FAILED" && (
                          <span className="inline-flex items-center gap-1.5 px-2 py-[2px] rounded-[2px] bg-[#93000a]/20 text-[#ffb4ab] font-mono-label text-[10px] border border-[#ffb4ab]/30">
                            <span className="w-1.5 h-1.5 rounded-full bg-[#ffb4ab]" />
                            FAILED
                          </span>
                        )}
                      </td>
                      <td className="py-3 px-4 text-right font-mono-data text-[12px] text-[#bcc9c6]">
                        {doc.totalChunks.toLocaleString()}
                      </td>
                      <td className="py-3 px-4 text-right font-mono-data text-[12px] text-[#bcc9c6]">
                        {formatFileSize(doc.fileSize)}
                      </td>
                      <td className="py-3 px-4 text-right font-mono-data text-[12px] text-[#879391]">
                        {new Date(doc.createdAt).toLocaleDateString()}
                      </td>
                      <td className="py-3 px-4 text-center">
                        <button
                          onClick={() => handleDeleteDoc(doc.id)}
                          className="text-[#879391] hover:text-[#ffb4ab] transition-colors p-1 rounded hover:bg-[#282a2a]"
                          title="Delete document"
                        >
                          <span className="material-symbols-outlined text-[16px]">delete</span>
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Paste Text Modal */}
      {isPasteModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-xs p-4">
          <div className="w-full max-w-lg bg-[#1a1c1c] border border-[#3d4947] rounded-[6px] shadow-2xl overflow-hidden flex flex-col">
            <div className="px-4 py-3 border-b border-[#3d4947] flex items-center justify-between bg-[#121414]">
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-[#6bd8cb] text-[18px]">content_paste</span>
                <h3 className="font-ui-sans-bold text-[14px] text-[#e2e2e2]">Paste Reference Text</h3>
              </div>
              <button onClick={() => setIsPasteModalOpen(false)} className="text-[#bcc9c6] hover:text-[#e2e2e2]">
                <span className="material-symbols-outlined text-[18px]">close</span>
              </button>
            </div>
            <form onSubmit={handleAddPastedDoc} className="p-4 space-y-3">
              <div>
                <label className="block font-mono-label text-[11px] uppercase text-[#bcc9c6] mb-1">Document Title</label>
                <input
                  type="text"
                  required
                  value={pasteTitle}
                  onChange={(e) => setPasteTitle(e.target.value)}
                  placeholder="e.g., system_architecture_notes"
                  className="w-full bg-[#121414] border border-[#3d4947] rounded-[4px] px-3 py-1.5 text-[12px] text-[#e2e2e2] focus:outline-none focus:border-[#6bd8cb]"
                />
              </div>
              <div>
                <label className="block font-mono-label text-[11px] uppercase text-[#bcc9c6] mb-1">Raw Text / Markdown</label>
                <textarea
                  rows={6}
                  required
                  value={pasteContent}
                  onChange={(e) => setPasteContent(e.target.value)}
                  placeholder="Paste documentation, API specs, schema definitions..."
                  className="w-full bg-[#121414] border border-[#3d4947] rounded-[4px] p-2.5 text-[12px] font-mono-data text-[#e2e2e2] focus:outline-none focus:border-[#6bd8cb] resize-none"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2 border-t border-[#3d4947]">
                <button type="button" onClick={() => setIsPasteModalOpen(false)} className="px-3 py-1.5 border border-[#3d4947] rounded-[4px] text-[12px] text-[#bcc9c6]">Cancel</button>
                <button type="submit" disabled={pasting} className="px-4 py-1.5 bg-[#6bd8cb] text-[#003732] font-ui-sans-bold text-[12px] rounded-[4px] hover:bg-[#89f5e7] disabled:opacity-50 flex items-center gap-1.5">
                  {pasting && <span className="material-symbols-outlined text-[13px] animate-spin">sync</span>}
                  <span>{pasting ? "Embedding..." : "Embed Document"}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Upload File Modal */}
      {isUploadModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-xs p-4">
          <div className="w-full max-w-lg bg-[#1a1c1c] border border-[#3d4947] rounded-[6px] shadow-2xl overflow-hidden flex flex-col">
            <div className="px-4 py-3 border-b border-[#3d4947] flex items-center justify-between bg-[#121414]">
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-[#6bd8cb] text-[18px]">upload_file</span>
                <h3 className="font-ui-sans-bold text-[14px] text-[#e2e2e2]">Upload Knowledge Base File</h3>
              </div>
              <button onClick={() => setIsUploadModalOpen(false)} className="text-[#bcc9c6] hover:text-[#e2e2e2]">
                <span className="material-symbols-outlined text-[18px]">close</span>
              </button>
            </div>
            <div className="p-4 space-y-4">
              <div
                onClick={() => fileInputRef.current?.click()}
                className="border-2 border-dashed border-[#3d4947] hover:border-[#6bd8cb] rounded-[6px] p-6 text-center bg-[#121414] cursor-pointer transition-colors"
              >
                <span className="material-symbols-outlined text-[32px] text-[#6bd8cb] mb-2">cloud_upload</span>
                <p className="font-ui-sans-md text-[13px] text-[#e2e2e2]">Click to browse or drag and drop</p>
                <p className="font-mono-label text-[11px] text-[#879391] mt-1">Supported: PDF, Markdown, TXT, code files</p>
              </div>
              <input
                ref={fileInputRef}
                type="file"
                className="hidden"
                onChange={(e) => {
                  const file = e.target.files?.[0];
                  if (file) handleUploadFile(file);
                }}
              />
              {uploading && (
                <div className="flex items-center gap-2 text-[#6bd8cb] font-mono-label text-[11px]">
                  <span className="material-symbols-outlined text-[14px] animate-spin">sync</span>
                  <span>Uploading and processing...</span>
                </div>
              )}
              <div className="flex justify-end gap-2 pt-2 border-t border-[#3d4947]">
                <button onClick={() => setIsUploadModalOpen(false)} className="px-3 py-1.5 border border-[#3d4947] rounded-[4px] text-[12px] text-[#bcc9c6]">Cancel</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
