package com.example.document_processing_pipeline.service;

import com.example.document_processing_pipeline.entity.*;
import com.example.document_processing_pipeline.repository.*;
import java.security.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentService {
  private final DocumentRepository documents;
  private final HistoryRepository history;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public Document upload(MultipartFile file, DocumentType type, String metadata) {
    if (file.isEmpty()) throw new IllegalArgumentException("A non-empty PDF file is required");
    if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf"))
      throw new IllegalArgumentException("Only PDF files are supported");
    try {
      String hash = sha256(file.getBytes());
      Optional<Document> duplicate = documents.findByContentHash(hash);
      if (duplicate.isPresent()) return duplicate.get();
      Document doc = documents.save(new Document(file.getOriginalFilename(), type, hash, metadata));
      history.save(new ProcessingHistory(doc, DocumentStatus.UPLOADED, null, 0));
      eventPublisher.publishEvent(new DocumentUploadedEvent(doc.getId(), file.getBytes()));
      return doc;
    } catch (Exception e) {
      if (e instanceof IllegalArgumentException) throw (IllegalArgumentException) e;
      throw new IllegalStateException("Could not accept the uploaded file", e);
    }
  }

  public Document get(String id) {
    return documents
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException("Document not found"));
  }

  public List<ProcessingHistory> history(String id) {
    get(id);
    return history.findByDocumentIdOrderByTimestampAsc(id);
  }

  public Page<Document> list(DocumentStatus s, DocumentType t, Pageable p) {
    if (s != null && t != null) return documents.findByStatusAndDocumentType(s, t, p);
    if (s != null) return documents.findByStatus(s, p);
    if (t != null) return documents.findByDocumentType(t, p);
    return documents.findAll(p);
  }

  private String sha256(byte[] bytes) throws NoSuchAlgorithmException {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
    StringBuilder b = new StringBuilder();
    for (byte value : digest) b.append(String.format("%02x", value));
    return b.toString();
  }
}
