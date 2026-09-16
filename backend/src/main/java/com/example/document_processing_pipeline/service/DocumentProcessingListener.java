package com.example.document_processing_pipeline.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
class DocumentProcessingListener {
  private final ProcessingWorker processingWorker;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onDocumentUploaded(DocumentUploadedEvent event) {
    log.info("documentId={} processingEventReceived", event.documentId());
    processingWorker.process(event.documentId(), event.content());
  }
}
