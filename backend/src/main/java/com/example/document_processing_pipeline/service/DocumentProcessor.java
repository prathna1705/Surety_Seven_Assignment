package com.example.document_processing_pipeline.service;

import com.example.document_processing_pipeline.entity.ExtractionResult;

public interface DocumentProcessor {
  ExtractionResult extract(byte[] content);
}
