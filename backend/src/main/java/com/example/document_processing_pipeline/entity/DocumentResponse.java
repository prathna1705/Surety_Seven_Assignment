package com.example.document_processing_pipeline.entity;

import java.time.Instant;

public record DocumentResponse(
    String documentId,
    String filename,
    DocumentType documentType,
    DocumentStatus status,
    Instant createdAt,
    Instant updatedAt,
    int processingAttempts,
    String failureReason,
    String validationErrors,
    ExtractionResponse result) {
  public static DocumentResponse from(Document d) {
    return new DocumentResponse(
        d.getId(),
        d.getFilename(),
        d.getDocumentType(),
        d.getStatus(),
        d.getCreatedAt(),
        d.getUpdatedAt(),
        d.getProcessingAttempts(),
        d.getFailureReason(),
        d.getValidationErrors(),
        d.getResult() == null ? null : ExtractionResponse.from(d.getResult()));
  }
}
