package com.example.document_processing_pipeline.controller;

import com.example.document_processing_pipeline.entity.*;
import com.example.document_processing_pipeline.dto.*;
import com.example.document_processing_pipeline.service.DocumentService;
import com.example.document_processing_pipeline.service.DocumentUploadResult;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {
  private final DocumentService service;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<DocumentResponseDto> uploadDocument(
      @RequestPart MultipartFile file,
      @RequestParam DocumentType documentType,
      @RequestParam(required = false) String metadata) {
    DocumentUploadResult uploadResult = service.uploadDocument(file, documentType, metadata);
    Document document = uploadResult.getDocument();
    ExtractionResult pdfExtractionResult = service.getExtractionResult(document.getId());
    DocumentResponseDto response =
        DocumentResponseDto.fromDocumentExtractionResultAndDuplicateStatus(
            document, pdfExtractionResult, uploadResult.isDuplicateUpload());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/getAllDocuments")
  DocumentPageDto getAllDocuments(
      @RequestParam(required = false) DocumentStatus status,
      @RequestParam(required = false) DocumentType documentType,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<Document> result =
        service.getAllDocuments(
            status,
            documentType,
            PageRequest.of(
                page, Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt")));
    return DocumentPageDto.fromDocumentPage(result);
  }

  @GetMapping("/{documentId}")
  DocumentResponseDto getDocumentDetails(@PathVariable String documentId) {
    Document document = service.getDocument(documentId);
    ExtractionResult pdfExtractionResult = service.getExtractionResult(document.getId());
    return DocumentResponseDto.fromDocumentAndExtractionResult(document, pdfExtractionResult);
  }

  @GetMapping("/{id}/history")
  List<ProcessingHistoryDto> getDocumentHistory(@PathVariable String id) {
    return service.getDocumentHistory(id).stream()
        .map(ProcessingHistoryDto::fromProcessingHistory)
        .toList();
  }
}
