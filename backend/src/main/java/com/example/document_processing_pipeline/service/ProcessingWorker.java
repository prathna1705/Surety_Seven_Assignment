package com.example.document_processing_pipeline.service;

import com.example.document_processing_pipeline.entity.*;
import com.example.document_processing_pipeline.config.DocumentProcessingProperties;
import com.example.document_processing_pipeline.exceptions.ProcessingException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
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
        processingStateService.begin(id);
        ExtractionResult result = processor.extract(content);
        String errors = validationErrors(result);
        if (errors != null) {
          processingStateService.fail(id, "VALIDATION_FAILED", errors);
          return;
        }
        processingStateService.complete(id, result);
        return;
      } catch (ProcessingException e) {
        log.warn("documentId={} attempt={} failed reason={}", id, attempt, e.getMessage());
        processingStateService.fail(id, e.getMessage(), null);
        if (!e.isRetryable() || attempt == processingProperties.maxAttempts()) return;
        waitBeforeRetry();
      } catch (Exception e) {
        log.error("documentId={} attempt={} unexpected processing failure", id, attempt, e);
        processingStateService.fail(id, "PROCESSOR_ERROR", null);
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

  private String validationErrors(ExtractionResult r) {
    List<String> e = new ArrayList<>();
    if (r.getCompanyName() == null || r.getCompanyName().isBlank())
      e.add("companyName is required");
    if (r.getRegistrationNumber() == null || r.getRegistrationNumber().isBlank())
      e.add("registrationNumber is required");
    if (r.getAnnualRevenue() == null || r.getAnnualRevenue().signum() < 0)
      e.add("annualRevenue must be at least 0");
    if (r.getDocumentDate() == null) e.add("documentDate must be a valid date");
    return e.isEmpty() ? null : String.join("; ", e);
  }
}
