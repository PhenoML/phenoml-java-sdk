## 11.0.0 - 2026-04-03
* The `PhenomlClient` and `AsyncPhenomlClient` builders have been redesigned with explicit, type-safe authentication patterns. The `clientId()` and `clientSecret()` builder methods have been removed; callers must now use `PhenomlClient.withToken(token)` for pre-generated access tokens or `PhenomlClient.withCredentials(clientId, clientSecret)` for OAuth client credentials. Additionally, the SDK now supports configurable request/response logging via a new `logging(LogConfig)` builder method, per-instance base URLs via `instanceUrl(String)`, and per-request query parameter injection via `RequestOptions.Builder.addQueryParameter()`.
* The SDK now handles standard HTTP rate-limiting headers (`Retry-After` and `X-RateLimit-Reset`) when determining retry delay, with a fallback to jittered exponential backoff capped at 60 seconds. Additionally, SSE streaming now supports event-level discriminated unions via the new `Stream.fromSseWithEventDiscrimination()` factory methods, enabling correct deserialization of SSE payloads where the type discriminator is carried in the SSE `event:` field.
* A new `list(RequestOptions)` overload is now available on the agent client, allowing callers to pass request options when listing agents. All agent endpoints now support forwarding custom query parameters supplied via `RequestOptions`. Streaming chat requests no longer time out prematurely thanks to an updated infinite call timeout.
* The `RawAgentClient` now supports a new `list(RequestOptions)` convenience overload for retrieving PhenoAgents with custom request options. All agent endpoints now forward query parameters supplied via `RequestOptions` to the server, enabling more flexible request customization. Additionally, streaming chat requests no longer time out prematurely thanks to an infinite call timeout for long-lived connections.
* The `AsyncRawPromptsClient` now supports passing query parameters via `RequestOptions` across all endpoints (create, list, get, update, delete, patch, and loadDefaults). A new `update(String id, RequestOptions)` convenience overload is also available, making it easier to call the update endpoint with default request settings.
* The `AgentChatStreamEvent.getType()` method now returns `Optional<AgentChatStreamEventType>` instead of the previously nested `Optional<AgentChatStreamEvent.Type>`. The inner `AgentChatStreamEvent.Type` class—along with its `Value` enum and `Visitor` interface—has been removed. Callers that reference `AgentChatStreamEvent.Type`, `AgentChatStreamEvent.Type.Value`, or `AgentChatStreamEvent.Type.Visitor` must be updated to use the new top-level `AgentChatStreamEventType` class.
* Additionally, `getPhenomlOnBehalfOf()` and `getPhenomlFhirProvider()` on `AgentChatRequest` and `AgentStreamChatRequest` are now excluded from JSON serialization (annotated `@JsonIgnore`); these fields are transmitted as HTTP headers rather than body fields. Query-parameter fields on `AgentGetChatMessagesRequest` (`chatSessionId`, `numMessages`, `role`, `order`) and `AgentListRequest` (`tags`) are also now `@JsonIgnore`-annotated to prevent accidental body serialization.
* Several nested inner types have been renamed and promoted to top-level classes. Callers referencing `AgentCreateRequest.Provider`, `AgentTemplate.Provider`, `ChatMessageTemplate.Role`, or `JsonPatchOperation.Op` must update their imports and type references to `AgentCreateRequestProvider`, `AgentTemplateProvider`, `ChatMessageTemplateRole`, and `JsonPatchOperationOp` respectively. A new `getToken(RequestOptions)` overload is also available on the async auth client.
* The `OAuthError.getError()` method now returns `OAuthErrorError` instead of the previously nested `OAuthError.Error` type. Callers referencing `OAuthError.Error`, its `Value` enum, or its `Visitor` interface must migrate to the equivalent members on `OAuthErrorError`. Additionally, a new `getToken(RequestOptions)` overload is available on `RawAuthClient` for convenience, and both auth endpoints now support forwarding custom query parameters via `RequestOptions`.
* Several Construe client methods now have additional convenience overloads that accept only the required path parameters and `RequestOptions`, without needing to construct a full request object. All endpoints also now support forwarding custom query parameters through `RequestOptions`.
* Several new convenience overloads are now available on the `RawConstrueClient`: `getCodeSystemDetail`, `deleteCustomCodeSystem`, `exportCustomCodeSystem`, `listCodesInACodeSystem`, and `getASpecificCode` can all be called with only a `codesystem` (and optional `codeId`) plus `RequestOptions`, without constructing a separate request object. Additionally, custom query parameters supplied via `RequestOptions` are now correctly forwarded to every API request.
* Several nested enum types have been promoted to top-level classes. `ExtractRequestConfig.ChunkingMethod`, `ExtractRequestConfig.ValidationMethod`, and `ExtractRequestConfig.ConsistencyEffort` are now `ExtractRequestConfigChunkingMethod`, `ExtractRequestConfigValidationMethod`, and `ExtractRequestConfigConsistencyEffort` respectively. `UploadRequest.Format` is now `UploadRequestFormat`. Callers must update all import statements and type references to use the new top-level class names.
* The `GetCodeSystemDetailResponse.Status` inner class has been replaced by a new top-level `GetCodeSystemDetailResponseStatus` type. Callers that reference `GetCodeSystemDetailResponse.Status` (or its nested `Value` enum and `Visitor` interface) must update their imports and type references to use `GetCodeSystemDetailResponseStatus` instead. All construe response builders (`GetCodeSystemDetailResponse`, `ListCodeSystemsResponse`, `ListCodesResponse`, `SemanticSearchResponse`, `TextSearchResponse`) now also expose `additionalProperty` and `additionalProperties` builder methods for attaching arbitrary metadata.
* The FHIR client methods (`search`, `create`, `upsert`, `delete`, `patch`, and `executeBundle`) now offer additional convenience overloads that accept resource body arguments directly, without requiring callers to construct request wrapper objects. All FHIR operations also now support supplying `RequestOptions` with per-request query parameters that are forwarded to the FHIR server.
* The `RawFhirClient` now supports convenient shorthand overloads for `search`, `create`, `upsert`, `delete`, `patch`, and `executeBundle` that accept the body type directly (e.g. `FhirResource`, `FhirBundle`) without needing to construct a full request wrapper. All FHIR operations now also accept an optional `RequestOptions` argument, and any query parameters set in `RequestOptions` are forwarded to the upstream FHIR server.
* The `FhirBundle.EntryItem` nested class (along with its inner `Request`, `Response`, and `Method` types) has been removed and replaced by the new top-level `FhirBundleEntryItem` class. Similarly, `FhirPatchRequestBodyItem.Op` has been extracted into a new top-level `FhirPatchRequestBodyItemOp` class. Consumers must update all references to these types. Additionally, header fields on FHIR request builders (`X-Phenoml-On-Behalf-Of`, `X-Phenoml-Fhir-Provider`, `query_parameters`) are now correctly excluded from JSON body serialization via `@JsonIgnore`.
* Several nested static inner classes have been extracted into dedicated top-level types. The following inner classes have been removed and must be replaced in existing code:
* `FhirResource.Meta` → `FhirResourceMeta`
* `FhirProviderListResponse.FhirProvidersItem` → `FhirProviderListResponseFhirProvidersItem`
* `FhirProviderResponse.Data` → `FhirProviderResponseData`
* `FhirQueryResponse.Data` → `FhirQueryResponseData`
* `CreateRequest.Resource` (including its `Value` enum and `Visitor` interface) → `CreateRequestResource`
* Update any imports and references to these types accordingly. Builder classes for the affected response types also now expose `additionalProperty(String, Object)` and `additionalProperties(Map)` convenience methods.
* The `CreateMultiResponse` and `SearchResponse` types have been updated to use new top-level classes in place of their previously nested inner types. `CreateMultiResponse.Bundle` is now `CreateMultiResponseBundle`, `CreateMultiResponse.ResourcesItem` is now `CreateMultiResponseResourcesItem`, and `SearchResponse.ResourceType` is now `SearchResponseResourceType`. Consumers must update all references to these types and their associated builder methods accordingly.
* The `CreateSummaryRequest.Mode` and `CreateSummaryRequest.FhirResources` inner classes have been removed and replaced by the top-level `CreateSummaryRequestMode` and `CreateSummaryRequestFhirResources` types. Similarly, `FhirBundle.EntryItem` has been replaced by the standalone `FhirBundleEntryItem` type. Callers referencing these inner classes must update their imports and type references. Additionally, builder classes across several request types now expose `additionalProperty` and `additionalProperties` methods for setting custom properties.
* Several nested static types have been extracted into top-level classes with updated names. `Lang2FhirAndCreateRequest.Resource` is now `Lang2FhirAndCreateRequestResource`, and `Lang2FhirAndCreateMultiResponse.ResponseBundle` / `ResourceInfoItem` are now `Lang2FhirAndCreateMultiResponseResponseBundle` / `Lang2FhirAndCreateMultiResponseResourceInfoItem`. Callers referencing the old inner class types must update their imports and type references to use the new top-level classes from `com.phenoml.api.resources.tools.types`.
* The `getData()` method on `McpServerResponse` and `McpServerToolResponse` now returns the new top-level types `McpServerResponseData` and `McpServerToolResponseData` respectively, replacing the former nested `McpServerResponse.Data` and `McpServerToolResponse.Data` inner classes. Callers referencing these inner types must update their imports and type references to the new standalone classes.
* The `fhirProviderId` field type on `CreateWorkflowRequest` and `UpdateWorkflowRequest` has changed from the inner class `CreateWorkflowRequest.FhirProviderId` / `UpdateWorkflowRequest.FhirProviderId` to new top-level types `CreateWorkflowRequestFhirProviderId` and `UpdateWorkflowRequestFhirProviderId` respectively. Callers that reference the old inner classes (e.g. `CreateWorkflowRequest.FhirProviderId.of(...)`) must update their imports and usages to the new top-level equivalents. Additionally, the builder interface `_FinalStage` for both request types now accepts the new top-level type in its `fhirProviderId(...)` method.
* Several nested inner types within workflow response classes have been promoted to top-level classes. Callers referencing `ExecuteWorkflowResponse.Results`, `WorkflowResponse.Graph`, `WorkflowStep.Type`, or `WorkflowStepSummary.Type` must update their imports and type references to the new top-level equivalents: `ExecuteWorkflowResponseResults`, `WorkflowResponseGraph`, `WorkflowStepType`, and `WorkflowStepSummaryType` respectively.
* The SDK now includes a configurable logging framework. Use `LogConfig` with `ILogger`, `LogLevel`, and the built-in `ConsoleLogger` to enable and customize SDK-level HTTP request/response logging — logging is silent by default so existing behavior is unchanged. Additionally, query parameters supplied via `RequestOptions` are now correctly forwarded on all MCP server tool requests, and a new `SseEvent<T>` class is available in the core package for Server-Sent Events handling.
* The SDK now supports Server-Sent Events (SSE) streaming for agent chat, introducing `AgentChatStreamEventType` (with values `MESSAGE_START`, `CONTENT_DELTA`, `MESSAGE_END`, `ERROR`, `TOOL_USE`, `TOOL_RESULT`) and a new `SseEventParser` utility for discriminated-union event parsing. New types `AgentCreateRequestProvider`, `AgentTemplateProvider`, and `ChatMessageTemplateRole` are also available. Additionally, all major request and response builder classes now expose `additionalProperty` and `additionalProperties` methods, and `AgentClient.list()` and `PromptsClient.update()` now accept an optional `RequestOptions` parameter.
* Several clients now support an optional `RequestOptions` parameter on additional methods. `AuthClient` and `AsyncAuthClient` gain a `getToken(RequestOptions)` overload, while `ConstrueClient` and `AsyncConstrueClient` gain `RequestOptions` overloads for `getCodeSystemDetail`, `deleteCustomCodeSystem`, `exportCustomCodeSystem`, `listCodesInACodeSystem`, and `getASpecificCode`. Two new enum types are available: `JsonPatchOperationOp` (for JSON Patch operations) and `OAuthErrorError` (for OAuth 2.0 error codes). All builder types now expose `additionalProperty` and `additionalProperties` helper methods for setting arbitrary extra fields.
* The SDK introduces five new enum types — `ExtractRequestConfigChunkingMethod`, `ExtractRequestConfigConsistencyEffort`, `ExtractRequestConfigValidationMethod`, `GetCodeSystemDetailResponseStatus`, and `UploadRequestFormat` — for strongly-typed handling of previously untyped string fields. Builder classes for `DeleteCodeSystemResponse`, `ExtractedCodeResult`, `ExtractRequestSystem`, `FeedbackResponse`, `GetCodeResponse`, `SemanticSearchResult`, and `TextSearchResult` now include `additionalProperty` and `additionalProperties` convenience methods. `AsyncFhirClient` gains `RequestOptions`-aware overloads for `search`, `create`, `upsert`, `delete`, `patch`, and `executeBundle`, as well as new no-options variants for `create`, `upsert`, `patch`, and `executeBundle`.
* The `FhirClient` now exposes additional overloaded methods — `search`, `create`, `upsert`, `delete`, `patch`, and `executeBundle` — each accepting an optional `RequestOptions` parameter for per-request configuration. Six new supporting types (`FhirBundleEntryItem`, `FhirBundleEntryItemRequest`, `FhirBundleEntryItemResponse`, `FhirBundleEntryItemRequestMethod`, `FhirPatchRequestBodyItemOp`, and `FhirResourceMeta`) are now available for working with FHIR bundle transactions and JSON Patch operations. Builder interfaces for `ErrorResponse` and `FhirProviderCreateRequest` also gain `additionalProperty` and `additionalProperties` helper methods.
* Builder classes for FHIR provider request and type objects now expose `additionalProperty(String, Object)` and `additionalProperties(Map)` methods, making it straightforward to attach custom metadata when constructing these objects. Three new types — `FhirProviderListResponseFhirProvidersItem`, `FhirProviderResponseData`, and `FhirQueryResponseData` — are available to represent richer FHIR list and query response payloads.
* The SDK now includes new response types for multi-document FHIR conversions: `CreateMultiResponseBundle`, `CreateMultiResponseBundleEntryItem`, `CreateMultiResponseBundleEntryItemRequest`, and `CreateMultiResponseResourcesItem`. A new `CreateRequestResource` enum is also available, providing 17 FHIR resource type constants (such as `MEDICATIONREQUEST`, `ENCOUNTER`, `PATIENT`, and `AUTO`). Additionally, all request builder classes now support `additionalProperty` and `additionalProperties` methods for passing arbitrary JSON fields through to the API.
* The SDK now includes new types to support expanded FHIR summarization and resource search capabilities. A new `SearchResponseResourceType` enum covers 30 FHIR resource types, `CreateSummaryRequestMode` offers `FLATTEN`, `NARRATIVE`, and `IPS` summary modes, and `CreateSummaryRequestFhirResources` accepts either a `FhirResource` or `FhirBundle`. Additionally, all builder classes now expose `additionalProperty` and `additionalProperties` helper methods for more convenient object construction.
* The SDK now includes several new types to support FHIR multi-resource workflows and MCP server integration: `Lang2FhirAndCreateMultiResponseResponseBundle`, `Lang2FhirAndCreateRequestResource` (a typed enum of 17 FHIR resource categories), `McpServerResponseData`, and `McpServerToolResponseData`. Additionally, `WorkflowsClient` and `AsyncWorkflowsClient` now expose `list(RequestOptions)` and `get(String, RequestOptions)` overloads, enabling per-call request customization such as custom timeouts and headers.
* Several new workflow types are now available. `CreateWorkflowRequestFhirProviderId` and `UpdateWorkflowRequestFhirProviderId` support specifying FHIR provider IDs as either a single string or a list of strings. `ExecuteWorkflowResponseResults` exposes per-step execution results, and `WorkflowResponseGraph` provides a simplified step-level view of a workflow. New `WorkflowStepType` and `WorkflowStepSummaryType` enums (`SEARCH`, `CREATE`, `WORKFLOW`, `DECISION_NODE`) are also available. All workflow builder classes now include `additionalProperty` and `additionalProperties` convenience methods.

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

