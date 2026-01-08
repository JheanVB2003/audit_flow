package com.auditflow.backend.service;

import com.auditflow.backend.entity.Document;
import com.auditflow.backend.repository.DocumentChunkRepository;
import com.auditflow.backend.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService{

    private final PdfService pdfService;
    private final GeminiService geminiService;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentRepository documentRepository;

    public DocumentService(PdfService pdfService, GeminiService geminiService, DocumentChunkRepository documentChunkRepository, DocumentRepository documentRepository) {
        this.pdfService = pdfService;
        this.geminiService = geminiService;
        this.documentChunkRepository = documentChunkRepository;
        this.documentRepository = documentRepository;
    }

    public String uploadDocument(MultipartFile file) throws IOException {

        Document doc = new Document();
        doc.setFileName(file.getOriginalFilename());
        doc.setUploadDate(LocalDateTime.now());

        doc =  documentRepository.save(doc);
        Long realDocId = doc.getId();

        System.out.println("Saved document with ID: " + realDocId);

        String fullText = pdfService.extractText(file);

        if (fullText == null || fullText.isEmpty()){
            throw new RuntimeException("The document is empty or could not be read.");
        }

        System.out.println("Extracted text. Total Size: " + fullText.length());

        List<String> chunks = splitText(fullText,1000);

        int count = 0;
        for (String chunkText : chunks){
            try{
                String vectorString = geminiService.getEmbedding(chunkText);

                documentChunkRepository.saveVector(chunkText, vectorString, realDocId);

                count++;
                System.out.println("Processing chunk " + count + "/" + chunks.size());

                Thread.sleep(1000);
            }catch (Exception e){
                System.out.println("Error processing chunk " + count + ": " + e.getMessage());
            }
        }

        documentRepository.save(doc);

        return "Processing completed!" + count + " Parts were vectorized and saved.";
    }

    private List<String> splitText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += chunkSize) {
            chunks.add(text.substring(i, Math.min(length, i + chunkSize)));
        }
        return chunks;
    }

}
