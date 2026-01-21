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

