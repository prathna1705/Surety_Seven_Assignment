package com.example.document_processing_pipeline.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "documents",
    indexes = @Index(name = "idx_document_hash", columnList = "contentHash", unique = true))
public class Document {
  @Id private String id;

  @Column(nullable = false)
  private String filename;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentType documentType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus status;

  @Column(nullable = false, unique = true)
  private String contentHash;

  @Column(length = 1000)
  private String metadata;

  @Column(length = 500)
  private String failureReason;

  @Column(length = 2000)
  private String validationErrors;

  @Column(nullable = false)
  private int processingAttempts;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  public Document(String filename, DocumentType type, String hash, String metadata) {
    id = "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    this.filename = filename;
    documentType = type;
    contentHash = hash;
    this.metadata = metadata;
    status = DocumentStatus.UPLOADED;
    createdAt = updatedAt = Instant.now();
  }

  public void processingStarted() {
    status = DocumentStatus.PROCESSING;
    processingAttempts++;
    failureReason = null;
    updatedAt = Instant.now();
  }

  public void processingCompleted() {
    status = DocumentStatus.PROCESSED;
    failureReason = null;
    validationErrors = null;
    updatedAt = Instant.now();
  }

  public void processingFailed(String reason, String errors) {
    status = DocumentStatus.FAILED;
    failureReason = reason;
    validationErrors = errors;
    updatedAt = Instant.now();
  }
}
