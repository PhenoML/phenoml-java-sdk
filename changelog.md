## 11.0.0 - 2026-04-02
* The SDK client builder pattern has been redesigned with explicit, type-safe authentication entry points. The `clientId()` and `clientSecret()` builder methods have been removed; instead, use `PhenomlClient.withToken(token)` for pre-generated access tokens, `PhenomlClient.withCredentials(clientId, clientSecret)` for OAuth client credentials, or `PhenomlClient.builder()` for a fluent builder that lets you choose authentication later. Additionally, the SDK now supports configurable request logging via a new `logging(LogConfig)` builder method, instance URL configuration via `instanceUrl(String)`, custom per-request query parameters via `RequestOptions.Builder.addQueryParameter()`, and an optional `grantType` for OAuth token requests.
* The SDK now supports smarter retry behavior: the HTTP client honors `Retry-After` and `X-RateLimit-Reset` response headers to determine the optimal retry delay, falling back to capped jittered exponential backoff when neither header is present. Additionally, the streaming layer introduces `Stream.fromSseWithEventDiscrimination()`, a new factory method for SSE streams where the payload is a discriminated union keyed on the SSE `event:` field, enabling correct deserialization of heterogeneous event streams.
* A new `list(RequestOptions)` overload is now available on the agent client, making it easier to pass request options when listing PhenoAgents. All agent endpoints now support query parameters supplied via `RequestOptions`. Additionally, streaming chat requests no longer time out at the call level, improving reliability for long-running stream connections.
* A new `list(RequestOptions)` overload is now available on the agent client, allowing retrieval of PhenoAgents without specifying a full request body. Query parameters supplied via `RequestOptions` are now correctly forwarded to all agent endpoints. Streaming chat connections no longer time out prematurely due to a call-timeout fix.
* The `AsyncRawPromptsClient` now supports passing custom query parameters through `RequestOptions` on all agent prompt endpoints (`create`, `list`, `get`, `update`, `delete`, `patch`, `loadDefaults`). A new convenience overload `update(String id, RequestOptions requestOptions)` has also been added.
* The request builder types (`AgentChatRequest`, `AgentStreamChatRequest`, `AgentGetChatMessagesRequest`, `AgentListRequest`, `AgentCreateRequest`) now support `additionalProperty` and `additionalProperties` builder methods for passing arbitrary extra fields. A new `update(String id, RequestOptions)` convenience overload is available on the agent prompts client. Additionally, header-only fields such as `X-Phenoml-On-Behalf-Of` and `X-Phenoml-Fhir-Provider` are no longer incorrectly serialized into the JSON request body, and all agent prompt endpoints now correctly forward query parameters supplied via `RequestOptions`.
* The `RawAuthClient` and `AsyncRawAuthClient` now support a new `getToken(RequestOptions)` overload that uses default client credentials, making it easier to fetch tokens without constructing a `ClientCredentialsRequest`. Query parameters supplied via `RequestOptions` are now correctly forwarded to all auth endpoint requests.
* Several new convenience overloads are now available on the async client, allowing methods like `getCodeSystemDetail`, `deleteCustomCodeSystem`, `exportCustomCodeSystem`, `listCodesInACodeSystem`, and `getASpecificCode` to be called with only a `RequestOptions` argument instead of a full request object. All SDK endpoints now also support forwarding custom query parameters supplied via `RequestOptions`.
* Several `RawConstrueClient` methods now have convenience overloads that accept only a `RequestOptions` argument, removing the need to pass an empty request object for `getCodeSystemDetail`, `deleteCustomCodeSystem`, `exportCustomCodeSystem`, `listCodesInACodeSystem`, and `getASpecificCode`. All HTTP methods now also forward any query parameters set on `RequestOptions` to the request URL. Request builder classes gain new `additionalProperty` and `additionalProperties` helper methods for setting arbitrary extra fields.
* Builder classes for request and response types (including `ExportCodeSystemResponse`, `ExtractCodesResult`, `ListCodeSystemsResponse`, `ListCodesResponse`, `SemanticSearchResponse`, `TextSearchResponse`, and the Construe search/export request types) now expose `additionalProperty(String, Object)` and `additionalProperties(Map<String, Object>)` methods, allowing consumers to attach arbitrary extra fields. Builder list-setter methods (e.g. `codes()`, `results()`, `systems()`) now safely handle `null` inputs without throwing a `NullPointerException`.
* The FHIR client methods (`search`, `create`, `upsert`, `delete`, `patch`, and `executeBundle`) now support convenience overloads that accept a resource body directly, without requiring a full request wrapper object. All FHIR operations also now forward query parameters supplied via `RequestOptions` to the upstream FHIR server.
* The `RawFhirClient` now provides convenience overloads for all FHIR operations (`search`, `create`, `upsert`, `delete`, `patch`, `executeBundle`) that accept direct body arguments and/or `RequestOptions`, reducing builder boilerplate for common use cases. Query parameters supplied via `RequestOptions` are now forwarded to the FHIR server for every operation. Additionally, `FhirCreateRequest`, `FhirDeleteRequest`, and `FhirExecuteBundleRequest` builders now expose `additionalProperty` and `additionalProperties` methods for attaching arbitrary metadata.
* Builder instances for request and type classes (including `FhirPatchRequest`, `FhirSearchRequest`, `FhirUpsertRequest`, `FhirBundle`, `CohortRequest`, `Lang2FhirAndCreateRequest`, `Lang2FhirAndCreateMultiRequest`, `Lang2FhirAndSearchRequest`, `McpServerToolCallRequest`, `CreateSummaryTemplateRequest`, and `UpdateSummaryTemplateRequest`) now expose `additionalProperty(String, Object)` and `additionalProperties(Map<String, Object>)` methods for attaching arbitrary extra fields. This release also fixes a bug where passing `null` to list or map builder setters would throw a `NullPointerException`, and corrects header-only fields (`X-Phenoml-On-Behalf-Of`, `X-Phenoml-Fhir-Provider`) to no longer be inadvertently serialized into the JSON request body.
* New convenience overloads `list(RequestOptions)` and `get(id, RequestOptions)` are now available on `WorkflowsClient` and `AsyncWorkflowsClient`, making it easier to pass request options without constructing a full request object. Additionally, `RequestOptions` query parameters are now correctly forwarded on all workflow endpoints, and `create`/`update` requests now serialize all fields from the request object.
* Workflow request builders (`CreateWorkflowRequest`, `UpdateWorkflowRequest`, `ExecuteWorkflowRequest`, `WorkflowsGetRequest`, and `WorkflowsListRequest`) now expose `additionalProperty(String, Object)` and `additionalProperties(Map)` methods for passing arbitrary key-value pairs in requests. The SDK also improves robustness by guarding against null map arguments in builder setters and correctly excluding the `verbose` query parameter from serialized request bodies.
* The SDK now includes a configurable logging framework. Use `LogConfig` with `ILogger`, `LogLevel`, and `Logger` to enable HTTP request/response logging (silent by default). Additionally, `RequestOptions` query parameters are now correctly forwarded to all MCP server tool endpoints, and a new `SseEvent<T>` type is available for Server-Sent Event handling.
* Several client methods now support an optional `RequestOptions` parameter, including `AgentClient.list()`, `PromptsClient.update()`, and `AuthClient.getToken()`. Builder classes across the SDK now expose `additionalProperty` and `additionalProperties` methods for passing through arbitrary JSON fields. The SDK also adds internal support for parsing Server-Sent Events (SSE) with discriminated union types.
* All builder types across the SDK now support `additionalProperty(String key, Object value)` and `additionalProperties(Map<String, Object>)` methods, making it easier to attach arbitrary metadata when constructing request and response objects. Additionally, `ConstrueClient` and `AsyncConstrueClient` now provide `RequestOptions`-accepting overloads for `getCodeSystemDetail`, `deleteCustomCodeSystem`, `exportCustomCodeSystem`, `listCodesInACodeSystem`, and `getASpecificCode`, giving callers per-request control over timeouts and other options.
* The `FhirClient` and `AsyncFhirClient` now expose additional method overloads for `search`, `create`, `upsert`, `delete`, `patch`, and `executeBundle` — including variants that accept a `RequestOptions` parameter and simplified signatures that no longer require constructing intermediate request wrapper objects. Builder classes for FHIR-related types (`FhirResource`, `ErrorResponse`, `FhirPatchRequestBodyItem`, and others) now include fluent `additionalProperty` and `additionalProperties` helpers for passing arbitrary metadata.
* Builder classes for all request and response types across the FHIR provider, lang2fhir, and summary resources now expose `additionalProperty(String key, Object value)` and `additionalProperties(Map<String, Object> additionalProperties)` convenience methods, making it easier to attach arbitrary additional properties without constructing a full map separately.
* The `WorkflowsClient` and `AsyncWorkflowsClient` now support per-request options via new `list(RequestOptions)` and `get(String, RequestOptions)` overloads. Additionally, all builder classes across the tools and workflows packages now expose `additionalProperty(String, Object)` and `additionalProperties(Map<String, Object>)` methods, allowing arbitrary extra fields to be set without requiring model changes.

## 10.4.0 - 2026-03-31
* The `ExtractRequestConfig` now supports an optional `consistencyEffort` parameter to control result consistency across repeated extraction requests. Higher levels apply stricter filtering to remove borderline codes that may vary between calls, improving determinism at the cost of additional latency.

## 10.3.0 - 2026-03-26
* The `ExtractRequestConfig` now supports context-based relevance filtering with two new optional fields: `extractionContext` to describe extraction goals and `minContextRelevance` to set a 0.0-1.0 threshold for chunk filtering. Additionally, a new `SOAP_NOTE` chunking method is available for structured medical document processing.

## 10.2.0 - 2026-03-17
* The SDK now supports MEDITECH as a new FHIR provider option. Use `Provider.MEDITECH` to configure MEDITECH provider integrations.

## 10.1.0 - 2026-03-11
* New optional `preview` parameter available on workflow execution requests. When enabled, create operations return mock resources instead of persisting to the FHIR server. The response also includes a `preview` field to indicate whether the workflow was executed in preview mode.

## 10.0.0 - 2026-03-11
* The `ErrorResponse` class has been removed from the summary types package. If your code references `com.phenoml.api.resources.summary.types.ErrorResponse`, you'll need to update your imports and error handling logic to use alternative error response types available in the SDK.

## 9.1.0 - 2026-03-09
* feat: add HTTP 429 and 503 error handling to FHIR client
* Enhance the FHIR client with comprehensive error handling for rate limiting (429) and service unavailability (503) scenarios. This improves the SDK's resilience and provides better error feedback to developers.
* Key changes:
* Add TooManyRequestsError for HTTP 429 responses with structured error handling
* Add ServiceUnavailableError for HTTP 503 responses with generic error handling
* Update both synchronous and asynchronous FHIR clients with new error cases
* Import new error classes in client implementations
* 🌿 Generated with Fern

## 9.0.0 - 2026-03-04

### Breaking Changes

- **Authentication**: Replaced username/password authentication with OAuth 2.0 client credentials. Builders now accept `clientId()` and `clientSecret()` (defaulting to `PHENOML_CLIENT_ID` and `PHENOML_CLIENT_SECRET` environment variables). Tokens are automatically obtained and refreshed via the `/v2/auth/token` endpoint.
- **Client renamed**: `PhenoML` → `PhenomlClient`, `AsyncPhenoML` → `AsyncPhenomlClient`.
- **Builder renamed**: `PhenoMLBuilder` → `PhenomlClientBuilder`, `AsyncPhenoMLBuilder` → `AsyncPhenomlClientBuilder`.
- **Wrapper clients removed**: `Client.java` and `AsyncClient.java` convenience wrappers have been removed. Use `PhenomlClient` / `AsyncPhenomlClient` directly.

### Migration Guide

**Authentication** — replace username/password with client credentials:

```java
// Before
PhenoMLClient client = PhenoMLClient.withCredentials(
    "user", "pass", "https://yourinstance.app.pheno.ml");

// After (option 1: env vars PHENOML_CLIENT_ID and PHENOML_CLIENT_SECRET)
PhenomlClient client = PhenomlClient.builder()
    .url("https://yourinstance.app.pheno.ml")
    .build();

// After (option 2: explicit credentials)
PhenomlClient client = PhenomlClient.builder()
    .clientId("YOUR_CLIENT_ID")
    .clientSecret("YOUR_CLIENT_SECRET")
    .url("https://yourinstance.app.pheno.ml")
    .build();
```

**Import updates:**

```java
// Before
import com.phenoml.api.Client;
// or
import com.phenoml.api.wrapper.PhenoMLClient;

// After
import com.phenoml.api.PhenomlClient;
```

### Added

- New `/v2/auth/token` OAuth 2.0 client credentials endpoint with `ClientCredentialsRequest`, `TokenResponse`, and `OAuthError` types.
- `OAuthTokenSupplier` for automatic token acquisition and caching.
- `InternalServerError` error type for authtoken module.

## 8.3.0 - 2026-03-03
* feat: add document multi-resource extraction endpoint
* Add a new endpoint for extracting multiple FHIR resources from documents (PDF/images).
* This endpoint combines document text extraction with multi-resource detection,
* automatically identifying Patient, Condition, MedicationRequest, Observation, and
* other resource types, with proper resource references (e.g., Conditions reference the Patient).
* Key changes:
* Add extractMultipleFhirResourcesFromADocument method to sync and async clients
* Add DocumentMultiRequest class for request parameters
* Add UnprocessableEntityError (422) exception handling
* Add documentation and usage examples for new endpoint
* Support both PDF and image formats with automatic content type detection
* 🌿 Generated with Fern

## 8.2.0 - 2026-03-03
* feat: add feedback submission endpoint for construe extraction results
* Add new `submitFeedbackOnExtractionResults` endpoint that allows users to submit feedback on the results from the Construe extraction API. The endpoint accepts the original text, the received extraction results, expected results, and optional feedback details to help improve extraction accuracy.
* Key changes:
* Add FeedbackRequest and FeedbackResponse types for the feedback API
* Implement submitFeedbackOnExtractionResults method in sync/async client classes
* Add comprehensive error handling for 400, 401, 500, and 503 status codes
* Include documentation with usage examples and parameter descriptions
* 🌿 Generated with Fern

## 8.1.0 - 2026-03-02
* feat: change resource parameter from enum to string in DocumentRequest
* Replace the strongly-typed Resource enum with a flexible String parameter to support
* any FHIR resource type or US Core profile name. This change makes the API more
* extensible and allows developers to specify any valid FHIR resource without being
* limited to predefined options.
* Key changes:
* Replace DocumentRequestResource enum with String type for resource parameter
* Remove Resource inner class and its associated visitor pattern implementation
* Update documentation to reflect support for any FHIR resource type
* Simplify usage from .resource(DocumentRequestResource.QUESTIONNAIRE) to .resource("questionnaire")
* 🌿 Generated with Fern

## 8.0.0 - 2026-02-26
* **BREAKING**: Refactor FHIR provider auth to use discriminated unions
* `FhirProviderCreateRequest` now uses a typed `auth` union (`FhirProviderCreateRequestAuth`) instead of flat `auth_method` + optional credential fields
* `addAuthConfig` now takes a new union type `FhirProviderAddAuthConfigRequest` with discriminated auth variants
* New concrete auth types: `JwtAuth`, `ClientSecretAuth`, `OnBehalfOfAuth`, `GoogleHealthcareAuth`, `TokenPassthroughAuth`, `NoAuth`
* Add `SERVICEREQUEST` to lang2fhir resource enums in `CreateRequest` and `Lang2FhirAndCreateRequest`

## 7.1.0 - 2026-02-24
* feat: add clientId parameter to FHIR provider auth configuration
* Add clientId as an optional parameter to FhirProviderAddAuthConfigRequest and
* FhirProviderAuthConfig classes to enable OAuth client ID configuration at the
* auth config level. This enhancement provides better flexibility for OAuth
* authentication by allowing per-configuration client IDs that take precedence
* over provider-level settings.
* Key changes:
* Add clientId optional field to FhirProviderAddAuthConfigRequest with getter, setter, and builder methods
* Add clientId optional field to FhirProviderAuthConfig with appropriate JSON serialization
* Update constructors, equals, hashCode, and builder patterns in both classes
* Add documentation specifying clientId is required for jwt, client_secret, and on_behalf_of auth methods
* Deprecate provider-level client_id in FhirProviderTemplate in favor of auth config level setting
* 🌿 Generated with Fern

## 7.0.0 - 2026-02-23
* docs: update FHIR provider delete documentation and remove isActive field
* Update documentation across all FHIR provider client classes to reflect that the delete operation now performs a hard delete instead of a soft delete. The documentation previously stated that deletion would only set is_active to false, but now accurately reflects that it deletes the FHIR provider entirely.
* Additionally, remove the deprecated isActive field from multiple data classes including FhirProviderSandboxInfo, FhirProviderTemplate, McpServerResponse.Data, and McpServerToolResponse.Data. This field removal indicates a shift from soft deletion patterns to hard deletion.
* Key changes:
* Update delete method documentation in all FHIR provider client classes
* Remove isActive field and related methods from FhirProviderSandboxInfo
* Remove isActive field and related methods from FhirProviderTemplate
* Remove isActive field and related methods from McpServerResponse.Data
* Remove isActive field and related methods from McpServerToolResponse.Data
* Update constructor signatures and builder patterns to exclude isActive
* 🌿 Generated with Fern

## 6.0.0 - 2026-02-23
* refactor: simplify streaming API and update auth documentation
* This major refactor simplifies the streaming API by replacing Stream<T> with Iterable<T>
* return types across agent client methods, removing the need for manual resource management.
* Additionally, updates FHIR provider authentication documentation to provide clearer guidance
* on auth method requirements and parameter usage.
* Key changes:
* Change streamChat methods from Stream<AgentChatStreamEvent> to Iterable<AgentChatStreamEvent>
* Remove Stream import and resource management documentation from agent clients
* Add credentialExpiry parameter to FHIR provider creation requests
* Update clientId documentation to specify required auth methods (jwt, client_secret, on_behalf_of)
* Clarify scopes parameter usage and applicable auth methods
* Simplify Role enum from provider-specific values to generic read/write/admin permissions
* Update Provider visitor to include visitPhenostore method
* 🌿 Generated with Fern

## 5.4.0 - 2026-02-20
* feat: add "phenostore" FHIR provider and improve binary compatibility
* Add phenostore as a new FHIR provider option and fix Provider.Visitor interface to preserve binary compatibility with existing implementations.
* Key changes:
* Add "phenostore" as a new FHIR provider option in Provider enum
* Add default visitPhenostore() method to Provider.Visitor to avoid breaking existing implementations
* 🌿 Generated with Fern

## 5.3.0 - 2026-02-20
* feat: add streaming chat capability and simplify CI workflow
* Add streaming chat functionality to the agent API, allowing real-time Server-Sent Events (SSE) responses for better user experience. Streamline the CI/CD pipeline by removing automated tagging and simplifying publish workflow.
* Key changes:
* Add streamChat endpoint with SSE support for real-time agent responses
* Include AgentStreamChatRequest and AgentChatStreamEvent types for streaming
* Simplify GitHub Actions workflow by removing automatic tag creation job
* Update publish job to trigger on manual tags instead of automatic ones
* Change from sonatypeCentralUpload to standard publish gradle task
* Add Sonatype staging repository configuration
* Update documentation with streaming chat examples and usage
* 🌿 Generated with Fern

## 5.2.0 - 2026-02-17
* feat: add enhanced reasoning option to AgentChatRequest
* Add support for enhanced reasoning capabilities in agent chat requests. This optional parameter enables improved response quality and reliability at the cost of increased latency, giving users control over the performance vs quality tradeoff.
* Key changes:
* Add enhancedReasoning optional boolean field to AgentChatRequest class
* Update request builders to include enhanced reasoning parameter setting
* Add documentation explaining latency vs quality tradeoff
* Integrate enhanced reasoning parameter into both sync and async client request handling
* 🌿 Generated with Fern

## 5.1.1 - 2026-02-13
* fix: improve null safety and fix signing configuration
* Enhance builder pattern robustness and correct Maven signing setup to prevent potential NullPointerExceptions and fix build configuration issues.
* Key changes:
* Add null checks to addAll() and putAll() operations in builder classes
* Fix Maven signing configuration to use separate key ID and secret key parameters
* Add custom header support to PhenoML and AsyncPhenoML builders
* Remove hardcoded User-Agent header from default headers
* 🌿 Generated with Fern

## 5.1.0 - 2026-02-13
* refactor: restructure UploadRequest API to support multiple formats
* Refactors the code system upload API from a discriminated union to a single
* unified request class. The new structure better supports both CSV and JSON
* upload formats while simplifying the client interface. Returns 202 immediately
* and processes uploads asynchronously with polling for status updates.
* Key changes:
* Move UploadRequestJson to UploadRequest in requests package
* Add format enum (CSV/JSON) to specify upload type
* Add CSV-specific fields: codeCol, descCol, defnCol for column mapping
* Replace longDescription and rationale fields with reason in ExtractedCodeResult
* Add GatewayTimeoutError for 504 responses
* Update documentation to reflect asynchronous processing
* Remove async parameter - all uploads now process asynchronously
* 🌿 Generated with Fern

## 5.0.0 - 2026-02-13
* refactor: remove userId field from template classes
* This change removes the userId field and related methods from multiple template classes across the codebase. The change affects classes in agent, fhirprovider, summary, tools, and workflows modules.
* Key changes:
* Remove userId field from ChatMessageTemplate, ChatSessionTemplate, FhirProviderTemplate, SummaryTemplate, McpServerResponse.Data, McpServerToolResponse.Data, WorkflowDefinition, and WorkflowResponse classes
* Remove getUserId() methods and associated JsonProperty annotations
* Remove userId parameter from constructors and builder methods
* Remove userId-related getter/setter methods from builder classes
* Update equals(), hashCode(), and from() methods to exclude userId comparisons
* Remove userId from constructor parameter lists in build() methods
* 🌿 Generated with Fern

## 4.3.0 - 2026-02-13
* feat: add code system export and async upload support
* Add new functionality to export custom code systems and support asynchronous code system uploads with improved upload request handling.
* Key changes:
* Add exportCustomCodeSystem API with optional version parameter for exporting custom code systems as JSON
* Restructure UploadRequest to use discriminated union pattern with UploadRequestCsv and UploadRequestJson
* Add async upload support with processing status tracking (processing/ready/failed)
* Update GetCodeSystemDetailResponse to include processing status field
* Remove 403 error handling from FHIR provider endpoints
* Update documentation with new CSV-specific examples and export endpoint details
* 🌿 Generated with Fern

## 4.2.0 - 2026-02-08
* feat: add code system management and improve search documentation
* Add comprehensive code system management capabilities with new endpoints for detailed metadata retrieval and custom system deletion. Enhance API documentation with clear availability notes for different endpoint types.
* Key changes:
* Add getCodeSystemDetail endpoint to retrieve full metadata including timestamps and builtin status
* Add deleteCustomCodeSystem endpoint for removing custom (non-builtin) code systems
* Add replace parameter to upload request for overwriting existing systems
* Add ForbiddenError handling for restricted operations on builtin systems
* Update search endpoint documentation to clarify built-in vs custom system availability
* Improve parameter descriptions with validation rules and reserved name restrictions
* 🌿 Generated with Fern

## 4.1.0 - 2026-02-07
* feat: add workflows field to agents and simplify FHIR profile upload
* Enhanced agent capabilities by adding support for workflow integration and streamlined
* FHIR profile upload process by deriving metadata from StructureDefinition JSON.
* Key changes:
* Add optional `workflows` field to AgentCreateRequest and AgentTemplate for exposing workflows as tools
* Simplify ProfileUploadRequest by removing `version` and `resource` parameters
* Update FHIR profile upload to derive metadata from StructureDefinition JSON itself
* Use lowercase `id` from StructureDefinition as unique identifier and lookup key
* Add comprehensive validation rules for profile uploads (duplicate id/url checks)
* Update response model to include `type` field instead of separate resource/version fields
* 🌿 Generated with Fern

## 4.0.0 - 2026-02-03
* feat: add automatic file type detection for DocumentRequest
* Simplify the DocumentRequest API by removing the explicit fileType parameter
* and implementing automatic file type detection from base64 content magic bytes.
* This improves developer experience by eliminating the need to specify file
* types when uploading documents.
* Key changes:
* Remove fileType parameter from DocumentRequest constructor and builder
* Delete FileType enum class and related visitor pattern implementation
* Update content field documentation to specify supported formats and auto-detection
* Simplify builder pattern by removing FileTypeStage interface
* Update hashCode and equals methods to reflect parameter removal
* 🌿 Generated with Fern

## 3.1.0 - 2026-01-29
* feat: rename text search method and add citations support
* This change improves the API with better method naming and new citation capabilities for code extraction traceability.
* Key changes:
* Rename textSearchKeywordBased method to terminologyServerTextSearch for better clarity
* Add citations support to extract codes with source text references and character offsets
* Add isAncestor flag to distinguish parent codes from directly extracted codes
* Update documentation with paid plan requirements and AMA terms notices
* Add BadGatewayError (502) exception handling for FHIR operations
* Improve API descriptions for better terminology server integration
* 🌿 Generated with Fern

## 3.0.0 - 2026-01-21
* feat: make provider field required in AgentCreateRequest
* Update the AgentCreateRequest API to require the provider field instead of making it optional. This ensures that all agent creation requests must specify a provider explicitly.
* The provider field is now required during agent creation, with clear documentation indicating that in shared/experiment environments, the default sandbox provider is used if a different provider is not explicitly specified.
* Key changes:
* Change provider field from Optional<Provider> to Provider (required)
* Update builder pattern to include new ProviderStage interface
* Modify constructor to require provider parameter
* Update documentation to clarify provider requirement
* Remove optional provider setter methods from builder
* 🌿 Generated with Fern

## 2.0.0 - 2026-01-21
* feat: remove isActive field from agent prompt API
* Deprecated the isActive field from prompt and agent models, transitioning from soft delete to hard delete functionality. This change simplifies the API surface by removing the state management complexity around active/inactive prompts.
* Key changes:
* Remove isActive field from AgentPromptsCreateRequest and AgentPromptsUpdateRequest
* Remove isActive field from PromptTemplate and AgentTemplate models
* Update delete operation documentation from "soft deletes" to "deletes"
* Remove isActive parameter from builder patterns and method signatures
* Update documentation to reflect hard delete behavior
* 🌿 Generated with Fern

## 1.0.1 - 2026-01-21
* SDK regeneration
* Unable to analyze changes with AI, incrementing PATCH version.

## 1.0.0 - 2026-01-20
* feat: remove isActive parameter from agent operations
* This change removes the isActive filtering and creation parameter from the agent API, simplifying the agent management interface by removing the ability to filter agents by active status or set active status during creation.
* Key changes:
* Remove isActive parameter from AgentListRequest and associated query parameter handling
* Remove isActive field from AgentCreateRequest and update builder pattern
* Update documentation to reflect removal of active status filtering
* Simplify agent creation workflow by removing active status requirement
* 🌿 Generated with Fern

