package com.example.document_processing_pipeline.config;

import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfiguration {
  private final DocumentProcessingProperties processingProperties;

  @Bean("documentProcessorExecutor")
  Executor documentProcessorExecutor() {
    ThreadPoolTaskExecutor e = new ThreadPoolTaskExecutor();
    e.setCorePoolSize(processingProperties.corePoolSize());
    e.setMaxPoolSize(processingProperties.maxPoolSize());
    e.setQueueCapacity(processingProperties.queueCapacity());
    e.setThreadNamePrefix("document-processor-");
    e.setWaitForTasksToCompleteOnShutdown(true);
    e.initialize();
    return e;
  }
}
