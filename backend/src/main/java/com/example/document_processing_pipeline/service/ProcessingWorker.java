package com.example.document_processing_pipeline.service;

import com.example.document_processing_pipeline.config.DocumentProcessingProperties;
import com.example.document_processing_pipeline.entity.*;
import com.example.document_processing_pipeline.exceptions.ProcessingException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessingWorker {
  private final DocumentProcessor processor;
  private final DocumentProcessingProperties processingProperties;
  private final ProcessingStateService processingStateService;

  @Async("documentProcessorExecutor")
  public void process(String id, byte[] content) {
    log.info("documentId={} processingStarted", id);
    try {
      Thread.sleep(150);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("documentId={} processingInterruptedBeforeFirstAttempt", id);
      return;
    }
    for (int attempt = 1; attempt <= processingProperties.maxAttempts(); attempt++) {
      try {
        log.info("documentId={} attempt={} extractionStarted", id, attempt);
        processingStateService.beginProcessing(id);
        ExtractionResult result = processor.extract(content);
        String errors = validationErrors(result);
        if (errors != null) {
          log.warn("documentId={} attempt={} processingValidationFailed validationErrors={}", id, attempt, errors);
          processingStateService.failProcessing(id, "VALIDATION_FAILED", errors);
          return;
        }
        processingStateService.completeProcessing(id, result);
        log.info("documentId={} attempt={} processingCompleted", id, attempt);
        return;
      } catch (ProcessingException exception) {
        log.warn("documentId={} attempt={} failed reason={}", id, attempt, exception.getMessage());
        processingStateService.failProcessing(id, exception.getMessage(), null);
        if (!exception.isRetryable() || attempt == processingProperties.maxAttempts()) {
          log.warn("documentId={} attempt={} processingStopped retryable={}", id, attempt, exception.isRetryable());
          return;
        }
        waitBeforeRetry();
      } catch (Exception e) {
        log.error("documentId={} attempt={} unexpected processing failure", id, attempt, e);
        processingStateService.failProcessing(id, "PROCESSOR_ERROR", null);
        return;
      }
    }
  }

  private void waitBeforeRetry() {
    try {
      Thread.sleep(processingProperties.retryDelayMs());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("processingRetryWaitInterrupted");
    }
  }

  private String validationErrors(ExtractionResult extractionResult) {
    List<String> exceptions = new ArrayList<>();
    if (extractionResult.getCompanyName() == null || extractionResult.getCompanyName().isBlank()) {
      exceptions.add("companyName is required");
    }
    if (extractionResult.getRegistrationNumber() == null
        || extractionResult.getRegistrationNumber().isBlank()) {
      exceptions.add("registrationNumber is required");
    }
    if (extractionResult.getAnnualRevenue() == null
        || extractionResult.getAnnualRevenue().signum() < 0) {
      exceptions.add("annualRevenue must be at least 0");
    }
    if (extractionResult.getDocumentDate() == null) {
      exceptions.add("documentDate must be a valid date");
    }
    return exceptions.isEmpty() ? null : String.join("; ", exceptions);
  }
}
