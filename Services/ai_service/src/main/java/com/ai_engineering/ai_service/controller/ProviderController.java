package com.ai_engineering.ai_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai_engineering.ai_service.provider.ProviderHealth;
import com.ai_engineering.ai_service.provider.ProviderManager;

/**
 * Read-only visibility into the configured AI providers: which are registered
 * and their live health. Supports the Goal's provider health-monitoring
 * responsibility without coupling callers to any vendor.
 */
@RestController
@RequestMapping("/api/v1/ai/providers")
public class ProviderController {

    private final ProviderManager providerManager;

    public ProviderController(ProviderManager providerManager) {
        this.providerManager = providerManager;
    }

    @GetMapping
    public ResponseEntity<List<String>> list() {
        return ResponseEntity.ok(providerManager.providerNames());
    }

    @GetMapping("/health")
    public ResponseEntity<List<ProviderHealth>> health() {
        return ResponseEntity.ok(providerManager.healthAll());
    }
}
