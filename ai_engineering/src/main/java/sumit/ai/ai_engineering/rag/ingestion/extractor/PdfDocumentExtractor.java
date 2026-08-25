package sumit.ai.ai_engineering.rag.ingestion.extractor;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import sumit.ai.ai_engineering.rag.model.DocumentType;

@Component
public class PdfDocumentExtractor implements DocumentExtractor {

    private static final Logger log = LoggerFactory.getLogger(PdfDocumentExtractor.class);

    @Override
    public boolean supports(DocumentType documentType) {
        return documentType == DocumentType.PDF;
    }

    @Override
    public String extractText(InputStream inputStream, String filename) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            if (document.isEncrypted()) {
                log.warn("Encrypted PDF provided: {}", filename);
                throw new IllegalArgumentException("Encrypted PDF files are not supported: " + filename);
            }
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            log.error("Failed to parse PDF document {}: {}", filename, e.getMessage());
            throw new IOException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }
}
