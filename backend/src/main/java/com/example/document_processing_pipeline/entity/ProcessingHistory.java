package com.example.document_processing_pipeline.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessingHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "document_id", nullable = false)
  private String documentId;

  @Enumerated(EnumType.STRING)
  private DocumentStatus status;

  private Instant timestamp;
  private String reason;
  private int attempt;

  public ProcessingHistory(String documentId, DocumentStatus status, String reason, int attempt) {
    this.documentId = documentId;
    this.status = status;
    this.reason = reason;
    this.attempt = attempt;
    timestamp = Instant.now();
  }
}
