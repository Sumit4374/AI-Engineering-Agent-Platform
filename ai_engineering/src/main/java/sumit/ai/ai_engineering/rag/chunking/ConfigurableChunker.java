package sumit.ai.ai_engineering.rag.chunking;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import sumit.ai.ai_engineering.memory.model.MemoryMessage;
import sumit.ai.ai_engineering.rag.model.ChunkMetadata;
import sumit.ai.ai_engineering.rag.model.DocumentType;

@Service
public class ConfigurableChunker implements ChunkingService {

    @Override
    public List<Chunk> chunk(
            UUID documentId,
            Long userId,
            String text,
            String fileName,
            DocumentType documentType,
            int maxTokens,
            int overlapTokens) {

        if (text == null || text.isBlank()) {
            return List.of();
        }

        int targetMaxChars = Math.max(100, maxTokens * 4);
        int overlapChars = Math.max(0, overlapTokens * 4);

        List<String> rawChunks = splitText(text, targetMaxChars, overlapChars);
        List<Chunk> result = new ArrayList<>(rawChunks.size());

        String language = inferLanguage(fileName, documentType);

        for (int i = 0; i < rawChunks.size(); i++) {
            String chunkContent = rawChunks.get(i).trim();
            if (chunkContent.isEmpty()) continue;

            UUID chunkId = UUID.randomUUID();
            ChunkMetadata metadata = new ChunkMetadata(
                    documentId,
                    chunkId,
                    fileName,
                    fileName,
                    null,
                    language,
                    LocalDateTime.now()
            );

            int estimatedTokens = MemoryMessage.estimateTokens(chunkContent);
            result.add(new Chunk(i, chunkContent, estimatedTokens, metadata));
        }

        return result;
    }

    private List<String> splitText(String text, int maxChars, int overlapChars) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + maxChars, length);

            // If we're not at the end of the text, look for a natural break point (newline or period)
            if (end < length) {
                int naturalBreak = findNaturalBreak(text, start, end);
                if (naturalBreak > start + (maxChars / 2)) {
                    end = naturalBreak;
                }
            }

            String chunk = text.substring(start, end);
            chunks.add(chunk);

            if (end >= length) {
                break;
            }

            // Move forward, subtracting overlap
            start = Math.max(start + 1, end - overlapChars);
        }

        return chunks;
    }

    private int findNaturalBreak(String text, int start, int end) {
        // Look backwards from end for double newline, single newline, or sentence end
        for (int i = end; i > start + (end - start) / 2; i--) {
            if (i < text.length() - 1 && text.charAt(i) == '\n' && text.charAt(i + 1) == '\n') {
                return i + 2;
            }
        }
        for (int i = end; i > start + (end - start) / 2; i--) {
            if (text.charAt(i) == '\n') {
                return i + 1;
            }
        }
        for (int i = end; i > start + (end - start) / 2; i--) {
            if (text.charAt(i) == '.' && (i + 1 == text.length() || Character.isWhitespace(text.charAt(i + 1)))) {
                return i + 1;
            }
        }
        return end;
    }

    private String inferLanguage(String fileName, DocumentType docType) {
        if (docType == DocumentType.MARKDOWN) return "markdown";
        if (fileName == null) return "text";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".java")) return "java";
        if (lower.endsWith(".py")) return "python";
        if (lower.endsWith(".js")) return "javascript";
        if (lower.endsWith(".ts")) return "typescript";
        if (lower.endsWith(".go")) return "go";
        if (lower.endsWith(".rs")) return "rust";
        if (lower.endsWith(".sql")) return "sql";
        if (lower.endsWith(".html")) return "html";
        if (lower.endsWith(".css")) return "css";
        if (lower.endsWith(".json")) return "json";
        if (lower.endsWith(".yaml") || lower.endsWith(".yml")) return "yaml";
        return "text";
    }
}
