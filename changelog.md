## 11.0.0 - 2026-04-03
* The client builder API has been redesigned to make authentication explicit and type-safe. The `clientId()` and `clientSecret()` builder methods have been removed; callers must now use `PhenomlClient.withCredentials(clientId, clientSecret)` for OAuth client credentials, `PhenomlClient.withToken(token)` for pre-generated access tokens, or `PhenomlClient.builder()` (which returns the new `_Builder` type) to configure authentication inline. Additionally, the return type of `PhenomlClient.builder()` has changed from `PhenomlClientBuilder` to `PhenomlClientBuilder._Builder`, requiring callers to update any stored references. New optional `logging(LogConfig)` and `instanceUrl(String)` configuration methods are also available on the builder.
* The SDK now respects server-provided rate limit headers (`Retry-After` and `X-RateLimit-Reset`) when determining retry delays, with appropriate jitter, falling back to capped exponential backoff when headers are absent. Additionally, SSE streaming now supports event-level discrimination via new `Stream.fromSseWithEventDiscrimination()` factory methods, enabling correct deserialization of discriminated union payloads where the discriminator is carried in the SSE `event` envelope field.
* The `AsyncRawAgentClient` now exposes a new `list(RequestOptions)` overload, making it easier to retrieve agents with custom request options and without requiring an explicit `AgentListRequest`. All agent endpoints now forward query parameters supplied via `RequestOptions`, and streaming chat connections no longer have a call timeout, improving reliability for long-running streams.
* A new `list(RequestOptions)` convenience overload is now available on the agent client, allowing callers to pass request options directly without constructing an `AgentListRequest`. All agent endpoints now support query parameters supplied via `RequestOptions`, and streaming chat connections no longer time out, enabling long-running server-sent event streams.
* The `AsyncRawPromptsClient` now supports passing custom query parameters through `RequestOptions` on all agent prompts endpoints. A new `update(String id, RequestOptions)` overload is also available for updating an existing prompt without providing an explicit request body.
* The `AgentChatStreamEvent.Type` inner class has been replaced by a new top-level `AgentChatStreamEventType` class. Callers referencing `AgentChatStreamEvent.Type` (or its nested `Value` enum and `Visitor` interface) must update their imports and type references to `AgentChatStreamEventType`. Additionally, HTTP header fields such as `phenomlOnBehalfOf` and `phenomlFhirProvider` on request objects are now correctly excluded from JSON body serialization and sent as headers only, fixing a serialization bug that caused them to appear in the request body.
* Several nested types have been promoted to top-level classes and renamed. `AgentCreateRequest.Provider` is now `AgentCreateRequestProvider`, `AgentTemplate.Provider` is now `AgentTemplateProvider`, `ChatMessageTemplate.Role` is now `ChatMessageTemplateRole`, and `JsonPatchOperation.Op` is now `JsonPatchOperationOp`. Callers that reference the old nested types must update their imports and usages to the new top-level equivalents. Additionally, `AsyncRawAuthClient` gains a new `getToken(RequestOptions)` overload and now correctly forwards query parameters from `RequestOptions` on all auth requests.
* The `OAuthError.Error` inner class has been renamed to `OAuthError.OAuthErrorError`. Callers that reference `OAuthError.Error` (or its static constants such as `OAuthError.Error.INVALID_CLIENT`) must update those references to `OAuthError.OAuthErrorError`. A new `getToken(RequestOptions)` convenience overload is also available on `RawAuthClient`.
* Several new convenience overloads are now available on `AsyncRawConstrueClient` (and its sync counterpart) for methods such as `getCodeSystemDetail`, `deleteCustomCodeSystem`, `exportCustomCodeSystem`, `listCodesInACodeSystem`, `getASpecificCode`, and the search endpoints — these accept a `RequestOptions` argument directly without requiring a full request object. Query parameters supplied through `RequestOptions` are now forwarded to the server on every request.
* Several new convenience overloads are now available on the Construe client — `getCodeSystemDetail`, `deleteCustomCodeSystem`, `exportCustomCodeSystem`, `listCodesInACodeSystem`, and `getASpecificCode` can each be called with only a `RequestOptions` argument, without needing to construct a full request object. All endpoints now also support forwarding custom query parameters via `RequestOptions`, and request builder classes include new `additionalProperty` and `additionalProperties` helper methods.
* Several nested enum types within `ExtractRequestConfig` and `UploadRequest` have been promoted to top-level classes. Callers must update references from `ExtractRequestConfig.ChunkingMethod`, `ExtractRequestConfig.ValidationMethod`, and `ExtractRequestConfig.ConsistencyEffort` to `ExtractRequestConfigChunkingMethod`, `ExtractRequestConfigValidationMethod`, and `ExtractRequestConfigConsistencyEffort` respectively. Similarly, `UploadRequest.Format` has been renamed to `UploadRequestFormat`.
* The `GetCodeSystemDetailResponse.Status` inner class has been replaced by a new top-level `GetCodeSystemDetailResponseStatus` type. Callers referencing `GetCodeSystemDetailResponse.Status`, `GetCodeSystemDetailResponse.Status.Value`, or `GetCodeSystemDetailResponse.Status.Visitor` must update their code to use `GetCodeSystemDetailResponseStatus` instead. Builder classes for `GetCodeSystemDetailResponse`, `ListCodesResponse`, `SemanticSearchResponse`, and `TextSearchResponse` now also support `additionalProperty` and `additionalProperties` methods for setting arbitrary extra fields.
* The FHIR client now supports convenience method overloads that accept resource body objects directly (e.g., `FhirResource`, `FhirBundle`, `List<FhirPatchRequestBodyItem>`) without requiring callers to construct request wrapper types. All FHIR operations (`search`, `create`, `upsert`, `delete`, `patch`, `executeBundle`) also now support passing `RequestOptions` query parameters, which are forwarded to the underlying FHIR server.
* The `RawFhirClient` now supports additional convenience method overloads for all FHIR operations (`search`, `create`, `upsert`, `delete`, `patch`, `executeBundle`), allowing callers to pass a resource body directly instead of constructing a request wrapper object, with or without `RequestOptions`. Query parameters supplied via `RequestOptions` are now correctly forwarded to the HTTP request URL. The `FhirCreateRequest`, `FhirDeleteRequest`, and `FhirExecuteBundleRequest` builders also expose new `additionalProperty` and `additionalProperties` methods for attaching arbitrary extra fields.
* The `FhirBundle.EntryItem` inner class (and its nested `Request`, `Response`, and `Method` types) has been removed and replaced by the top-level `FhirBundleEntryItem` class; callers must update references accordingly. Similarly, `FhirPatchRequestBodyItem.Op` has been replaced by the top-level `FhirPatchRequestBodyItemOp` class. Additionally, the `getPhenomlOnBehalfOf()`, `getPhenomlFhirProvider()`, and `getQueryParameters()` methods on FHIR request classes are now marked `@JsonIgnore`, correcting a bug where these HTTP header/query values were incorrectly included in the JSON body.
* Several nested inner types in the FHIR resource classes have been promoted to standalone top-level classes. Callers who reference `FhirResource.Meta`, `FhirProviderResponse.Data`, `FhirQueryResponse.Data`, `FhirProviderListResponse.FhirProvidersItem`, or `CreateRequest.Resource` must update their code to use the new top-level equivalents: `FhirResourceMeta`, `FhirProviderResponseData`, `FhirQueryResponseData`, `FhirProviderListResponseFhirProvidersItem`, and `CreateRequestResource` respectively.
* The `CreateMultiResponse` and `SearchResponse` types have been restructured: nested inner classes `CreateMultiResponse.Bundle`, `CreateMultiResponse.ResourcesItem`, and `SearchResponse.ResourceType` have been replaced with top-level classes `CreateMultiResponseBundle`, `CreateMultiResponseResourcesItem`, and `SearchResponseResourceType` respectively.
**Migration:** Update all references to these nested types to use the new top-level class names. The fields, methods, and serialized JSON format are otherwise unchanged.
* Several request and type classes have been restructured in this release. The inner classes `CreateSummaryRequest.Mode`, `CreateSummaryRequest.FhirResources`, and `FhirBundle.EntryItem` have been removed and replaced by top-level types `CreateSummaryRequestMode`, `CreateSummaryRequestFhirResources`, and `FhirBundleEntryItem` respectively. Callers referencing these inner classes must update their imports and type references to use the new top-level equivalents.
* Several nested static types within the `tools` package have been promoted to top-level classes. `Lang2FhirAndCreateRequest.Resource` is now `Lang2FhirAndCreateRequestResource`, `Lang2FhirAndCreateMultiResponse.ResponseBundle` is now `Lang2FhirAndCreateMultiResponseResponseBundle`, and `Lang2FhirAndCreateMultiResponse.ResourceInfoItem` is now `Lang2FhirAndCreateMultiResponseResourceInfoItem`. Update any code that references these inner types to use the new top-level class names from the `com.phenoml.api.resources.tools.types` package.
* The `McpServerResponse.Data` and `McpServerToolResponse.Data` nested classes have been replaced by the top-level types `McpServerResponseData` and `McpServerToolResponseData` respectively. Callers that reference `McpServerResponse.Data` or `McpServerToolResponse.Data` (e.g. via `getData()` or the builder's `data(...)` methods) must update their type references to the new top-level classes.
* The `fhirProviderId` field on `CreateWorkflowRequest` and `UpdateWorkflowRequest` has changed type from the nested inner classes `CreateWorkflowRequest.FhirProviderId` / `UpdateWorkflowRequest.FhirProviderId` to new top-level classes `CreateWorkflowRequestFhirProviderId` and `UpdateWorkflowRequestFhirProviderId` respectively. Callers that reference the old inner types or use `FhirProviderId.of(...)` must update their imports and usage to the new top-level classes. Additionally, new `list(RequestOptions)` and `get(String, RequestOptions)` convenience overloads are available on the workflows client, and all request builders now expose `additionalProperty` / `additionalProperties` methods.
* Several nested inner types used in workflow response classes have been promoted to standalone top-level classes. `ExecuteWorkflowResponse.Results` is now `ExecuteWorkflowResponseResults`, `WorkflowResponse.Graph` is now `WorkflowResponseGraph`, `WorkflowStep.Type` is now `WorkflowStepType`, and `WorkflowStepSummary.Type` is now `WorkflowStepSummaryType`. Callers referencing any of these inner classes must update their imports and type references accordingly.
* The SDK now includes a structured logging system for HTTP request and response visibility. Configure logging via the new `LogConfig` class with `LogLevel` (DEBUG, INFO, WARN, ERROR) and an optional custom `ILogger` implementation; logging is silent by default. Sensitive headers (e.g. `Authorization`, `X-Api-Key`) are automatically redacted in log output.
* The SDK now supports agent chat streaming with a new `AgentChatStreamEventType` enum (covering `MESSAGE_START`, `CONTENT_DELTA`, `MESSAGE_END`, `ERROR`, `TOOL_USE`, and `TOOL_RESULT` events) and an internal `SseEventParser` for Server-Sent Events handling. New types `AgentCreateRequestProvider`, `AgentTemplateProvider`, and `ChatMessageTemplateRole` are also available. Additionally, `AgentClient` and `PromptsClient` (and their async variants) now accept an optional `RequestOptions` parameter on their `list` and `update` methods respectively, and all major builder types expose new `additionalProperty` and `additionalProperties` convenience methods.
* The SDK now supports per-request `RequestOptions` on additional methods: `getToken`, `getCodeSystemDetail`, `deleteCustomCodeSystem`, `exportCustomCodeSystem`, `listCodesInACodeSystem`, and `getASpecificCode`. Two new enum types are available — `JsonPatchOperationOp` for JSON Patch operations and `OAuthErrorError` for OAuth error codes. All request and response builder classes now expose `additionalProperty` and `additionalProperties` helper methods for attaching arbitrary metadata.
* Several new capabilities are available in this release:
* Five new enum types have been introduced — `ExtractRequestConfigChunkingMethod`, `ExtractRequestConfigConsistencyEffort`, `ExtractRequestConfigValidationMethod`, `GetCodeSystemDetailResponseStatus`, and `UploadRequestFormat` — providing strongly-typed options for extraction configuration and code-system management.
* Builder classes on `DeleteCodeSystemResponse`, `ExtractedCodeResult`, `FeedbackResponse`, `GetCodeResponse`, `SemanticSearchResult`, `TextSearchResult`, and `ExtractRequestSystem` now support `additionalProperty(String, Object)` and `additionalProperties(Map)` convenience methods for attaching arbitrary metadata.
* `AsyncFhirClient` now exposes `RequestOptions`-aware overloads for all FHIR operations (search, create, upsert, delete, patch, executeBundle), as well as previously missing no-options overloads for create, upsert, and patch.
* The `FhirClient` now provides `RequestOptions`-accepting overloads for all FHIR operations (`search`, `create`, `upsert`, `delete`, `patch`, and `executeBundle`), enabling per-call control over timeouts and other request settings. Several new types are also available: `FhirBundleEntryItem`, `FhirBundleEntryItemRequest`, `FhirBundleEntryItemResponse`, `FhirBundleEntryItemRequestMethod`, `FhirPatchRequestBodyItemOp`, and `FhirResourceMeta`, providing structured representations for FHIR bundle entries and JSON Patch operations.
* Three new union types are now available in the FHIR provider module: `FhirProviderListResponseFhirProvidersItem` (representing either a `FhirProviderTemplate` or `FhirProviderSandboxInfo` in list responses), `FhirProviderResponseData` (the same union for single-item response data), and `FhirQueryResponseData` (representing either a `Map<String, Object>` or `String` from FHIR query responses). All FHIR provider builder classes now also expose `additionalProperty` and `additionalProperties` convenience methods for setting arbitrary extra fields.
* The SDK now includes new response types for multi-document FHIR extraction: `CreateMultiResponseBundle`, `CreateMultiResponseBundleEntryItem`, `CreateMultiResponseBundleEntryItemRequest`, and `CreateMultiResponseResourcesItem`. A new `CreateRequestResource` enum is available with 17 FHIR resource type values (e.g. `AUTO`, `MEDICATIONREQUEST`, `ENCOUNTER`, `PROCEDURE`). Additionally, all lang2fhir request and response builders now expose `additionalProperty` and `additionalProperties` methods for passing through custom fields.
* The SDK now supports new FHIR summarization modes via `CreateSummaryRequestMode` (`FLATTEN`, `NARRATIVE`, `IPS`) and a new `CreateSummaryRequestFhirResources` union type accepting either a `FhirResource` or `FhirBundle`. A new `SearchResponseResourceType` enum covering 30 FHIR resource types is available for lang2fhir search responses. Additionally, `Lang2FhirAndCreateMultiResponseResourceInfoItem` and `FhirBundleEntryItem` are introduced as new response types, and all major builder classes now expose `additionalProperty` and `additionalProperties` convenience methods.
* The SDK now includes new types for MCP server management: `McpServerResponseData` and `McpServerToolResponseData` expose server and tool metadata (id, name, description, URL, input schema). A new `Lang2FhirAndCreateMultiResponseResponseBundle` type and `Lang2FhirAndCreateRequestResource` enum support multi-resource FHIR creation workflows across 17 resource types. Additionally, `WorkflowsClient` and `AsyncWorkflowsClient` now accept an optional `RequestOptions` parameter on `list()` and `get()` for per-call customization.
* Several new types and builder improvements are now available in the workflows API:
* `CreateWorkflowRequestFhirProviderId` and `UpdateWorkflowRequestFhirProviderId` are new union types that accept either a single FHIR provider ID (`String`) or a list of IDs (`List<String>`).
* `ExecuteWorkflowResponseResults` exposes per-step execution results keyed by step ID.
* `WorkflowResponseGraph` provides a simplified view of workflow steps (via `WorkflowStepSummary`) without full operation details.
* `WorkflowStepType` and `WorkflowStepSummaryType` enums are now available with values `SEARCH`, `CREATE`, `WORKFLOW`, and `DECISION_NODE`.
* All workflow response and definition builder classes now expose `additionalProperty` and `additionalProperties` convenience methods for setting arbitrary extra fields.

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

