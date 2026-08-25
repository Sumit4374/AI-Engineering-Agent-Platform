package sumit.ai.ai_engineering.rag.ingestion.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.rag.model.DocumentType;

@Component
public class CodeDocumentExtractor implements DocumentExtractor {

    @Override
    public boolean supports(DocumentType documentType) {
        return documentType == DocumentType.CODE;
    }

    @Override
    public String extractText(InputStream inputStream, String filename) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
