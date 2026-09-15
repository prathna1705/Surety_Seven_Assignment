package com.example.document_processing_pipeline.repository;

import com.example.document_processing_pipeline.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface DocumentRepository extends JpaRepository<Document, String> {
  Optional<Document> findByContentHash(String contentHash);

  Page<Document> findByStatusAndDocumentType(
      DocumentStatus status, DocumentType type, Pageable pageable);

  Page<Document> findByStatus(DocumentStatus status, Pageable pageable);

  Page<Document> findByDocumentType(DocumentType type, Pageable pageable);
}
