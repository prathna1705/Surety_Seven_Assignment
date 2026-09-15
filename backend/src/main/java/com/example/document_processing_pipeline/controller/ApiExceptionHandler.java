package com.example.document_processing_pipeline.controller;

import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(NoSuchElementException.class)
  ResponseEntity<Map<String, String>> notFound() {
    return ResponseEntity.status(404).body(Map.of("message", "Document was not found"));
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  ResponseEntity<Map<String, String>> badRequest(RuntimeException e) {
    return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
  }
}
