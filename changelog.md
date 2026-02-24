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

