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

        Document doc =createAndSaveDocument(file);
        System.out.println("Saved document with ID: " + doc.getId());

        String fullText = extractContent(file);
        System.out.println("Extracted text. Total Size: " + fullText.length());

        int count = processAndVectorize(fullText, doc.getId());

        return "Processing completed!" + count + " Parts were vectorized and saved.";
    }

    private Document createAndSaveDocument(MultipartFile file) throws IOException {
        Document doc = new Document();
        doc.setFileName(file.getOriginalFilename());
        doc.setUploadDate(LocalDateTime.now());
        return documentRepository.save(doc);
    }

    private String extractContent(MultipartFile file) throws IOException{
        String fullText = pdfService.extractText(file);

        if (fullText == null || fullText.isEmpty()){
            throw new RuntimeException("The document is empty or could not be read.");
        }
        return fullText;
    }

    private int processAndVectorize(String fullText, Long docId){
        List<String> chunks = splitText(fullText, 1000);
        int count = 0;
        for (String chunkText : chunks) {
            try {
                vectorizeChunk(chunkText, docId);
                count++;
                System.out.println("Processing chunk " + count + "/" + chunks.size());
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Error processing chunk \" + count + \": \" + e.getMessage()");
            }
        }
        return count;
    }

    private void vectorizeChunk(String chunkText, Long docId){
        String vectorString = geminiService.getEmbedding(chunkText);
        documentChunkRepository.saveVector(chunkText, vectorString, docId);
    }

    private List<String> splitText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        int start = 0;

        while (start < length){
            int end = Math.min(start + chunkSize, length);

            if (end < length){
                int lastSpace = text.substring(start, end).lastIndexOf(' ');

                if (lastSpace > 0){
                    end = start + lastSpace;
                }

            }
            String chunk = text.substring(start, end).trim();

            if (!chunk.isEmpty()){
                chunks.add(chunk);
            }

            start = end + 1;
        }
        return chunks;
    }

}
