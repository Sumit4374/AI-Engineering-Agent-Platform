package sumit.ai.ai_engineering.rag.model;

public enum DocumentType {
    TEXT,
    MARKDOWN,
    PDF,
    CODE,
    UNKNOWN;

    public static DocumentType fromFilename(String filename) {
        if (filename == null) return UNKNOWN;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".txt")) return TEXT;
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) return MARKDOWN;
        if (lower.endsWith(".pdf")) return PDF;
        if (lower.endsWith(".java") || lower.endsWith(".py") || lower.endsWith(".js")
                || lower.endsWith(".ts") || lower.endsWith(".go") || lower.endsWith(".rs")
                || lower.endsWith(".c") || lower.endsWith(".cpp") || lower.endsWith(".h")
                || lower.endsWith(".sql") || lower.endsWith(".sh") || lower.endsWith(".json")
                || lower.endsWith(".xml") || lower.endsWith(".yaml") || lower.endsWith(".yml")
                || lower.endsWith(".html") || lower.endsWith(".css")) {
            return CODE;
        }
        return UNKNOWN;
    }
}
