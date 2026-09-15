package com.example.document_processing_pipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DocumentProcessingPipelineApplication {

  public static void main(String[] args) {
    SpringApplication.run(DocumentProcessingPipelineApplication.class, args);
  }
}
