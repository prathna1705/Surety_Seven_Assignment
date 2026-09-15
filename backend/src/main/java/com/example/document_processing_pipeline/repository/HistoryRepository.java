package com.example.document_processing_pipeline.repository;

import com.example.document_processing_pipeline.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface HistoryRepository extends JpaRepository<ProcessingHistory, Long> {
  List<ProcessingHistory> findByDocumentIdOrderByTimestampAsc(String documentId);
}
