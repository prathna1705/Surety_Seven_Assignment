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
    try {
      Thread.sleep(150);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return;
    }
    for (int attempt = 1; attempt <= processingProperties.maxAttempts(); attempt++) {
      try {
        processingStateService.beginProcessing(id);
        ExtractionResult result = processor.extract(content);
        String errors = validationErrors(result);
        if (errors != null) {
          processingStateService.failProcessing(id, "VALIDATION_FAILED", errors);
          return;
        }
        processingStateService.completeProcessing(id, result);
        return;
      } catch (ProcessingException exception) {
        log.warn("documentId={} attempt={} failed reason={}", id, attempt, exception.getMessage());
        processingStateService.failProcessing(id, exception.getMessage(), null);
        if (!exception.isRetryable() || attempt == processingProperties.maxAttempts()) {
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
