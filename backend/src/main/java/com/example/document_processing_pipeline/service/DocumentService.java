package com.example.document_processing_pipeline.service;

import com.example.document_processing_pipeline.entity.*;
import com.example.document_processing_pipeline.repository.*;
import java.security.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentService {
  private final DocumentRepository documents;
  private final HistoryRepository history;
  private final ExtractionResultRepository extractionResults;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public DocumentUploadResult uploadDocument(
      MultipartFile file, DocumentType type, String metadata) {
    log.info("filename={} documentType={} uploadValidationStarted", file.getOriginalFilename(), type);
    if (file.isEmpty()) {
      log.warn("filename={} uploadRejected reason=EMPTY_FILE", file.getOriginalFilename());
      throw new IllegalArgumentException("A non-empty PDF file is required");
    }

    if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".pdf")) {
      log.warn("filename={} uploadRejected reason=UNSUPPORTED_FILE_TYPE", file.getOriginalFilename());
      throw new IllegalArgumentException("Only PDF files are supported");
    }

    try {
      String hash = sha256(file.getBytes());
      Optional<Document> duplicate = documents.findByContentHash(hash);
      if (duplicate.isPresent()) {
        Document existingDocument = duplicate.get();
        log.info("documentId={} filename={} duplicateUploadDetected", existingDocument.getId(), file.getOriginalFilename());
        return new DocumentUploadResult(existingDocument, true);
      }
      Document doc = new Document(file.getOriginalFilename(), type, hash, metadata);
      Document newDocument = documents.save(doc);
      history.save(new ProcessingHistory(newDocument.getId(), DocumentStatus.UPLOADED, null, 0));
      eventPublisher.publishEvent(new DocumentUploadedEvent(newDocument.getId(), file.getBytes()));
      log.info("documentId={} status=UPLOADED processingEventPublished", newDocument.getId());
      return new DocumentUploadResult(doc, false);
    } catch (Exception e) {
      if (e instanceof IllegalArgumentException) {
        throw (IllegalArgumentException) e;
      }
      log.error("filename={} uploadFailed", file.getOriginalFilename(), e);
      throw new IllegalStateException("Could not accept the uploaded file", e);
    }
  }

  public Document getDocument(String id) {
    log.debug("documentId={} documentLookupStarted", id);
    return documents
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException("Document not found"));
  }

  public ExtractionResult getExtractionResult(String documentId) {
    log.debug("documentId={} extractionResultLookupStarted", documentId);
    return extractionResults.findByDocumentId(documentId).orElse(null);
  }

  public List<ProcessingHistory> getDocumentHistory(String id) {
    log.debug("documentId={} processingHistoryLookupStarted", id);
    return history.findByDocumentIdOrderByTimestampAsc(id);
  }

  public Page<Document> getAllDocuments(
      DocumentStatus documentStatus, DocumentType documentType, Pageable pageable) {
    log.debug("statusFilter={} documentTypeFilter={} page={} size={} documentListLookupStarted", documentStatus, documentType, pageable.getPageNumber(), pageable.getPageSize());
    if (documentStatus != null && documentType != null) {
      return documents.findByStatusAndDocumentType(documentStatus, documentType, pageable);
    }
    if (documentStatus != null) {
      return documents.findByStatus(documentStatus, pageable);
    }
    if (documentType != null) {
      return documents.findByDocumentType(documentType, pageable);
    }
    return documents.findAll(pageable);
  }

  private String sha256(byte[] bytes) throws NoSuchAlgorithmException {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
    StringBuilder b = new StringBuilder();
    for (byte value : digest) b.append(String.format("%02x", value));
    return b.toString();
  }
}
