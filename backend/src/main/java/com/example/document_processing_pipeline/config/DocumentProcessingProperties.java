package com.example.document_processing_pipeline.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "document-processing")
public record DocumentProcessingProperties(
    int maxAttempts, long retryDelayMs, int corePoolSize, int maxPoolSize, int queueCapacity) {}
