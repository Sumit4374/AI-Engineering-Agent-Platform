package sumit.ai.ai_engineering.rag.ingestion.extractor;

import java.io.IOException;
import java.io.InputStream;

import sumit.ai.ai_engineering.rag.model.DocumentType;

public interface DocumentExtractor {

    boolean supports(DocumentType documentType);

    String extractText(InputStream inputStream, String filename) throws IOException;
}
