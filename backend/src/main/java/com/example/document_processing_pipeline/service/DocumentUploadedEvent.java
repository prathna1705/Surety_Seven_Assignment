package com.example.document_processing_pipeline.service;

public record DocumentUploadedEvent(String documentId, byte[] content) {}
