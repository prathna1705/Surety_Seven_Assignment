package com.example.document_processing_pipeline.service;

import com.example.document_processing_pipeline.entity.Document;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DocumentUploadResult {
  private final Document document;
  private final boolean duplicateUpload;
}
