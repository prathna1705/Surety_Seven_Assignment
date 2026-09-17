# AI Collaboration Record

### AI Tool Used - Codex
This document explains how AI (Codex) was used while building this assignment. It records the questions asked, the design options discussed, and the decisions that were accepted or changed by the project owner.

It is not an application requirement. Its purpose is to make the development process transparent for code review and technical discussion.

## What AI was used for

Codex was used to:

- Generate the initial Java/Spring Boot and React/Vite implementation.
- Explain design choices in simple terms when questions were raised.
- Apply user-directed changes to backend, frontend, database, logging, tests, and documentation.
- Run backend tests and frontend builds after changes.
- Adding proper logging.

The project owner reviewed the generated implementation continuously and made the final decisions listed below.

## Backend and architecture discussion

### Database choice

**Question asked:** Which database is appropriate for this document-processing use case?

**Initial AI implementation:** H2 file database.

**Owner feedback:** Use a proper database suitable for the application.

**Final decision:** PostgreSQL is used at runtime. H2 remains only for automated tests because it is fast and does not require an external database.

**Reason:** PostgreSQL supports durable data, concurrent access, indexes, unique constraints, and a clearer production path.

### Entity mappings and database tables

**Question asked:** Is it necessary to create JPA mappings between `Document`, `ExtractionResult`, and history tables?

**Initial AI implementation:** JPA `@OneToOne` and `@ManyToOne` mappings.

**Owner feedback:** The mappings add unnecessary complexity. Store document IDs in related tables instead.

**Final decision:** There are no JPA relationship mappings.

- `documents` stores one row per document and its current status.
- `extraction_result` stores a unique `document_id` column.
- `processing_history` stores a `document_id` column.

This keeps the persistence layer simple and makes queries explicit through repositories.

### Processing-history rows

**Question asked:** Is it correct to add multiple history rows for the same document as its status changes?

**Final decision:** Yes.

`documents` contains the current status. `processing_history` is an audit table containing each transition, for example:

```text
UPLOADED → PROCESSING → FAILED → PROCESSING → PROCESSED
```

This makes retries and failures understandable without duplicating the main document row.

### Asynchronous processing event

**Question asked:** Why publish an event after file upload?

**Final decision:** The document is first saved in a transaction. After that transaction commits, an event starts background processing.

This keeps upload fast and avoids a worker trying to read a document before it is stored successfully.

### Duplicate document uploads

**Question asked:** What happens when the same file is uploaded with a different filename?

**Final decision:** Hash file bytes with SHA-256, not the filename. Identical file content produces the same hash regardless of the name.

If the hash already exists:

- No new document row is created.
- No new processing job starts.
- A structured duplicate-upload log is written.
- The response includes `duplicateUpload: true`.
- The UI shows the original filename that was uploaded.

## API and code-style discussion

### Annotations and boilerplate

**Question asked:** Use annotations where appropriate instead of writing boilerplate code.

**Final decision:** Lombok is used for straightforward boilerplate:

- `@Getter`
- `@NoArgsConstructor`
- `@RequiredArgsConstructor`
- `@Slf4j`

Meaningful state transitions remain explicit methods on the domain entity, such as starting, completing, or failing processing.

### Naming

**Question asked:** Improve variable and function names according to clean-code practice.

**Final decision:** Generic names such as `d`, `p`, `s`, `t`, and generic mapping methods like `from` were replaced or avoided where possible. Mapping methods communicate their input, for example:

```java
fromDocumentAndExtractionResult(...)
fromDocumentPage(...)
```

### Records versus DTO classes

**Question asked:** Should response records be kept inside the controller or replaced with DTOs?

**Initial AI recommendation:** Controller-scoped records are fine when used only by one controller.

**Owner feedback:** Use simple DTO classes instead.

**Final decision:** API response objects are Lombok-backed DTO classes under the `dto` package:

- `DocumentResponseDto`
- `DocumentPageDto`
- `ExtractionResultDto`
- `ProcessingHistoryDto`

## Frontend discussion

### CORS and server configuration

**Question asked:** Why was CORS hard-coded with `@CrossOrigin(origins = "http://localhost:5173")`?

**Initial AI implementation:** Controller-level hard-coded CORS origin.

**Owner feedback:** Deployment configuration should be in YAML/environment settings, not the controller.

**Final decision:** CORS, backend port, database connection, and worker settings are externalized in `application.yaml` and environment variables.

### Frontend API URL

**Question asked:** Can the frontend avoid breaking when the backend host or port changes?

**Initial AI implementation:** Hard-coded `http://localhost:8080` in React.

**Owner feedback:** Use a better way to configure it.

**Final decision:** The frontend uses a relative `/api` base URL. Vite proxies `/api` to the configurable `VITE_BACKEND_URL` value.

Changing the backend host or port only requires changing frontend environment configuration, not React source code.

Note: changing an actual API route, such as `/documents` to `/documents/getAllDocuments`, is an API contract change and requires updating the frontend route as well.

### Enum values in the upload form

**Question asked:** Why did `INSURANCE POLICY` fail to convert to the backend enum?

**Cause found:** The UI displayed a readable label and accidentally sent that label instead of `INSURANCE_POLICY`.

**Final decision:** HTML options use enum values while displaying friendly labels.

### Automatic refresh behavior

**Question asked:** Why did the document cards look as if they refreshed constantly?

**Cause found:** The UI polls every 2.5 seconds for asynchronous status updates and was enabling the full loading state on every poll.

**Final decision:** Initial load shows a loading message. Later background polls update silently, so the UI does not flicker.

### Document list design

**Question asked:** Should documents display as cards in rows, and how should unused space be handled?

**Initial AI implementation:** Responsive card grid.

**Owner feedback:** A 3/4/5-column grid can leave empty positions and make it unclear why the next document appears on the next page.

**Final decision:** Use a responsive table with ten documents per page. It displays filename, document ID, type, status, upload date, and a View details button. This better fits an internal document-management application.

### Frontend pagination

**Question asked:** Add Previous and Next controls.

**Final decision:** The UI sends `page` and `size=10` to the backend, shows the current page and total count, disables buttons at the boundaries, and returns to page zero when filters change.

## Testing work completed with AI

The automated tests cover:

- valid PDF upload
- empty-file rejection
- invalid file type rejection
- duplicate upload with a different filename
- successful processing
- invalid extracted data
- timeout followed by successful retry
- retry exhaustion
- non-retryable corruption failure

Backend tests use H2 in memory and Mockito. They do not require a running PostgreSQL database. Frontend changes are verified with `npm run build`.

## Final project conventions

Future contributors should preserve these decisions unless the project owner explicitly changes them:

- PostgreSQL for runtime and H2 only for tests.
- No JPA relationship annotations; related tables use plain `document_id` values.
- DTO classes for controller responses.
- Externalized configuration; no hard-coded backend host/port or CORS origin in source code.
- SHA-256 content-based duplicate detection.
- A main document row for current status and separate history rows for every status transition.
- Table-based frontend document list with server-side pagination.
- No visible mock-failure selector in the UI.
