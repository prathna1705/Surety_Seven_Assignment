package com.example.document_processing_pipeline.dto;

import com.example.document_processing_pipeline.entity.Document;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPageDto {
  private List<DocumentResponseDto> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;

  public static DocumentPageDto fromDocumentPage(Page<Document> documentPage) {
    List<DocumentResponseDto> documentResponses =
        documentPage.getContent().stream()
            .map(document -> DocumentResponseDto.fromDocumentAndExtractionResult(document, null))
            .toList();
    return new DocumentPageDto(
        documentResponses,
        documentPage.getNumber(),
        documentPage.getSize(),
        documentPage.getTotalElements(),
        documentPage.getTotalPages());
  }
}
