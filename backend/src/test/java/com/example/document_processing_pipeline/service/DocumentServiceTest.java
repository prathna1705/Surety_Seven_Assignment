package com.example.document_processing_pipeline.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.document_processing_pipeline.entity.*;
import com.example.document_processing_pipeline.repository.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
  @Mock private DocumentRepository documentRepository;
  @Mock private HistoryRepository historyRepository;
  @Mock private ExtractionResultRepository extractionResultRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  private DocumentService documentService;

  @BeforeEach
  void setUp() {
    documentService =
        new DocumentService(
            documentRepository, historyRepository, extractionResultRepository, eventPublisher);
  }

  @Test
  void uploadsPdfAndPublishesProcessingEvent() {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "statement.pdf", "application/pdf", "content".getBytes(StandardCharsets.UTF_8));
    when(documentRepository.findByContentHash(anyString())).thenReturn(Optional.empty());
    when(documentRepository.save(any(Document.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    DocumentUploadResult uploadResult =
        documentService.uploadDocument(file, DocumentType.FINANCIAL_STATEMENT, "broker=acme");
    Document uploadedDocument = uploadResult.getDocument();

    assertEquals(DocumentStatus.UPLOADED, uploadedDocument.getStatus());
    assertFalse(uploadResult.isDuplicateUpload());
    verify(documentRepository).save(uploadedDocument);
    verify(historyRepository)
        .save(
            argThat(
                history ->
                    history.getDocumentId().equals(uploadedDocument.getId())
                        && history.getStatus() == DocumentStatus.UPLOADED));
    ArgumentCaptor<Object> publishedEventCaptor = ArgumentCaptor.forClass(Object.class);
    verify(eventPublisher).publishEvent(publishedEventCaptor.capture());
    DocumentUploadedEvent publishedEvent = (DocumentUploadedEvent) publishedEventCaptor.getValue();
    assertEquals(uploadedDocument.getId(), publishedEvent.documentId());
  }

  @Test
  void returnsExistingDocumentWithoutPublishingAnotherEventForDuplicateContent() {
    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "renamed.pdf",
            "application/pdf",
            "same content".getBytes(StandardCharsets.UTF_8));
    Document existingDocument = new Document("original.pdf", DocumentType.OTHER, "hash", null);
    when(documentRepository.findByContentHash(anyString()))
        .thenReturn(Optional.of(existingDocument));

    DocumentUploadResult uploadResult = documentService.uploadDocument(file, DocumentType.OTHER, null);

    assertSame(existingDocument, uploadResult.getDocument());
    assertTrue(uploadResult.isDuplicateUpload());
    verify(documentRepository, never()).save(any());
    verify(historyRepository, never()).save(any());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void rejectsEmptyFile() {
    MockMultipartFile file =
        new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> documentService.uploadDocument(file, DocumentType.OTHER, null));

    assertEquals("A non-empty PDF file is required", exception.getMessage());
    verifyNoInteractions(documentRepository, historyRepository, eventPublisher);
  }

  @Test
  void rejectsUnsupportedFileType() {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "statement.txt", "text/plain", "content".getBytes(StandardCharsets.UTF_8));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> documentService.uploadDocument(file, DocumentType.OTHER, null));

    assertEquals("Only PDF files are supported", exception.getMessage());
    verifyNoInteractions(documentRepository, historyRepository, eventPublisher);
  }
}
