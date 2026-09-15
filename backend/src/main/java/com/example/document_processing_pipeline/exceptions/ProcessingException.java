package com.example.document_processing_pipeline.exceptions;

import lombok.Getter;

@Getter
public class ProcessingException extends RuntimeException {
  private final boolean retryable;

  public ProcessingException(String message, boolean retryable) {
    super(message);
    this.retryable = retryable;
  }
}
