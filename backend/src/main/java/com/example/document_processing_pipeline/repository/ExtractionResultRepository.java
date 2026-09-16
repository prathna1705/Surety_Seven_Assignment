package com.example.document_processing_pipeline.repository;

import com.example.document_processing_pipeline.entity.ExtractionResult;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtractionResultRepository extends JpaRepository<ExtractionResult, Long> {
  Optional<ExtractionResult> findByDocumentId(String documentId);
}
