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

  @ManyToOne(optional = false)
  private Document document;

  @Enumerated(EnumType.STRING)
  private DocumentStatus status;

  private Instant timestamp;
  private String reason;
  private int attempt;

  public ProcessingHistory(Document d, DocumentStatus s, String r, int a) {
    document = d;
    status = s;
    reason = r;
    attempt = a;
    timestamp = Instant.now();
  }
}
