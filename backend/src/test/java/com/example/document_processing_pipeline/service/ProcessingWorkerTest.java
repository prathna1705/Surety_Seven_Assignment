package com.example.document_processing_pipeline.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.document_processing_pipeline.config.DocumentProcessingProperties;
import com.example.document_processing_pipeline.entity.ExtractionResult;
import com.example.document_processing_pipeline.exceptions.ProcessingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessingWorkerTest {
  @Mock private DocumentProcessor documentProcessor;
  @Mock private ProcessingStateService processingStateService;
  private ProcessingWorker processingWorker;

  @BeforeEach
  void setUp() {
    processingWorker =
        new ProcessingWorker(
            documentProcessor,
            new DocumentProcessingProperties(3, 0, 1, 1, 10),
            processingStateService);
  }

  @Test
  void completesProcessingForValidExtraction() {
    ExtractionResult extractionResult = validResult();
    when(documentProcessor.extract(any())).thenReturn(extractionResult);

    processingWorker.process("DOC-1", new byte[0]);

    InOrder calls = inOrder(processingStateService);
    calls.verify(processingStateService).beginProcessing("DOC-1");
    calls.verify(processingStateService).completeProcessing("DOC-1", extractionResult);
    verify(processingStateService, never()).failProcessing(anyString(), anyString(), any());
  }

  @Test
  void marksInvalidExtractionAsValidationFailureWithoutRetrying() {
    when(documentProcessor.extract(any()))
        .thenReturn(new ExtractionResult("", "", "address", new BigDecimal("-1"), null));

    processingWorker.process("DOC-2", new byte[0]);

    verify(processingStateService).beginProcessing("DOC-2");
    verify(processingStateService)
        .failProcessing(eq("DOC-2"), eq("VALIDATION_FAILED"), contains("companyName is required"));
    verify(processingStateService, never()).completeProcessing(anyString(), any());
    verify(documentProcessor, times(1)).extract(any());
  }

  @Test
  void retriesTimeoutThenCompletesSuccessfully() {
    ExtractionResult extractionResult = validResult();
    when(documentProcessor.extract(any()))
        .thenThrow(new ProcessingException("PROCESSOR_TIMEOUT", true))
        .thenReturn(extractionResult);

    processingWorker.process("DOC-3", new byte[0]);

    verify(processingStateService, times(2)).beginProcessing("DOC-3");
    verify(processingStateService).failProcessing("DOC-3", "PROCESSOR_TIMEOUT", null);
    verify(processingStateService).completeProcessing("DOC-3", extractionResult);
  }

  @Test
  void stopsAfterConfiguredRetriesAreExhausted() {
    when(documentProcessor.extract(any()))
        .thenThrow(new ProcessingException("PROCESSOR_TIMEOUT", true));

    processingWorker.process("DOC-4", new byte[0]);

    verify(processingStateService, times(3)).beginProcessing("DOC-4");
    verify(processingStateService, times(3)).failProcessing("DOC-4", "PROCESSOR_TIMEOUT", null);
    verify(processingStateService, never()).completeProcessing(anyString(), any());
  }

  private ExtractionResult validResult() {
    return new ExtractionResult("Company", "REG-1", "Address", BigDecimal.ZERO, LocalDate.now());
  }
}
