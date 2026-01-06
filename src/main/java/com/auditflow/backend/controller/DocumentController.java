package com.auditflow.backend.controller;

import com.auditflow.backend.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService docService;

    public DocumentController(DocumentService docService){
        this.docService = docService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocuments(@RequestParam("file") MultipartFile file){
        try {
            String result = docService.uploadDocument(file);

            return ResponseEntity.ok(result);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error processing document: " + e.getMessage());
        }
    }


}
