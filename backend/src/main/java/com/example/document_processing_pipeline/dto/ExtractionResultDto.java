package com.example.document_processing_pipeline.dto;

import com.example.document_processing_pipeline.entity.ExtractionResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionResultDto {
  private String companyName;
  private String registrationNumber;
  private String address;
  private BigDecimal annualRevenue;
  private LocalDate documentDate;

  public static ExtractionResultDto fromExtractionResult(ExtractionResult extractionResult) {
    return new ExtractionResultDto(
        extractionResult.getCompanyName(),
        extractionResult.getRegistrationNumber(),
        extractionResult.getAddress(),
        extractionResult.getAnnualRevenue(),
        extractionResult.getDocumentDate());
  }
}
