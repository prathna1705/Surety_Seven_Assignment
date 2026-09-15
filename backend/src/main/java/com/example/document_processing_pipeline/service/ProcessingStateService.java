package com.example.document_processing_pipeline.service;

import com.example.document_processing_pipeline.entity.*;
import com.example.document_processing_pipeline.repository.DocumentRepository;
import com.example.document_processing_pipeline.repository.HistoryRepository;
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

  @Transactional
  public void begin(String documentId) {
    Document document = documents.findById(documentId).orElseThrow();
    document.startAttempt();
    documents.save(document);
    history.save(
        new ProcessingHistory(
            document, DocumentStatus.PROCESSING, null, document.getProcessingAttempts()));
    log.info(
        "documentId={} attempt={} status=PROCESSING",
        documentId,
        document.getProcessingAttempts());
  }

  @Transactional
  public void complete(String documentId, ExtractionResult result) {
    Document document = documents.findById(documentId).orElseThrow();
    document.processed(result);
    documents.save(document);
    history.save(
        new ProcessingHistory(
            document, DocumentStatus.PROCESSED, null, document.getProcessingAttempts()));
    log.info("documentId={} status=PROCESSED", documentId);
  }

  @Transactional
  public void fail(String documentId, String reason, String validationErrors) {
    Document document = documents.findById(documentId).orElseThrow();
    document.failed(reason, validationErrors);
    documents.save(document);
    history.save(
        new ProcessingHistory(
            document, DocumentStatus.FAILED, reason, document.getProcessingAttempts()));
  }
}
