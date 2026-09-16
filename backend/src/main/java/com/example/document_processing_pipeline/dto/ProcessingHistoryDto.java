package com.example.document_processing_pipeline.dto;

import com.example.document_processing_pipeline.entity.ProcessingHistory;
import com.example.document_processing_pipeline.entity.DocumentStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingHistoryDto {
  private DocumentStatus status;
  private Instant timestamp;
  private String reason;
  private int attempt;

  public static ProcessingHistoryDto fromProcessingHistory(ProcessingHistory processingHistory) {
    return new ProcessingHistoryDto(
        processingHistory.getStatus(),
        processingHistory.getTimestamp(),
        processingHistory.getReason(),
        processingHistory.getAttempt());
  }
}
