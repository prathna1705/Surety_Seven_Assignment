package com.example.document_processing_pipeline.dto;

import com.example.document_processing_pipeline.entity.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDto {
  private String documentId;
  private String filename;
  private DocumentType documentType;
  private DocumentStatus status;
  private Instant createdAt;
  private Instant updatedAt;
  private int processingAttempts;
  private String failureReason;
  private String validationErrors;
  private ExtractionResultDto result;
  private boolean duplicateUpload;

  public static DocumentResponseDto fromDocumentAndExtractionResult(
      Document document, ExtractionResult extractionResult) {
    return fromDocumentExtractionResultAndDuplicateStatus(document, extractionResult, false);
  }

  public static DocumentResponseDto fromDocumentExtractionResultAndDuplicateStatus(
      Document document, ExtractionResult extractionResult, boolean duplicateUpload) {
    return new DocumentResponseDto(
        document.getId(),
        document.getFilename(),
        document.getDocumentType(),
        document.getStatus(),
        document.getCreatedAt(),
        document.getUpdatedAt(),
        document.getProcessingAttempts(),
        document.getFailureReason(),
        document.getValidationErrors(),
        extractionResult == null
            ? null
            : ExtractionResultDto.fromExtractionResult(extractionResult),
        duplicateUpload);
  }
}
