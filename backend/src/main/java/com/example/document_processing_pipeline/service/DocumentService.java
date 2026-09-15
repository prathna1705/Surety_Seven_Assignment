package com.example.document_processing_pipeline.service;

import com.example.document_processing_pipeline.entity.*;
import com.example.document_processing_pipeline.repository.*;
import java.security.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentService {
  private final DocumentRepository documents;
  private final HistoryRepository history;
  private final ExtractionResultRepository extractionResults;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public Document uploadDocument(MultipartFile file, DocumentType type, String metadata) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("A non-empty PDF file is required");
    }

    if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".pdf")) {
      throw new IllegalArgumentException("Only PDF files are supported");
    }

    try {
      String hash = sha256(file.getBytes());
      Optional<Document> duplicate = documents.findByContentHash(hash);
      if (duplicate.isPresent()) {
        return duplicate.get();
      }
      Document doc = new Document(file.getOriginalFilename(), type, hash, metadata);
      Document newDocument = documents.save(doc);
      history.save(new ProcessingHistory(newDocument.getId(), DocumentStatus.UPLOADED, null, 0));
      eventPublisher.publishEvent(new DocumentUploadedEvent(newDocument.getId(), file.getBytes()));
      return doc;
    } catch (Exception e) {
      if (e instanceof IllegalArgumentException) {
        throw (IllegalArgumentException) e;
      }
      throw new IllegalStateException("Could not accept the uploaded file", e);
    }
  }

  public Document getDocument(String id) {
    return documents
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException("Document not found"));
  }

  public ExtractionResult getExtractionResult(String documentId) {
    return extractionResults.findByDocumentId(documentId).orElse(null);
  }

  public List<ProcessingHistory> getDocumentHistory(String id) {
    return history.findByDocumentIdOrderByTimestampAsc(id);
  }

  public Page<Document> getAllDocuments(
      DocumentStatus documentStatus, DocumentType documentType, Pageable pageable) {
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
