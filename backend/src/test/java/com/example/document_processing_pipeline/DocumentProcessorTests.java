package com.example.document_processing_pipeline;
import com.example.document_processing_pipeline.entity.ExtractionResult;
import com.example.document_processing_pipeline.exceptions.ProcessingException;
import com.example.document_processing_pipeline.service.*; import org.junit.jupiter.api.Test; import java.math.BigDecimal; import static org.junit.jupiter.api.Assertions.*;
class DocumentProcessorTests {
 private final MockDocumentProcessor processor=new MockDocumentProcessor();
 @Test void extractsSuccessfulDocument(){ExtractionResult r=processor.extract("normal".getBytes());assertEquals("ABC Construction Pvt Ltd",r.getCompanyName());assertEquals(0,new BigDecimal("12500000").compareTo(r.getAnnualRevenue()));}
 @Test void returnsInvalidExtractionForInvalidResultMarker(){ExtractionResult r=processor.extract("INVALID_RESULT".getBytes());assertTrue(r.getCompanyName().isBlank());assertTrue(r.getAnnualRevenue().signum()<0);}
 @Test void reportsTimeoutAsRetryable(){
  ProcessingException e=assertThrows(ProcessingException.class,()->processor.extract("TIMEOUT".getBytes()));assertTrue(e.isRetryable());assertEquals("PROCESSOR_TIMEOUT",e.getMessage());}
}
