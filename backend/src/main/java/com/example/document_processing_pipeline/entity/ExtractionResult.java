package com.example.document_processing_pipeline.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExtractionResult {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "document_id", nullable = false, unique = true, updatable = false)
  private String documentId;

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
    this(null, companyName, registrationNumber, address, annualRevenue, documentDate);
  }

  public ExtractionResult(
      String documentId,
      String companyName,
      String registrationNumber,
      String address,
      BigDecimal annualRevenue,
      LocalDate documentDate) {
    this.documentId = documentId;
    this.companyName = companyName;
    this.registrationNumber = registrationNumber;
    this.address = address;
    this.annualRevenue = annualRevenue;
    this.documentDate = documentDate;
  }
}
