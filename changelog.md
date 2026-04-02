## 11.0.0 - 2026-04-02
* The `PhenomlClient` and `AsyncPhenomlClient` builder API has been redesigned with explicit, typed authentication entry points. The `builder()` method now returns a `_Builder` step-builder (previously `PhenomlClientBuilder`), and the `clientId()`/`clientSecret()` methods have been removed. Callers must migrate to `PhenomlClient.withCredentials(clientId, clientSecret)` for OAuth client credentials, or `PhenomlClient.withToken(token)` for pre-generated access tokens. Additionally, new `logging(LogConfig)` and `instanceUrl(String)` options are available on the builder, and `RequestOptions` now supports per-request query parameter injection via `addQueryParameter()`.
* The SDK now features smarter HTTP retry behavior: the retry interceptor respects `Retry-After` and `X-RateLimit-Reset` response headers to derive wait times before falling back to exponential backoff with jitter. Additionally, SSE streaming now supports event-level discrimination via two new `Stream.fromSseWithEventDiscrimination()` factory methods, enabling correct deserialization of discriminated-union SSE payloads keyed on the SSE `event` field.
* A new `list(RequestOptions)` overload is now available on the agent client, allowing callers to pass request options when listing PhenoAgents without a filter body. All agent API methods now forward query parameters from `RequestOptions` to the server, enabling greater request customization. Additionally, streaming chat requests no longer time out on long-running connections.
* The `RawAgentClient` now supports a new `list(RequestOptions)` convenience overload, allowing callers to pass request options directly without constructing an `AgentListRequest`. All agent endpoints now honor query parameters supplied via `RequestOptions`, and streaming chat connections have improved timeout handling to prevent premature disconnects.
* The `AsyncRawPromptsClient` now exposes a new `update(String id, RequestOptions requestOptions)` overload, making it easier to update a prompt using default request body values. All agent prompts endpoints also now correctly forward query parameters supplied via `RequestOptions` to the server.
* The `AgentChatStreamEvent.getType()` method now returns `Optional<AgentChatStreamEventType>` instead of the previous `Optional<AgentChatStreamEvent.Type>` inner class. Callers that reference `AgentChatStreamEvent.Type`, its `Value` enum, or its `Visitor` interface must migrate to the new standalone `AgentChatStreamEventType` class. Additionally, several request fields previously annotated with `@JsonProperty` (such as `X-Phenoml-On-Behalf-Of`, `X-Phenoml-Fhir-Provider`, `chat_session_id`, `num_messages`, `role`, `order`, and `tags`) are now marked `@JsonIgnore`, which changes how these fields are serialized in JSON contexts.
* Several nested inner types have been extracted into standalone top-level classes. `AgentCreateRequest.Provider` is now `AgentCreateRequestProvider`, `AgentTemplate.Provider` is now `AgentTemplateProvider`, `ChatMessageTemplate.Role` is now `ChatMessageTemplateRole`, and `JsonPatchOperation.Op` is now `JsonPatchOperationOp`. Any code referencing these types by their old qualified names (e.g. `AgentCreateRequest.Provider`) must be updated to use the new top-level class names.
* The `OAuthError` type's inner `Error` class (and its associated `Value` enum and `Visitor` interface) has been removed and replaced by the new top-level `OAuthErrorError` type. Callers referencing `OAuthError.Error`, `OAuthError.Error.Value`, or `OAuthError.Error.Visitor` must update to use `OAuthErrorError` and its equivalents. Additionally, a new `getToken(RequestOptions)` overload is now available on `RawAuthClient`, and all auth token endpoints now support passing custom query parameters via `RequestOptions`.
* Several client methods now support new convenience overloads that accept only required path parameters and `RequestOptions`, removing the need to construct an empty request builder. Affected methods include `getCodeSystemDetail`, `deleteCustomCodeSystem`, `exportCustomCodeSystem`, `listCodesInACodeSystem`, and `getASpecificCode`. Additionally, any query parameters set on `RequestOptions` are now forwarded to the outgoing request URL across all endpoints.
* Several new convenience overloads are now available on the `RawConstrueClient`: `getCodeSystemDetail`, `deleteCustomCodeSystem`, `exportCustomCodeSystem`, `listCodesInACodeSystem`, and `getASpecificCode` can each be called with only a `codesystem` (and optional `RequestOptions`) argument, without constructing an explicit request object. Additionally, all HTTP methods now forward custom query parameters supplied via `RequestOptions`, giving callers finer control over outgoing requests.
* Several nested enum types within `ExtractRequestConfig` and `UploadRequest` have been promoted to top-level classes. `ExtractRequestConfig.ChunkingMethod`, `ExtractRequestConfig.ValidationMethod`, and `ExtractRequestConfig.ConsistencyEffort` are now `ExtractRequestConfigChunkingMethod`, `ExtractRequestConfigValidationMethod`, and `ExtractRequestConfigConsistencyEffort` respectively; `UploadRequest.Format` is now `UploadRequestFormat`. Update all references and imports to use the new top-level class names.
* The `GetCodeSystemDetailResponse.Status` inner class has been renamed to the top-level `GetCodeSystemDetailResponseStatus` type. Callers that reference `GetCodeSystemDetailResponse.Status`, its `Value` enum, or its `Visitor` interface must update to use `GetCodeSystemDetailResponseStatus` instead. Builder classes across several response types (`ListCodeSystemsResponse`, `ListCodesResponse`, `SemanticSearchResponse`, `TextSearchResponse`) now expose `additionalProperty` and `additionalProperties` convenience methods.
* The async FHIR client now offers convenience overloads for `search`, `create`, `upsert`, `delete`, `patch`, and `executeBundle` that accept FHIR resource objects directly, removing the need to manually construct request wrapper types. All FHIR operations also now support passing additional query parameters through `RequestOptions`.
* The FHIR client now supports convenience method overloads for `search`, `create`, `upsert`, `delete`, `patch`, and `executeBundle` that accept the resource body directly (without needing to wrap it in a request object), optionally paired with `RequestOptions`. Query parameters set on `RequestOptions` are now correctly forwarded to all FHIR HTTP requests. The request builder for `FhirCreateRequest`, `FhirDeleteRequest`, and `FhirExecuteBundleRequest` also exposes new `additionalProperty` and `additionalProperties` builder methods.
* The `FhirBundle.EntryItem` inner class (along with its nested `Request`, `Response`, and `Method` types) has been removed and replaced by the top-level `FhirBundleEntryItem` class. Similarly, `FhirPatchRequestBodyItem.Op` has been removed and replaced by the top-level `FhirPatchRequestBodyItemOp` class. Callers must update all references to these types accordingly. Additionally, builder methods `additionalProperty` and `additionalProperties` are now available on `FhirPatchRequest`, `FhirSearchRequest`, `FhirUpsertRequest`, `FhirBundle`, and `FhirPatchRequestBodyItem` builders for convenient population of extra fields.
* Several nested inner classes have been promoted to top-level types, requiring callers to update their import and type references:
* `FhirResource.Meta` → `FhirResourceMeta`
* `FhirProviderListResponse.FhirProvidersItem` → `FhirProviderListResponseFhirProvidersItem`
* `FhirProviderResponse.Data` → `FhirProviderResponseData`
* `FhirQueryResponse.Data` → `FhirQueryResponseData`
* `CreateRequest.Resource` → `CreateRequestResource`
* Update any direct usage of these inner class names to reference the new top-level equivalents. All builder and getter signatures have been updated accordingly.
* Several nested types in the `lang2fhir` response classes have been promoted to top-level classes and renamed to avoid ambiguity. `CreateMultiResponse.Bundle` is now `CreateMultiResponseBundle`, `CreateMultiResponse.ResourcesItem` is now `CreateMultiResponseResourcesItem`, and `SearchResponse.ResourceType` is now `SearchResponseResourceType`. Callers must update all import statements and type references that previously used the old nested class names.
* The `CreateSummaryRequest.Mode` inner class has been replaced by the top-level `CreateSummaryRequestMode` type, `CreateSummaryRequest.FhirResources` has been replaced by `CreateSummaryRequestFhirResources`, and `FhirBundle.EntryItem` has been replaced by the top-level `FhirBundleEntryItem` type. Callers must update their imports and type references accordingly. Additionally, the `X-Phenoml-On-Behalf-Of` and `X-Phenoml-Fhir-Provider` fields on `CohortRequest` and `Lang2FhirAndCreateMultiRequest` are no longer serialized into the request body (they are treated as HTTP headers only).
* The inner `Resource` type previously nested inside `Lang2FhirAndCreateRequest` has been moved to a new top-level class `Lang2FhirAndCreateRequestResource`. Similarly, `ResponseBundle` and `ResourceInfoItem` nested inside `Lang2FhirAndCreateMultiResponse` have been moved to `Lang2FhirAndCreateMultiResponseResponseBundle` and `Lang2FhirAndCreateMultiResponseResourceInfoItem` respectively. Consumers referencing these types must update their imports and type references to the new top-level class names.
* The `data` field on `McpServerResponse` and `McpServerToolResponse` has changed type from the removed inner classes `McpServerResponse.Data` and `McpServerToolResponse.Data` to new top-level types `McpServerResponseData` and `McpServerToolResponseData` respectively. Callers referencing `McpServerResponse.Data` or `McpServerToolResponse.Data` must update their code to use the new top-level types.
* The `fhirProviderId` field type on `CreateWorkflowRequest` and `UpdateWorkflowRequest` has changed from the inner class `CreateWorkflowRequest.FhirProviderId` / `UpdateWorkflowRequest.FhirProviderId` to new top-level types `CreateWorkflowRequestFhirProviderId` and `UpdateWorkflowRequestFhirProviderId`. Callers that reference the old inner `FhirProviderId` types or their static factory methods (`FhirProviderId.of(...)`) must migrate to the new top-level equivalents. Additionally, new convenience overloads `list(RequestOptions)` and `get(String, RequestOptions)` are now available on the workflows client.
* Several nested types within workflow response classes have been promoted to standalone top-level classes with new names. Callers must update references as follows: `ExecuteWorkflowResponse.Results` → `ExecuteWorkflowResponseResults`, `WorkflowResponse.Graph` → `WorkflowResponseGraph`, `WorkflowStep.Type` → `WorkflowStepType`, and `WorkflowStepSummary.Type` → `WorkflowStepSummaryType`. All public method signatures on the affected builders and getters have been updated to reflect these new types.
* The SDK now includes a configurable logging framework. Use `LogConfig` and `LogLevel` to enable HTTP request/response logging with automatic redaction of sensitive headers, or supply a custom `ILogger` implementation. Additionally, double values that are whole numbers are now serialized without a decimal point, RFC 2822 date-time strings are supported for deserialization, and a new `SseEvent<T>` class is available for working with Server-Sent Events.
* The SDK now supports agent chat streaming via a new `SseEventParser` utility and a new `AgentChatStreamEventType` enum (`MESSAGE_START`, `CONTENT_DELTA`, `MESSAGE_END`, `ERROR`, `TOOL_USE`, `TOOL_RESULT`). New union types `AgentCreateRequestProvider` and `AgentTemplateProvider` (accepting either a `String` or `List<String>`) and a new `ChatMessageTemplateRole` enum are also available. Additionally, `AgentClient`, `AsyncAgentClient`, `PromptsClient`, and `AsyncPromptsClient` now accept an optional `RequestOptions` parameter on their `list` and `update` methods, and all major builder types expose new `additionalProperty` / `additionalProperties` helpers for passing through extra fields.
* New `RequestOptions`-accepting overloads are now available on `AuthClient`, `AsyncAuthClient`, `ConstrueClient`, and `AsyncConstrueClient`, allowing per-call configuration of timeouts, headers, and other options. Two new enum types have been added: `JsonPatchOperationOp` (for JSON Patch operations) and `OAuthErrorError` (for OAuth 2.0 error codes). All builder classes now support `additionalProperty(String, Object)` and `additionalProperties(Map<String, Object>)` convenience methods for attaching arbitrary extra fields to requests and responses.
* The SDK introduces five new type classes — `ExtractRequestConfigChunkingMethod`, `ExtractRequestConfigConsistencyEffort`, `ExtractRequestConfigValidationMethod`, `GetCodeSystemDetailResponseStatus`, and `UploadRequestFormat` — expanding the set of strongly-typed values available when configuring extraction requests, upload operations, and code system queries. Builder classes for `DeleteCodeSystemResponse`, `ExtractedCodeResult`, `FeedbackResponse`, `GetCodeResponse`, `SemanticSearchResult`, `TextSearchResult`, and `ExtractRequestSystem` now expose `additionalProperty` and `additionalProperties` helper methods for attaching arbitrary metadata. `AsyncFhirClient` also gains `RequestOptions`-aware overloads for all FHIR operations (`search`, `create`, `upsert`, `delete`, `patch`, `executeBundle`), providing finer-grained control over per-request configuration.
* The `FhirClient` now supports `RequestOptions` overloads for all FHIR operations (`search`, `create`, `upsert`, `delete`, `patch`, and `executeBundle`), enabling per-request timeout and header customization. New types — `FhirBundleEntryItem`, `FhirBundleEntryItemRequest`, `FhirBundleEntryItemRequestMethod`, `FhirBundleEntryItemResponse`, `FhirPatchRequestBodyItemOp`, and `FhirResourceMeta` — are now available to model FHIR bundle entries and JSON Patch operations. Builder interfaces for `ErrorResponse` and `FhirProviderCreateRequest` also gain `additionalProperty` and `additionalProperties` methods for flexible extensibility.
* The SDK introduces three new union types for FHIR provider responses: `FhirProviderListResponseFhirProvidersItem` (wrapping `FhirProviderTemplate` or `FhirProviderSandboxInfo`), `FhirProviderResponseData` (same variants), and `FhirQueryResponseData` (wrapping `Map<String, Object>` or `String`). All FHIR provider builder classes now expose `additionalProperty(String, Object)` and `additionalProperties(Map<String, Object>)` convenience methods for passing arbitrary extra fields.
* The SDK now includes a `CreateRequestResource` enum with 17 FHIR resource types (including `AUTO`, `MEDICATIONREQUEST`, `ENCOUNTER`, `PATIENT`, `PROCEDURE`, and more) for use with multi-document conversion requests. New response types — `CreateMultiResponseBundle`, `CreateMultiResponseBundleEntryItem`, `CreateMultiResponseBundleEntryItemRequest`, and `CreateMultiResponseResourcesItem` — provide structured access to multi-document FHIR bundle results. Request builders across the `lang2fhir` module now support `additionalProperty` and `additionalProperties` methods for passing custom fields.
* The SDK now includes new types supporting expanded summary and FHIR resource capabilities. A `CreateSummaryRequestMode` enum (`FLATTEN`, `NARRATIVE`, `IPS`) is available to control summary output format, and `CreateSummaryRequestFhirResources` accepts either a `FhirResource` or `FhirBundle` as input. A new `SearchResponseResourceType` enum covers 30 FHIR resource types for lang2fhir search responses, and `Lang2FhirAndCreateMultiResponseResourceInfoItem` surfaces per-resource metadata (temp ID, resource type, and description) from multi-resource creation calls. Builder classes across summary and tools types now expose `additionalProperty` and `additionalProperties` helpers for setting arbitrary extra fields.
* The SDK now includes new types for FHIR multi-resource creation and MCP server integration. A new `Lang2FhirAndCreateRequestResource` enum covers 17 FHIR resource types (including `AUTO`, `PATIENT`, `ENCOUNTER`, and more), while `Lang2FhirAndCreateMultiResponseResponseBundle`, `McpServerResponseData`, and `McpServerToolResponseData` are available as structured response types. Additionally, `WorkflowsClient` and `AsyncWorkflowsClient` now expose `list(RequestOptions)` and `get(String, RequestOptions)` overloads for per-call request configuration.
* The SDK now includes several new workflow types and builder improvements. Two new union types, `CreateWorkflowRequestFhirProviderId` and `UpdateWorkflowRequestFhirProviderId`, allow the FHIR provider ID to be specified as either a single string or a list of strings. New types `ExecuteWorkflowResponseResults` and `WorkflowResponseGraph` expose per-step execution results and a simplified workflow graph view, respectively. New `WorkflowStepType` and `WorkflowStepSummaryType` enums enumerate supported step kinds. All workflow builder classes now offer `additionalProperty` and `additionalProperties` convenience methods for passing through arbitrary extra fields.

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

