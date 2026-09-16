package com.example.document_processing_pipeline.exceptions;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {
  @ExceptionHandler(NoSuchElementException.class)
  ResponseEntity<Map<String, String>> notFound(NoSuchElementException exception) {
    log.warn("requestRejected reason=DOCUMENT_NOT_FOUND");
    return ResponseEntity.status(404).body(Map.of("message", "Document was not found"));
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  ResponseEntity<Map<String, String>> badRequest(RuntimeException e) {
    log.warn("requestRejected reason={}", e.getMessage());
    return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
  }
}
