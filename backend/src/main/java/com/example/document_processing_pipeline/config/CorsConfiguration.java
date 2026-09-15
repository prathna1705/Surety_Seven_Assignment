package com.example.document_processing_pipeline.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.cors")
public record CorsConfiguration(List<String> allowedOrigins) {}
