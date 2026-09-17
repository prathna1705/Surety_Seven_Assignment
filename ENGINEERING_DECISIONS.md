# Engineering Decisions

This document answers the engineering questions for the SuretySeven Document Processing Pipeline assignment.

## Why did you choose your architecture?

I chose a modular monolith: one Spring Boot application with a React frontend.

The application is split into clear responsibilities:

- **Controller layer** accepts HTTP requests and returns DTOs.
- **Service layer** handles upload, duplicate detection, processing state changes, and asynchronous workflow.
- **Repository layer** persists documents, extracted results, and processing history.
- **Async worker** runs document extraction outside the upload request.
- **React UI** provides upload, filtering, pagination, document details, extracted data, and history.

This architecture is appropriate for the assignment because it is simple to run, easy to test, and still separates business concerns. Introducing microservices, queues, or multiple databases at this scale would increase operational complexity without providing enough benefit.

## Why did you choose your database?

PostgreSQL is used for runtime data because document metadata, statuses, processing history, and extraction results are structured relational data.

PostgreSQL provides:

- durable storage across restarts
- transactions for reliable status/history writes
- unique constraints for content-hash duplicate detection
- indexes and filtering for status/document-type searches
- safe concurrent access when more than one request or worker is active

H2 is used only in automated tests. It starts quickly in memory and avoids requiring a PostgreSQL server during test execution.

## How does asynchronous processing work?

1. The upload endpoint validates the PDF and stores a `documents` row with status `UPLOADED`.
2. It stores an `UPLOADED` entry in `processing_history`.
3. It publishes a document-uploaded event.
4. A transaction event listener receives that event only after the database transaction commits.
5. The listener submits processing to a configured bounded async executor.
6. The worker records `PROCESSING`, invokes the mock processor, validates the extracted result, and records `PROCESSED` or `FAILED`.

The upload response returns immediately; it does not wait for extraction. The frontend polls the list API quietly to display current status updates.

## How do retries work?

The worker allows up to three attempts, configured in `application.yaml`.

Retryable failures:

- `PROCESSOR_TIMEOUT`
- `PROCESSOR_ERROR`

Terminal failures:

- `VALIDATION_FAILED`
- `CORRUPTED_DOCUMENT`

For a retryable failure, the worker records a `FAILED` history entry, waits for the configured retry delay, then records a new `PROCESSING` entry and tries again. Once processing succeeds, it records `PROCESSED`. If all attempts fail, the latest state remains `FAILED`.

## How do you prevent duplicate processing?

The backend calculates a SHA-256 hash from the uploaded file bytes. The `documents.content_hash` column has a unique constraint.

When a file is uploaded:

1. The service searches for an existing row with the same content hash.
2. If found, it returns the existing document with `duplicateUpload: true`.
3. It does not create a new document, history entry, or processing event.

This works even when the same file is uploaded under a different filename. The database unique constraint is also the final protection against concurrent duplicate uploads.

## What happens if the application crashes during processing?

The document row and any completed history entries remain in PostgreSQL. However, the current implementation uses an in-process event and executor. If the application crashes after the upload transaction commits but before the task starts, or while it is processing, the in-memory task is lost.

The document may remain in `UPLOADED` or `PROCESSING` status until investigated or retried manually. This is an accepted limitation of the assignment-scale design.

In production, I would use a transactional outbox table and durable queue. A background publisher would reliably send committed work to a queue, and workers would use leases, acknowledgements, and scheduled retry jobs. A recovery job could identify documents stuck in `PROCESSING` beyond a timeout and safely retry them.

## What would you change for 1 million documents per day?

I would evolve the system as follows:

- Store uploaded PDFs in object storage such as S3, not in application memory or a database.
- Replace the in-process event with a transactional outbox and durable queue such as SQS, RabbitMQ, or Kafka.
- Run independently scalable worker instances that consume queue messages.
- Use message acknowledgements, visibility timeouts, dead-letter queues, and exponential-backoff retries.
- Partition PostgreSQL tables by time or tenant where necessary and add indexes based on production query patterns.
- Add rate limiting, upload-size limits, antivirus scanning, authentication, authorization, and tenant isolation.
- Add metrics, dashboards, distributed tracing, alerts, and structured centralized logging.
- Use a real extraction/OCR provider with circuit breakers, timeout limits, and provider-specific retry policies.
- Add archival/retention policies for documents and history data.

## What are the biggest limitations of this implementation?

- The processor is mocked; it does not perform real PDF parsing, OCR, or AI extraction.
- Processing uses an in-process executor rather than a durable distributed queue.
- A crash can leave a document in `UPLOADED` or `PROCESSING` without automatic recovery.
- Files are passed as byte arrays during processing instead of being stored in object storage.
- There is no authentication, authorization, tenant model, antivirus scanning, or rate limiting.
- Database schema changes use Hibernate `ddl-auto: update`; production systems should use versioned migrations such as Flyway or Liquibase.
- UI polling is simple fixed-interval polling; server-sent events or WebSockets may be better for high-volume live updates.
- The current mock failure markers are a development/testing convenience, not real PDF corruption detection.

These limitations are deliberate trade-offs to keep the assignment solution small, readable, and runnable while leaving a clear path to production-scale improvements.
