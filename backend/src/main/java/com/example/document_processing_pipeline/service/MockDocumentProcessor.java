package com.example.document_processing_pipeline.service;

import com.example.document_processing_pipeline.entity.ExtractionResult;
import com.example.document_processing_pipeline.exceptions.ProcessingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class MockDocumentProcessor implements DocumentProcessor {
  public ExtractionResult extract(byte[] content) {
    String text = new String(content);
    if (text.contains("TIMEOUT")) throw new ProcessingException("PROCESSOR_TIMEOUT", true);
    if (text.contains("ERROR")) throw new ProcessingException("PROCESSOR_ERROR", true);
    if (text.contains("INVALID_RESULT"))
      return new ExtractionResult("", "", "New Delhi", new BigDecimal("-1"), null);
    return new ExtractionResult(
        "ABC Construction Pvt Ltd",
        "U12345DL2020PTC123456",
        "New Delhi",
        new BigDecimal("12500000"),
        LocalDate.of(2026, 8, 15));
  }
}
