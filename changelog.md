## 12.3.0 - 2026-04-24
### Added
* **`ExtractRequestConfigValidationMethod.CHUNK_CODE_JACCARD_SIMILARITY`** — new validation method that filters extracted codes using token-level Jaccard similarity between the source text chunk and the code description.
* **`ExtractRequestConfig.chunkCodeJaccardSimilarityFilteringThreshold`** — new optional `Float` field (and builder setter) on `ExtractRequestConfig` that sets the minimum Jaccard similarity score (0.0–1.0) required for a code to pass validation when using the `chunk_code_jaccard_similarity` method; ignored by all other validation methods.
* **`ExtractRequestConfigChunkingMethod.CLINICAL_NER_EXTRACT`** — new chunking method that extracts clinical concepts (problems, tests, treatments) and uses each as an individual chunk; supports source-text citations alongside the existing `sentences` method.

## 12.2.0 - 2026-04-14
* Three new optional fields expand custom Implementation Guide (IG) support across the SDK. `CreateMultiRequest` and `DocumentMultiRequest` now accept an `implementationGuide` field to include profiles from a named custom IG alongside US Core profiles during resource detection. `ProfileUploadRequest` gains an `implementationGuide` field (to group uploaded profiles under a named IG) and a `profileContext` field (natural language context that guides LLM profile selection, stored as IG-level metadata). All new fields are optional and existing call sites require no changes.

## 12.1.0 - 2026-04-13
* Two new optional `detectionEffort` fields are now available on `CreateMultiRequest` and `DocumentMultiRequest`. Set it to `CreateMultiRequestDetectionEffort.STANDARD` for a single detection pass or `CreateMultiRequestDetectionEffort.DEEP` for multiple passes with higher recall. The field defaults to omitted (server default applies) when not specified.

## 12.0.1 - 2026-04-13
* docs: expand Javadoc for `consistencyEffort` in `ExtractRequestConfig`
* Improve the documentation for the `consistencyEffort` field and its
* builder setter in `ExtractRequestConfig` to more accurately describe
* how consistency effort interacts with `validation_method` and
* `min_context_relevance`.
* Key changes:
* Clarify that "borderline codes" are more broadly "borderline results"
* Add documentation explaining consistency is applied to the validation
* step when `validation_method` is not "none" (unanimous validation
* across rounds required)
* Add documentation explaining consistency is applied to the relevance
* ranking step when `validation_method` is "none" and
* `min_context_relevance` is above 0
* 🌿 Generated with Fern

## 12.0.0 - 2026-04-03
* The `generateToken` method has been removed from `AuthClient`, `AsyncAuthClient`, `RawAuthClient`, and `AsyncRawAuthClient`, along with the associated `AuthGenerateTokenRequest` and `AuthGenerateTokenResponse` types. Callers must migrate to the OAuth 2.0 client credentials endpoint using `getToken(ClientCredentialsRequest)`, which provides the same token-acquisition functionality under RFC 6749 §4.4. The `BadRequestErrorBody` and `UnauthorizedErrorBody` types have also been removed.

## 11.0.0 - 2026-04-03
* The client builder API has been redesigned with explicit, type-safe authentication strategies. The `clientId()` and `clientSecret()` setter methods on `PhenomlClientBuilder` and `AsyncPhenomlClientBuilder` have been removed; callers must now use `PhenomlClient.withCredentials(clientId, clientSecret)` for OAuth client credentials auth or `PhenomlClient.withToken(token)` for pre-generated token auth. The `builder()` factory method now returns a `PhenomlClientBuilder._Builder` instead of `PhenomlClientBuilder`, so any code storing the result in a typed variable must be updated. Additionally, SDK-level logging can now be configured via the new `logging(LogConfig)` builder method, and per-request query parameters can be injected through the new `RequestOptions.Builder.addQueryParameter()` methods.
* The SDK now supports smarter HTTP retry behavior: when a server responds with `Retry-After` or `X-RateLimit-Reset` headers, the retry interceptor honors those values (with appropriate jitter) instead of relying solely on exponential backoff. Additionally, SSE streaming now supports event-level discriminated unions via new `Stream.fromSseWithEventDiscrimination()` factory methods, enabling correct deserialization of SSE payloads where the `event:` field acts as a type discriminator.
* The `AsyncRawAgentClient` now supports a new `list(RequestOptions)` overload, and all agent endpoints forward custom query parameters supplied via `RequestOptions`. The `streamChat` endpoint no longer has a call timeout applied, preventing premature disconnects on long-running streams.
* The `RawAgentClient` now supports a new `list(RequestOptions)` overload for passing request options directly when listing PhenoAgents. All agent endpoints also forward any query parameters defined in `RequestOptions` to the outbound request, and streaming chat connections no longer time out prematurely thanks to a disabled call timeout.
* A new `update(String id, RequestOptions requestOptions)` overload is now available on `AsyncRawPromptsClient`, making it easier to update a prompt using only its ID with custom request options. All agent prompt endpoints now also support forwarding query parameters supplied via `RequestOptions` to the underlying HTTP request.
* The `AgentChatStreamEvent.Type` inner class has been removed and replaced with a top-level `AgentChatStreamEventType` type. Callers referencing `AgentChatStreamEvent.Type`, `AgentChatStreamEvent.Type.Value`, or `AgentChatStreamEvent.Type.Visitor` must update their imports and usages to use `AgentChatStreamEventType` instead.
* Additionally, the `phenomlOnBehalfOf` and `phenomlFhirProvider` fields on `AgentChatRequest` and `AgentStreamChatRequest`, and the `chatSessionId`, `numMessages`, `role`, and `order` fields on `AgentGetChatMessagesRequest`, are no longer serialized as JSON body properties — they are now excluded from serialization (`@JsonIgnore`). If your application relied on these fields being present in the serialized JSON, you will need to pass them through the appropriate header or query parameter mechanism instead.
* Several nested provider and role types have been promoted to top-level classes. Callers referencing `AgentCreateRequest.Provider`, `AgentTemplate.Provider`, `ChatMessageTemplate.Role`, or `JsonPatchOperation.Op` must update those references to `AgentCreateRequestProvider`, `AgentTemplateProvider`, `ChatMessageTemplateRole`, and `JsonPatchOperationOp` respectively. All method signatures that accepted or returned these types have been updated accordingly.
* The `OAuthError` type has been updated: the nested `OAuthError.Error` class (along with its `Value` enum and `Visitor` interface) has been removed and replaced with the standalone `OAuthErrorError` type. Callers that reference `OAuthError.Error`, `OAuthError.Error.Value`, or `OAuthError.Error.Visitor` must update to use `OAuthErrorError` instead. Additionally, `RawAuthClient` now supports a new `getToken(RequestOptions)` convenience overload and forwards `RequestOptions` query parameters to all auth token endpoints.
* Several new convenience overloads are now available on the `AsyncRawConstrueClient` for `getCodeSystemDetail`, `deleteCustomCodeSystem`, `exportCustomCodeSystem`, `listCodesInACodeSystem`, and `getASpecificCode`, allowing callers to pass only a `RequestOptions` argument without constructing a separate request object. All endpoints now also support forwarding custom query parameters supplied via `RequestOptions`.
* Several `RawConstrueClient` methods now have new overloads that accept a `RequestOptions` argument directly — including `getCodeSystemDetail`, `deleteCustomCodeSystem`, `exportCustomCodeSystem`, `listCodesInACodeSystem`, and `getASpecificCode` — making it easier to pass custom query parameters without constructing a full request object. All endpoints now also forward any query parameters supplied via `RequestOptions` to the outgoing HTTP request.
* Several nested enum types have been moved to top-level classes as part of an SDK refactor. Callers must update references to `ExtractRequestConfig.ChunkingMethod`, `ExtractRequestConfig.ValidationMethod`, `ExtractRequestConfig.ConsistencyEffort`, and `UploadRequest.Format` to use the new top-level equivalents: `ExtractRequestConfigChunkingMethod`, `ExtractRequestConfigValidationMethod`, `ExtractRequestConfigConsistencyEffort`, and `UploadRequestFormat` respectively. All builder types across request and response objects now also expose `additionalProperty` and `additionalProperties` helper methods for passing extra fields.
* The `GetCodeSystemDetailResponse.Status` nested class has been replaced by a new top-level `GetCodeSystemDetailResponseStatus` type. Callers referencing `GetCodeSystemDetailResponse.Status`, its `Value` enum, or its `Visitor` interface must update their code to use `GetCodeSystemDetailResponseStatus` instead. Additionally, all Construe response builder types now expose `additionalProperty` and `additionalProperties` convenience methods for attaching arbitrary metadata.
* The FHIR client methods (`search`, `create`, `upsert`, `delete`, `patch`, `executeBundle`) now offer additional convenience overloads that accept typed body objects directly, eliminating the need to construct a request wrapper manually. All FHIR operations also now forward any query parameters supplied via `RequestOptions` to the underlying FHIR server.
* The `RawFhirClient` now supports additional convenience overloads for all FHIR operations (`search`, `create`, `upsert`, `delete`, `patch`, `executeBundle`), allowing callers to pass raw body objects and `RequestOptions` directly without constructing intermediate request wrapper objects. Query parameters supplied via `RequestOptions` are now forwarded to the FHIR server for all HTTP methods. Additionally, request builder classes (`FhirCreateRequest`, `FhirDeleteRequest`, `FhirExecuteBundleRequest`) now expose `additionalProperty` and `additionalProperties` builder methods.
* The `FhirBundle.getEntry()` method now returns `List<FhirBundleEntryItem>` instead of the former inner class `List<FhirBundle.EntryItem>`, and `FhirPatchRequestBodyItem.getOp()` now returns the top-level `FhirPatchRequestBodyItemOp` instead of the inner class `FhirPatchRequestBodyItem.Op`. Any code referencing `FhirBundle.EntryItem`, `FhirBundle.EntryItem.Request`, `FhirBundle.EntryItem.Response`, `FhirBundle.EntryItem.Request.Method`, or `FhirPatchRequestBodyItem.Op` must be updated to use the new standalone types. Additionally, the `FhirSearchRequest` query parameters and all authentication header fields across FHIR request types are now correctly excluded from JSON serialization.
* Several previously nested inner types have been promoted to standalone top-level classes. The following renames are **breaking** and require updating imports and type references:
* `FhirResource.Meta` is now `FhirResourceMeta`
* `FhirProviderListResponse.FhirProvidersItem` is now `FhirProviderListResponseFhirProvidersItem`
* `FhirProviderResponse.Data` is now `FhirProviderResponseData`
* `FhirQueryResponse.Data` is now `FhirQueryResponseData`
* `CreateRequest.Resource` is now `CreateRequestResource`
* All public methods that previously accepted or returned these inner types (e.g. `getMeta()`, `getData()`, `getFhirProviders()`, `getResource()`, and their corresponding builder methods) now use the new top-level type names. Update all call sites and imports accordingly. Builders also gain new `additionalProperty` and `additionalProperties` convenience methods.
* The nested types `CreateMultiResponse.Bundle`, `CreateMultiResponse.ResourcesItem`, and `SearchResponse.ResourceType` have been removed and replaced with top-level classes `CreateMultiResponseBundle`, `CreateMultiResponseResourcesItem`, and `SearchResponseResourceType` respectively. Callers that reference these types must update their imports and type declarations to use the new top-level class names.
* The SDK has refactored several inner types into top-level classes: `CreateSummaryRequest.Mode` is now `CreateSummaryRequestMode`, `CreateSummaryRequest.FhirResources` is now `CreateSummaryRequestFhirResources`, and `FhirBundle.EntryItem` is now `FhirBundleEntryItem`. Callers referencing these inner classes must update their imports and type references. Additionally, `CohortRequest` and `Lang2FhirAndCreateMultiRequest` now correctly treat `X-Phenoml-On-Behalf-Of` and `X-Phenoml-Fhir-Provider` as HTTP headers rather than body fields.
* The nested types `Lang2FhirAndCreateRequest.Resource`, `Lang2FhirAndCreateMultiResponse.ResponseBundle`, and `Lang2FhirAndCreateMultiResponse.ResourceInfoItem` have been removed and replaced with top-level classes `Lang2FhirAndCreateRequestResource`, `Lang2FhirAndCreateMultiResponseResponseBundle`, and `Lang2FhirAndCreateMultiResponseResourceInfoItem` respectively. Callers must update their import statements and type references to use the new top-level class names. Additionally, the `getResource()` method on `Lang2FhirAndCreateRequest` now returns `Lang2FhirAndCreateRequestResource` instead of the former inner `Resource` type.
* The `getData()` method on `McpServerResponse` now returns `Optional<McpServerResponseData>` instead of `Optional<McpServerResponse.Data>`, and on `McpServerToolResponse` it now returns `Optional<McpServerToolResponseData>` instead of `Optional<McpServerToolResponse.Data>`. Callers referencing the old inner `Data` types must update their imports and type references to the new top-level classes. Additionally, the async workflows client now correctly propagates `RequestOptions` query parameters to all workflow endpoints.
* The `CreateWorkflowRequest` and `UpdateWorkflowRequest` classes now use the new top-level `CreateWorkflowRequestFhirProviderId` and `UpdateWorkflowRequestFhirProviderId` types instead of the previous inner `FhirProviderId` class. Callers that referenced `CreateWorkflowRequest.FhirProviderId` or `UpdateWorkflowRequest.FhirProviderId` will need to update their imports to the new top-level types. Additionally, new `list(RequestOptions)` and `get(String, RequestOptions)` convenience overloads are available on the workflows client, and all request builders now support `additionalProperty` / `additionalProperties` methods.
* Several workflow-related nested types have been promoted to top-level classes. Callers referencing `WorkflowStep.Type`, `WorkflowStepSummary.Type`, `WorkflowResponse.Graph`, or `ExecuteWorkflowResponse.Results` must update their imports and references to the new names: `WorkflowStepType`, `WorkflowStepSummaryType`, `WorkflowResponseGraph`, and `ExecuteWorkflowResponseResults` respectively. The return types of `getType()` and `getGraph()`/`getResults()` builder methods have changed accordingly.
* The SDK now includes a configurable logging framework (`ILogger`, `LogConfig`, `LogLevel`, `Logger`, `ConsoleLogger`, `LoggingInterceptor`) that is silent by default. Consumers can opt in to HTTP request/response logging with automatic redaction of sensitive headers. Additionally, a new `SseEvent<T>` class is available for Server-Sent Event processing, RFC 2822 date deserialization is now supported, and integer-valued doubles are serialized without a trailing decimal point.
* The SDK now supports agent chat streaming with a new `AgentChatStreamEventType` enum and an internal `SseEventParser` utility for handling Server-Sent Events. New types `AgentCreateRequestProvider`, `AgentTemplateProvider`, and `ChatMessageTemplateRole` are available for agent configuration. Additionally, `list` and `update` methods on `AgentClient`, `AsyncAgentClient`, `PromptsClient`, and `AsyncPromptsClient` now accept an optional `RequestOptions` parameter, and all major builder types expose `additionalProperty`/`additionalProperties` methods for passing through arbitrary fields.
* Several clients now support an optional `RequestOptions` parameter on additional methods: `AuthClient.getToken`, and the `ConstrueClient` methods `getCodeSystemDetail`, `deleteCustomCodeSystem`, `exportCustomCodeSystem`, `listCodesInACodeSystem`, and `getASpecificCode`. Two new enum types are available — `JsonPatchOperationOp` for JSON Patch operations and `OAuthErrorError` for OAuth error classification. All request and response builder types now expose `additionalProperty` and `additionalProperties` convenience methods for attaching arbitrary metadata.
* Several new types and capabilities are now available in the SDK:
* New enum types `ExtractRequestConfigChunkingMethod`, `ExtractRequestConfigConsistencyEffort`, `ExtractRequestConfigValidationMethod`, `GetCodeSystemDetailResponseStatus`, and `UploadRequestFormat` have been added to support expanded API options.
* `AsyncFhirClient` methods (`search`, `create`, `upsert`, `delete`, `patch`, `executeBundle`) now have `RequestOptions`-accepting overloads for fine-grained request control.
* Builder classes for `DeleteCodeSystemResponse`, `ExtractedCodeResult`, `FeedbackResponse`, `GetCodeResponse`, `SemanticSearchResult`, `TextSearchResult`, and `ExtractRequestSystem` now support `additionalProperty` and `additionalProperties` methods for attaching arbitrary metadata.
* The `FhirClient` now exposes `RequestOptions`-accepting overloads for all FHIR operations: `search`, `create`, `upsert`, `delete`, `patch`, and `executeBundle`. Several new model types have also been introduced — `FhirBundleEntryItem`, `FhirBundleEntryItemRequest`, `FhirBundleEntryItemResponse`, `FhirBundleEntryItemRequestMethod`, `FhirPatchRequestBodyItemOp`, and `FhirResourceMeta` — to support richer FHIR bundle and JSON Patch workflows. The `ErrorResponse` and `FhirProviderCreateRequest` builders now support `additionalProperty` and `additionalProperties` methods for attaching arbitrary metadata.
* Three new FHIR provider union types are now available: `FhirProviderListResponseFhirProvidersItem`, `FhirProviderResponseData`, and `FhirQueryResponseData`. All FHIR provider type builders now expose `additionalProperty(String, Object)` and `additionalProperties(Map<String, Object>)` methods for passing through arbitrary extra fields. Deserialization of discriminated union auth types has also been made more robust.
* The SDK now includes a `CreateRequestResource` enum with 17 FHIR resource type constants (e.g. `MEDICATIONREQUEST`, `ENCOUNTER`, `AUTO`) for use in multi-document FHIR creation requests. Three new response types — `CreateMultiResponseBundle`, `CreateMultiResponseBundleEntryItem`, and `CreateMultiResponseBundleEntryItemRequest` — model the structured bundle output, and a new `CreateMultiResponseResourcesItem` type captures per-resource metadata including `tempId`, `resourceType`, and `description`. All request and response builders also gain optional `additionalProperty` / `additionalProperties` methods for passing arbitrary extra fields.
* The SDK now supports richer FHIR-based summary and search workflows. A new `CreateSummaryRequestMode` enum (`FLATTEN`, `NARRATIVE`, `IPS`) allows callers to control the output format of summary requests, and a new `CreateSummaryRequestFhirResources` union type accepts either a `FhirResource` or `FhirBundle` as input. A new `SearchResponseResourceType` enum covers 30 FHIR resource types for use in lang2fhir search responses, and `FhirBundleEntryItem` and `Lang2FhirAndCreateMultiResponseResourceInfoItem` types are now available for working with FHIR bundle entries and multi-resource creation metadata. Additionally, all major response and request builder classes now expose `additionalProperty` and `additionalProperties` helper methods for setting arbitrary extra fields.
* The SDK now includes support for MCP (Model Context Protocol) server integrations via two new types: `McpServerResponseData` and `McpServerToolResponseData`. A new `Lang2FhirAndCreateRequestResource` type provides a strongly-typed enum for 17 FHIR resource targets (e.g. `PATIENT`, `ENCOUNTER`, `MEDICATIONREQUEST`), and `Lang2FhirAndCreateMultiResponseResponseBundle` represents multi-resource FHIR bundle responses. Additionally, `WorkflowsClient` and `AsyncWorkflowsClient` now expose `RequestOptions`-accepting overloads for `list()` and `get()` to support per-request configuration.
* Several new workflow types are now available. `CreateWorkflowRequestFhirProviderId` and `UpdateWorkflowRequestFhirProviderId` union types allow specifying a single provider ID or a list of provider IDs when creating or updating workflows. `ExecuteWorkflowResponseResults` exposes per-step execution results keyed by step ID, `WorkflowResponseGraph` provides a simplified view of workflow steps, and the new `WorkflowStepType` and `WorkflowStepSummaryType` enums enumerate the supported step kinds (`SEARCH`, `CREATE`, `WORKFLOW`, `DECISION_NODE`). All workflow response builders now include `additionalProperty` and `additionalProperties` convenience methods for setting extra fields.

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

