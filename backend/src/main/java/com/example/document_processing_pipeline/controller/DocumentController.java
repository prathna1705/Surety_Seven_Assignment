package com.example.document_processing_pipeline.controller;

import com.example.document_processing_pipeline.entity.*;
import com.example.document_processing_pipeline.service.DocumentService;
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
  ResponseEntity<DocumentResponse> upload(
      @RequestPart MultipartFile file,
      @RequestParam DocumentType documentType,
      @RequestParam(required = false) String metadata) {
    Document d = service.upload(file, documentType, metadata);
    return ResponseEntity.status(HttpStatus.CREATED).body(DocumentResponse.from(d));
  }

  @GetMapping
  DocumentPage list(
      @RequestParam(required = false) DocumentStatus status,
      @RequestParam(required = false) DocumentType documentType,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<Document> result =
        service.list(
            status,
            documentType,
            PageRequest.of(
                page, Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt")));
    return DocumentPage.from(result);
  }

  @GetMapping("/{id}")
  DocumentResponse get(@PathVariable String id) {
    return DocumentResponse.from(service.get(id));
  }

  @GetMapping("/{id}/history")
  List<HistoryResponse> history(@PathVariable String id) {
    return service.history(id).stream()
        .map(
            h ->
                new HistoryResponse(h.getStatus(), h.getTimestamp(), h.getReason(), h.getAttempt()))
        .toList();
  }

  record HistoryResponse(
      DocumentStatus status, java.time.Instant timestamp, String reason, int attempt) {}

  record DocumentPage(
      List<DocumentResponse> content, int page, int size, long totalElements, int totalPages) {
    static DocumentPage from(Page<Document> p) {
      return new DocumentPage(
          p.getContent().stream().map(DocumentResponse::from).toList(),
          p.getNumber(),
          p.getSize(),
          p.getTotalElements(),
          p.getTotalPages());
    }
  }
}
