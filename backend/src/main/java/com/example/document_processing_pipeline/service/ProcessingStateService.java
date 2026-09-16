package com.example.document_processing_pipeline.service;

import com.example.document_processing_pipeline.entity.*;
import com.example.document_processing_pipeline.repository.DocumentRepository;
import com.example.document_processing_pipeline.repository.HistoryRepository;
import com.example.document_processing_pipeline.repository.ExtractionResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
class ProcessingStateService {
  private final DocumentRepository documents;
  private final HistoryRepository history;
  private final ExtractionResultRepository extractionResults;

  @Transactional
  public void beginProcessing(String documentId) {
    Document document = documents.findById(documentId).orElseThrow();
    document.processingStarted();
    documents.save(document);
    history.save(
        new ProcessingHistory(
            documentId, DocumentStatus.PROCESSING, null, document.getProcessingAttempts()));
    log.info(
        "documentId={} attempt={} status=PROCESSING", documentId, document.getProcessingAttempts());
  }

  @Transactional
  public void completeProcessing(String documentId, ExtractionResult result) {
    Document document = documents.findById(documentId).orElseThrow();
    ExtractionResult persistedResult =
        new ExtractionResult(
            documentId,
            result.getCompanyName(),
            result.getRegistrationNumber(),
            result.getAddress(),
            result.getAnnualRevenue(),
            result.getDocumentDate());
    extractionResults.save(persistedResult);
    document.processingCompleted();
    documents.save(document);
    history.save(
        new ProcessingHistory(
            documentId, DocumentStatus.PROCESSED, null, document.getProcessingAttempts()));
    log.info("documentId={} status=PROCESSED", documentId);
  }

  @Transactional
  public void failProcessing(String documentId, String reason, String validationErrors) {
    Document document = documents.findById(documentId).orElseThrow();
    document.processingFailed(reason, validationErrors);
    documents.save(document);
    history.save(
        new ProcessingHistory(
            documentId, DocumentStatus.FAILED, reason, document.getProcessingAttempts()));
    log.warn(
        "documentId={} attempt={} status=FAILED reason={} validationFailure={}",
        documentId,
        document.getProcessingAttempts(),
        reason,
        validationErrors != null);
  }
}
