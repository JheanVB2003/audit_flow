package com.auditflow.backend.service;

import com.auditflow.backend.entity.DocumentChunck;
import com.auditflow.backend.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final GeminiService geminiService;
    private final DocumentChunkRepository documentChunkRepository;

    public AuditService(GeminiService geminiService, DocumentChunkRepository documentChunkRepository) {
        this.geminiService = geminiService;
        this.documentChunkRepository = documentChunkRepository;
    }

    public String askDocument(String question) {
        //Transformar a pergunta do usuário em números (Vetor)
        String questionVector = geminiService.getEmbedding(question);

        //Ir no banco e buscar os trechos mais relevantes
        List<DocumentChunck> similarChunks = documentChunkRepository.findSimilarChunks(questionVector);

        //Montar o texto de contexto (Juntar os pedaços achados)
        String context = similarChunks.stream()
                .map(DocumentChunck::getContent)
                .collect(Collectors.joining("\n---\n"));

        String prompt = """
                Você é um auditor jurídico experiente.
                Use APENAS o contexto abaixo para responder à pergunta.
                Se a resposta não estiver no contexto, diga "Não encontrei essa informação no documento".
                              
                CONTEXTO DO CONTRATO:
                %s
                               
                PERGUNTA: %s              
                """.formatted(context, question);

        return geminiService.askGemini(prompt);

    }

}
