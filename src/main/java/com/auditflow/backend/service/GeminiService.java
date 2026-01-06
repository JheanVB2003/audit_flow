package com.auditflow.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class GeminiService {
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    private final RestClient restClient;

    @Value("${gemini.api-key}")
    private String apiKey;

    public GeminiService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public String askGemini(String prompt){
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "{\"error\": \"API Key não configurada. Verifique o application.properties\"}";
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + apiKey;

        String requestJson = """
                {
                    "contents": [{
                        "parts": [{"text": "%s"}]
                    }]
                }""".formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        try {
            return restClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .body(requestJson)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException e) {
            logger.error("Erro na API do Gemini: {}", e.getResponseBodyAsString());
            return e.getResponseBodyAsString();
        } catch (Exception e) {
            logger.error("Erro interno", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    public String getEmbedding(String text){
        String url = "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key=" + apiKey;

        String requestJson = """
            {
                "content": {
                    "parts": [{
                        "text": "%s"
                    }]
                }
            }
            """.formatted(text.replace("\"", "\\\"").replace("\n", " "));

        String rawResponse = restClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .body(requestJson)
                .retrieve()
                .body(String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawResponse);
            JsonNode valuesNode = root.path("embedding").path("values");

            if (valuesNode.isMissingNode()){
                throw new RuntimeException("Google don't return vetor:" + rawResponse);
            }
            return valuesNode.toString();
        }catch (Exception e){
            throw new RuntimeException("Error process vetor:" + e.getMessage());
        }
    }

    public String listAvailableModels() {
        String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;

        System.out.println("Consultando modelos em: " + url.replace(apiKey, "ESCONDIDO")); // Log para debug

        return restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }
}
