## 2.0.0 - 2026-01-21
* refactor: remove isActive field from prompt and agent models
* Removes the deprecated isActive field from prompt and agent models across the codebase, simplifying the API surface and removing unused functionality.
* Key changes:
* Remove isActive field from AgentPromptsCreateRequest and AgentPromptsUpdateRequest
* Remove isActive field from AgentTemplate and PromptTemplate models
* Update delete operation documentation from "soft delete" to "delete"
* Remove isActive parameter from builder patterns and constructors
* Update equals/hashCode methods to exclude isActive field
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

