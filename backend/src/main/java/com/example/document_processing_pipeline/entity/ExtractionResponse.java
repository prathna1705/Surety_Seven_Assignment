package com.example.document_processing_pipeline.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

record ExtractionResponse(
    String companyName,
    String registrationNumber,
    String address,
    BigDecimal annualRevenue,
    LocalDate documentDate) {
  static ExtractionResponse from(ExtractionResult r) {
    return new ExtractionResponse(
        r.getCompanyName(),
        r.getRegistrationNumber(),
        r.getAddress(),
        r.getAnnualRevenue(),
        r.getDocumentDate());
  }
}
