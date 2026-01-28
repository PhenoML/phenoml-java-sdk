## 3.0.1 - 2026-01-28
* refactor: rename method and update API documentation
* Rename textSearchKeywordBased to terminologyServerTextSearch to better reflect its functionality. Update Javadoc comments across all Construe API methods to provide clearer descriptions of functionality and terminology server interactions. Add AMA requirement notices for CPT usage and paid plan requirements for custom code system uploads.
* Key changes:
* Rename textSearchKeywordBased method to terminologyServerTextSearch across all client classes
* Improve method descriptions to explicitly mention terminology server interactions
* Add AMA requirements notice for CPT usage on relevant endpoints
* Update custom code system documentation to mention paid plan requirement
* Add new BadGatewayError handling for FHIR endpoints with 502 status code support
* Enhance API documentation clarity across multiple construe methods
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

