// lib/api.ts — Full typed API client for the AI Engineering Agent Platform

import { getToken } from "./auth";

export const BASE_URL = "http://localhost:8080";

// ─── Error Handling ──────────────────────────────────────────────────────────

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public body?: unknown
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function handleResponse<T>(res: Response): Promise<T> {
  if (res.status === 204) return undefined as T;
  if (!res.ok) {
    let body: unknown;
    try { body = await res.json(); } catch { body = await res.text(); }
    const msg =
      (body as { detail?: string })?.detail ||
      (body as { message?: string })?.message ||
      (body as { error?: string })?.error ||
      res.statusText;
    throw new ApiError(res.status, msg, body);
  }
  return res.json() as Promise<T>;
}

function authHeaders(extra?: HeadersInit): HeadersInit {
  const token = getToken();
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(extra ?? {}),
  };
}

function authHeadersNoContentType(): HeadersInit {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

// ─── Types ───────────────────────────────────────────────────────────────────

export interface LoginResponse {
  userId: number;
  userName: string;
  email: string;
  role: string;
  token: string;
}

export interface ChatResponse {
  response: string;
  conversationId: string | null;
}

export interface CodeReviewScore {
  maintainability: number;
  readability: number;
  performance: number;
  security: number;
}

export interface CodeReviewIssue {
  severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  title: string;
  explanation: string;
  location: string;
}

export interface CodeReviewRecommendation {
  title: string;
  reason: string;
}

export interface CodeReviewResponse {
  score: CodeReviewScore;
  overallAssessment: string;
  issues: CodeReviewIssue[];
  recommendation: CodeReviewRecommendation[];
}

export interface ExplainResponse {
  explanation: string;
}

export interface SummarizeResponse {
  title: string;
  summary: string;
  keyPoints: string[];
}

export interface ConversationDTO {
  id: string;
  userId: number;
  title: string;
  status: "ACTIVE" | "ARCHIVED";
  createdAt: string;
  updatedAt: string;
}

export interface MessageDTO {
  id: string;
  conversationId: string;
  role: "USER" | "ASSISTANT" | "SYSTEM" | "TOOL";
  content: string;
  tokenUsage: number | null;
  createdAt: string;
}

export interface ConversationDetailDTO extends ConversationDTO {
  messages: MessageDTO[];
}

export interface DocumentDTO {
  id: string;
  userId: number;
  fileName: string;
  contentType: string;
  documentType: "PDF" | "TEXT" | "MARKDOWN" | "CODE" | "UNKNOWN";
  fileSize: number;
  status: "PENDING" | "PROCESSING" | "READY" | "FAILED";
  totalChunks: number;
  createdAt: string;
}

export interface DocumentUploadResponse {
  id: string;
  fileName: string;
  status: string;
  totalChunks: number;
  message: string;
}

export interface RetrievedChunkDTO {
  chunkId: string;
  documentId: string;
  content: string;
  score: number;
  fileName: string;
  page: number;
  language: string;
}

export interface RagQueryResponse {
  answer: string;
  conversationId: string;
  sources: RetrievedChunkDTO[];
  totalSourcesFound: number;
}

export interface AgentStepDTO {
  id: string;
  stepIndex: number;
  stepName: string;
  toolName: string;
  inputArgs: string;
  outputResult: string;
  status: "PENDING" | "RUNNING" | "COMPLETED" | "FAILED" | "SKIPPED";
  durationMs: number;
  createdAt: string;
}

export interface AgentExecutionDTO {
  id: string;
  userId: number;
  goal: string;
  status: "RUNNING" | "COMPLETED" | "FAILED" | "CANCELLED";
  iterations: number;
  tokenUsage: number;
  startedAt: string;
  completedAt: string;
}

export interface AgentExecutionDetailDTO extends AgentExecutionDTO {
  planJson: string;
  result: string;
  error: string | null;
  steps: AgentStepDTO[];
}

export interface McpServerInfo {
  name: string;
  version: string;
  protocolVersion: string;
  toolCount: number;
  capabilities: string[];
}

export interface McpToolDefinition {
  name: string;
  description: string;
  category: string;
  inputSchema: Record<string, unknown>;
}

export interface McpToolCallResult {
  toolName: string;
  content: string;
  isError: boolean;
  errorMessage: string | null;
}

// ─── Auth ────────────────────────────────────────────────────────────────────

export async function signup(
  userName: string,
  email: string,
  password: string,
  phoneNumber?: string
): Promise<LoginResponse> {
  const res = await fetch(`${BASE_URL}/api/v1/auth/signup`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ userName, email, password, phoneNumber }),
  });
  return handleResponse<LoginResponse>(res);
}

export async function login(login: string, password: string): Promise<LoginResponse> {
  const res = await fetch(`${BASE_URL}/api/v1/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ login, password }),
  });
  return handleResponse<LoginResponse>(res);
}

// ─── AI Capabilities ─────────────────────────────────────────────────────────

export async function sendMessage(
  request: string,
  conversationId?: string
): Promise<ChatResponse> {
  const res = await fetch(`${BASE_URL}/api/v1/ai/chat`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ request, conversationId }),
  });
  return handleResponse<ChatResponse>(res);
}

export async function streamMessage(
  request: string,
  conversationId: string | undefined,
  onToken: (token: string) => void,
  onDone?: () => void
): Promise<void> {
  const res = await fetch(`${BASE_URL}/api/v1/ai/chat/stream`, {
    method: "POST",
    headers: authHeaders({ Accept: "text/event-stream" }),
    body: JSON.stringify({ request, conversationId }),
  });
  if (!res.ok) {
    let body: unknown;
    try { body = await res.json(); } catch { body = await res.text(); }
    const msg =
      (body as { detail?: string })?.detail ||
      (body as { message?: string })?.message ||
      (body as { error?: string })?.error ||
      res.statusText;
    throw new ApiError(res.status, msg, body);
  }
  const reader = res.body?.getReader();
  if (!reader) {
    throw new ApiError(res.status, "Readable stream not supported");
  }
  const decoder = new TextDecoder();
  let buffer = "";
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split("\n");
    buffer = lines.pop() || "";
    for (const line of lines) {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith(":")) continue;
      if (line.startsWith("data:")) {
        const data = line.startsWith("data: ") ? line.slice(6) : line.slice(5);
        if (data && data !== "[DONE]") {
          onToken(data);
        }
      } else {
        onToken(line);
      }
    }
  }
  if (buffer.trim()) {
    if (buffer.startsWith("data:")) {
      const data = buffer.startsWith("data: ") ? buffer.slice(6) : buffer.slice(5);
      if (data && data !== "[DONE]") onToken(data);
    } else if (!buffer.startsWith(":")) {
      onToken(buffer);
    }
  }
  onDone?.();
}

export async function reviewCode(
  code: string,
  conversationId?: string
): Promise<CodeReviewResponse> {
  const res = await fetch(`${BASE_URL}/api/v1/ai/review`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ code, conversationId }),
  });
  return handleResponse<CodeReviewResponse>(res);
}

export async function explainTopic(
  topic: string,
  conversationId?: string
): Promise<ExplainResponse> {
  const res = await fetch(`${BASE_URL}/api/v1/ai/explain`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ topic, conversationId }),
  });
  return handleResponse<ExplainResponse>(res);
}

export async function summarizeText(
  text: string,
  type: "TECHNICAL" | "GENERAL" | "RESEARCH" | "MEETING" | "EXECUTIVE",
  conversationId?: string
): Promise<SummarizeResponse> {
  const res = await fetch(`${BASE_URL}/api/v1/ai/summarize`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ text, type, conversationId }),
  });
  return handleResponse<SummarizeResponse>(res);
}

// ─── Conversations ───────────────────────────────────────────────────────────

export async function createConversation(title: string): Promise<ConversationDTO> {
  const res = await fetch(`${BASE_URL}/api/v1/conversations`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ title }),
  });
  return handleResponse<ConversationDTO>(res);
}

export async function listConversations(): Promise<ConversationDTO[]> {
  const res = await fetch(`${BASE_URL}/api/v1/conversations`, {
    headers: authHeaders(),
  });
  return handleResponse<ConversationDTO[]>(res);
}

export async function getConversation(id: string): Promise<ConversationDetailDTO> {
  const res = await fetch(`${BASE_URL}/api/v1/conversations/${id}`, {
    headers: authHeaders(),
  });
  return handleResponse<ConversationDetailDTO>(res);
}

export async function updateConversationTitle(
  id: string,
  title: string
): Promise<ConversationDTO> {
  const res = await fetch(`${BASE_URL}/api/v1/conversations/${id}/title`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify({ title }),
  });
  return handleResponse<ConversationDTO>(res);
}

export async function deleteConversation(id: string): Promise<void> {
  const res = await fetch(`${BASE_URL}/api/v1/conversations/${id}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  return handleResponse<void>(res);
}

export async function getMessages(conversationId: string): Promise<MessageDTO[]> {
  const res = await fetch(
    `${BASE_URL}/api/v1/conversations/${conversationId}/messages`,
    { headers: authHeaders() }
  );
  return handleResponse<MessageDTO[]>(res);
}

export async function appendMessage(
  conversationId: string,
  role: "USER" | "ASSISTANT" | "SYSTEM" | "TOOL",
  content: string,
  tokenUsage?: number
): Promise<MessageDTO> {
  const res = await fetch(
    `${BASE_URL}/api/v1/conversations/${conversationId}/messages`,
    {
      method: "POST",
      headers: authHeaders(),
      body: JSON.stringify({ role, content, tokenUsage }),
    }
  );
  return handleResponse<MessageDTO>(res);
}

// ─── RAG — Documents ─────────────────────────────────────────────────────────

export async function uploadDocument(file: File): Promise<DocumentUploadResponse> {
  const formData = new FormData();
  formData.append("file", file);
  const res = await fetch(`${BASE_URL}/api/v1/documents/upload`, {
    method: "POST",
    headers: authHeadersNoContentType(),
    body: formData,
  });
  return handleResponse<DocumentUploadResponse>(res);
}

export async function ingestText(
  title: string,
  content: string
): Promise<DocumentUploadResponse> {
  const res = await fetch(`${BASE_URL}/api/v1/documents/text`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ title, content }),
  });
  return handleResponse<DocumentUploadResponse>(res);
}

export async function listDocuments(): Promise<DocumentDTO[]> {
  const res = await fetch(`${BASE_URL}/api/v1/documents`, {
    headers: authHeaders(),
  });
  return handleResponse<DocumentDTO[]>(res);
}

export async function getDocument(id: string): Promise<DocumentDTO> {
  const res = await fetch(`${BASE_URL}/api/v1/documents/${id}`, {
    headers: authHeaders(),
  });
  return handleResponse<DocumentDTO>(res);
}

export async function deleteDocument(id: string): Promise<void> {
  const res = await fetch(`${BASE_URL}/api/v1/documents/${id}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  return handleResponse<void>(res);
}

// ─── RAG — Query ─────────────────────────────────────────────────────────────

export async function queryDocuments(
  query: string,
  conversationId?: string,
  topK = 5,
  minSimilarity = 0.2
): Promise<RagQueryResponse> {
  const res = await fetch(`${BASE_URL}/api/v1/rag/query`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ query, conversationId, topK, minSimilarity }),
  });
  return handleResponse<RagQueryResponse>(res);
}

export async function retrieveChunks(
  query: string,
  topK = 5,
  minSimilarity = 0.2
): Promise<RetrievedChunkDTO[]> {
  const res = await fetch(`${BASE_URL}/api/v1/rag/retrieve`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ query, topK, minSimilarity }),
  });
  return handleResponse<RetrievedChunkDTO[]>(res);
}

// ─── Autonomous Agent ─────────────────────────────────────────────────────────

export async function executeAgent(
  goal: string,
  maxIterations?: number,
  allowRag?: boolean
): Promise<AgentExecutionDetailDTO> {
  const res = await fetch(`${BASE_URL}/api/v1/agents/execute`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ goal, maxIterations, allowRag }),
  });
  return handleResponse<AgentExecutionDetailDTO>(res);
}

export async function listExecutions(): Promise<AgentExecutionDTO[]> {
  const res = await fetch(`${BASE_URL}/api/v1/agents`, {
    headers: authHeaders(),
  });
  return handleResponse<AgentExecutionDTO[]>(res);
}

export async function getExecution(id: string): Promise<AgentExecutionDetailDTO> {
  const res = await fetch(`${BASE_URL}/api/v1/agents/${id}`, {
    headers: authHeaders(),
  });
  return handleResponse<AgentExecutionDetailDTO>(res);
}

export async function cancelExecution(id: string): Promise<AgentExecutionDTO> {
  const res = await fetch(`${BASE_URL}/api/v1/agents/${id}/cancel`, {
    method: "POST",
    headers: authHeaders(),
  });
  return handleResponse<AgentExecutionDTO>(res);
}

// ─── MCP ─────────────────────────────────────────────────────────────────────

export async function getMcpServerInfo(): Promise<McpServerInfo> {
  const res = await fetch(`${BASE_URL}/api/v1/mcp/info`, {
    headers: authHeaders(),
  });
  return handleResponse<McpServerInfo>(res);
}

export async function listMcpTools(): Promise<McpToolDefinition[]> {
  const res = await fetch(`${BASE_URL}/api/v1/mcp/tools`, {
    headers: authHeaders(),
  });
  return handleResponse<McpToolDefinition[]>(res);
}

export async function executeMcpTool(
  toolName: string,
  args: Record<string, unknown>
): Promise<McpToolCallResult> {
  const res = await fetch(`${BASE_URL}/api/v1/mcp/execute`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ toolName, arguments: args }),
  });
  return handleResponse<McpToolCallResult>(res);
}
