package com.auditflow.backend.controller;

import com.auditflow.backend.service.GeminiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService){
        this.geminiService = geminiService;
    }

    @GetMapping(value = "/chat", produces = "application/json")
    public String chat(@RequestParam String message){
        return geminiService.askGemini(message);
    }

    @GetMapping("/models")
    public String verModelosDisponiveis() {
        return geminiService.listAvailableModels();
    }
}
