package com.example.document_processing_pipeline.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExtractionResult {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String companyName;
  private String registrationNumber;
  private String address;
  private BigDecimal annualRevenue;
  private LocalDate documentDate;

  public ExtractionResult(
      String companyName,
      String registrationNumber,
      String address,
      BigDecimal annualRevenue,
      LocalDate documentDate) {
    this.companyName = companyName;
    this.registrationNumber = registrationNumber;
    this.address = address;
    this.annualRevenue = annualRevenue;
    this.documentDate = documentDate;
  }
}
