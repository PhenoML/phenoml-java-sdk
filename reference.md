# Reference
## Agent
<details><summary><code>client.agent.create(request) -> AgentResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Creates a new PhenoAgent with specified configuration
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().create(
    AgentCreateRequest
        .builder()
        .name("Medical Assistant")
        .provider(
            AgentCreateRequestProvider.of("7002b0b4-8d09-445a-bf65-0fafdaf26c35")
        )
        .description("An AI assistant for medical information processing")
        .prompts(
            Arrays.asList("prompt_123")
        )
        .tags(
            Optional.of(
                Arrays.asList("medical", "fhir")
            )
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**request:** `AgentCreateRequest` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.agent.list() -> ListResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves a list of PhenoAgents belonging to the authenticated user
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().list(
    ListRequest
        .builder()
        .tags("tags")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**tags:** `Optional<String>` — Filter by tags
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.agent.get(id) -> AgentResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves a specific agent by its ID
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().get("id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — Agent ID
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.agent.update(id, request) -> AgentResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Updates an existing agent's configuration
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().update(
    "id",
    AgentCreateRequest
        .builder()
        .name("Medical Assistant")
        .provider(
            AgentCreateRequestProvider.of("7002b0b4-8d09-445a-bf65-0fafdaf26c35")
        )
        .description("Updated description for the medical assistant")
        .prompts(
            Arrays.asList("prompt_123")
        )
        .tags(
            Optional.of(
                Arrays.asList("medical", "fhir", "updated")
            )
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — Agent ID
    
</dd>
</dl>

<dl>
<dd>

**request:** `AgentCreateRequest` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.agent.delete(id) -> DeleteResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes an existing agent
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().delete("id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — Agent ID
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.agent.patch(id, request) -> AgentResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Patches an existing agent's configuration
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().patch(
    "id",
    Arrays.asList(
        JsonPatchOperation
            .builder()
            .op(JsonPatchOperationOp.REPLACE)
            .path("/description")
            .value("patched description")
            .build(),
        JsonPatchOperation
            .builder()
            .op(JsonPatchOperationOp.ADD)
            .path("/tags/-")
            .value("updated")
            .build()
    )
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — Agent ID
    
</dd>
</dl>

<dl>
<dd>

**request:** `List<JsonPatchOperation>` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Chat
<details><summary><code>client.agent.chat.send(request) -> AgentChatResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Send a message to an agent and receive a JSON response.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().chat().send(
    AgentChatRequest
        .builder()
        .message("What is the patient's current condition?")
        .agentId("agent-123")
        .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
        .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
        .sessionId("session-abc123")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**phenomlOnBehalfOf:** `Optional<String>` 

Optional header for on-behalf-of authentication. Used when making requests on behalf of another user or entity.
Must be in the format: Patient/{uuid} or Practitioner/{uuid}
    
</dd>
</dl>

<dl>
<dd>

**phenomlFhirProvider:** `Optional<String>` 

Optional header for FHIR provider authentication. Contains credentials in the format {fhir_provider_id}:{oauth2_token}.
Multiple FHIR provider integrations can be provided as comma-separated values.
    
</dd>
</dl>

<dl>
<dd>

**message:** `String` — The message to send to the agent
    
</dd>
</dl>

<dl>
<dd>

**context:** `Optional<String>` — Optional context for the conversation
    
</dd>
</dl>

<dl>
<dd>

**sessionId:** `Optional<String>` — Optional session ID for conversation continuity. Only one request may be active for a session at a time; overlapping turns for the same session return 409 Conflict.
    
</dd>
</dl>

<dl>
<dd>

**agentId:** `String` — The ID of the agent to chat with
    
</dd>
</dl>

<dl>
<dd>

**enhancedReasoning:** `Optional<Boolean>` — Enable enhanced reasoning capabilities. Increases latency but improves response quality and reliability.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.agent.chat.stream(request) -> Iterable&amp;lt;AgentChatStreamEvent&amp;gt;</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Send a message to an agent and receive the response as a Server-Sent Events
(SSE) stream. Events include message_start, content_delta, tool_use,
tool_result, message_end, and error.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().chat().stream(
    AgentStreamChatRequest
        .builder()
        .message("What is the patient's current condition?")
        .agentId("agent-123")
        .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
        .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
        .sessionId("session-abc123")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**phenomlOnBehalfOf:** `Optional<String>` 

Optional header for on-behalf-of authentication. Used when making requests on behalf of another user or entity.
Must be in the format: Patient/{uuid} or Practitioner/{uuid}
    
</dd>
</dl>

<dl>
<dd>

**phenomlFhirProvider:** `Optional<String>` 

Optional header for FHIR provider authentication. Contains credentials in the format {fhir_provider_id}:{oauth2_token}.
Multiple FHIR provider integrations can be provided as comma-separated values.
    
</dd>
</dl>

<dl>
<dd>

**message:** `String` — The message to send to the agent
    
</dd>
</dl>

<dl>
<dd>

**context:** `Optional<String>` — Optional context for the conversation
    
</dd>
</dl>

<dl>
<dd>

**sessionId:** `Optional<String>` — Optional session ID for conversation continuity. Only one request may be active for a session at a time; overlapping turns for the same session return 409 Conflict.
    
</dd>
</dl>

<dl>
<dd>

**agentId:** `String` — The ID of the agent to chat with
    
</dd>
</dl>

<dl>
<dd>

**enhancedReasoning:** `Optional<Boolean>` — Enable enhanced reasoning capabilities. Increases latency but improves response quality and reliability.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.agent.chat.listMessages() -> ListMessagesResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves a list of chat messages for a given chat session
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().chat().listMessages(
    ListMessagesRequest
        .builder()
        .chatSessionId("chat_session_id")
        .numMessages(1)
        .role(ListMessagesRequestRole.USER)
        .order(ListMessagesRequestOrder.ASC)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**chatSessionId:** `String` — Chat session ID
    
</dd>
</dl>

<dl>
<dd>

**numMessages:** `Optional<Integer>` — Number of messages to return
    
</dd>
</dl>

<dl>
<dd>

**role:** `Optional<ListMessagesRequestRole>` 

Filter by one or more message roles. Multiple roles can be specified as a comma-separated string.
If not specified, messages with all roles are returned.

**Available roles:**
- `user` - Messages from the user
- `assistant` - Text responses from the AI assistant
- `model` - Function/tool call requests from the model
- `function` - Function/tool call results
    
</dd>
</dl>

<dl>
<dd>

**order:** `Optional<ListMessagesRequestOrder>` — Order of messages
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Agent Prompts
<details><summary><code>client.agent.prompts.create(request) -> AgentPromptsResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Creates a new agent prompt
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().prompts().create(
    AgentPromptsCreateRequest
        .builder()
        .name("Medical Assistant System Prompt")
        .content("You are a helpful medical assistant specialized in FHIR data processing.")
        .description("System prompt for medical assistant agent")
        .isDefault(false)
        .tags(
            Optional.of(
                Arrays.asList("medical", "system")
            )
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**name:** `String` — Prompt name
    
</dd>
</dl>

<dl>
<dd>

**description:** `Optional<String>` — Prompt description
    
</dd>
</dl>

<dl>
<dd>

**content:** `String` — Prompt content
    
</dd>
</dl>

<dl>
<dd>

**isDefault:** `Optional<Boolean>` — Whether this is a default prompt
    
</dd>
</dl>

<dl>
<dd>

**tags:** `Optional<List<String>>` — Tags for categorizing the prompt
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.agent.prompts.list() -> PromptsListResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves a list of agent prompts belonging to the authenticated user
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().prompts().list();
```
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.agent.prompts.get(id) -> AgentPromptsResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves a specific prompt by its ID
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().prompts().get("id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — Prompt ID
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.agent.prompts.update(id, request) -> AgentPromptsResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Updates an existing prompt
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().prompts().update(
    "id",
    AgentPromptsUpdateRequest
        .builder()
        .name("Medical Assistant System Prompt")
        .description("Updated system prompt")
        .content("You are a helpful medical assistant. Always cite ICD-10 codes when discussing diagnoses.")
        .isDefault(false)
        .tags(
            Optional.of(
                Arrays.asList("medical", "system", "updated")
            )
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — Prompt ID
    
</dd>
</dl>

<dl>
<dd>

**name:** `Optional<String>` — Prompt name
    
</dd>
</dl>

<dl>
<dd>

**description:** `Optional<String>` — Prompt description
    
</dd>
</dl>

<dl>
<dd>

**content:** `Optional<String>` — Prompt content
    
</dd>
</dl>

<dl>
<dd>

**isDefault:** `Optional<Boolean>` — Whether this is a default prompt
    
</dd>
</dl>

<dl>
<dd>

**tags:** `Optional<List<String>>` — Tags for categorizing the prompt
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.agent.prompts.delete(id) -> PromptsDeleteResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes a prompt
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().prompts().delete("id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — Prompt ID
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.agent.prompts.patch(id, request) -> AgentPromptsResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Patches an existing prompt
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.agent().prompts().patch(
    "id",
    Arrays.asList(
        JsonPatchOperation
            .builder()
            .op(JsonPatchOperationOp.REPLACE)
            .path("/content")
            .value("Updated prompt content.")
            .build()
    )
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — Agent Prompt ID
    
</dd>
</dl>

<dl>
<dd>

**request:** `List<JsonPatchOperation>` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Authtoken
<details><summary><code>client.authtoken.getToken(request) -> TokenResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

OAuth 2.0 client credentials token endpoint (RFC 6749 §4.4).
Accepts client_id and client_secret in the request body (JSON or
form-encoded) or via Basic Auth header (RFC 6749 §2.3.1), and
returns an access token with token expiration information.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.authtoken().getToken(
    ClientCredentialsRequest
        .builder()
        .clientId("your_client_id")
        .clientSecret("your_client_secret")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**grantType:** `Optional<String>` — Must be "client_credentials" if provided
    
</dd>
</dl>

<dl>
<dd>

**clientId:** `Optional<String>` — The client ID (credential username)
    
</dd>
</dl>

<dl>
<dd>

**clientSecret:** `Optional<String>` — The client secret (credential password)
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Cohort
<details><summary><code>client.cohort.analyze(request) -> CohortResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Converts natural language text into structured FHIR search queries for patient cohort analysis
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.cohort().analyze(
    CohortRequest
        .builder()
        .text("female patients over 65 with diabetes but not hypertension")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**text:** `String` — Natural language text describing patient cohort criteria
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Code Systems
<details><summary><code>client.construe.codeSystems.upload(request) -> UploadResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Upload a custom medical code system with codes and descriptions for use in code extraction. Requires a paid plan.
Returns 202 immediately; embedding generation runs asynchronously. Poll
GET /construe/codes/systems/{codesystem}?version={version} to check when status
transitions from "processing" to "ready" or "failed".
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.construe().codeSystems().upload(
    UploadRequest
        .builder()
        .name("CUSTOM_CODES")
        .version("1.0")
        .format(UploadRequestFormat.JSON)
        .codes(
            Optional.of(
                Arrays.asList(
                    CodeResponse
                        .builder()
                        .code("X001")
                        .description("Example custom code 1")
                        .build(),
                    CodeResponse
                        .builder()
                        .code("X002")
                        .description("Example custom code 2")
                        .build()
                )
            )
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**name:** `String` 

Name of the code system. Names are case-insensitive and stored uppercase.
Builtin system names (e.g. ICD-10-CM, SNOMED_CT_US_LITE, LOINC, CPT, etc.) are
reserved and cannot be used for custom uploads; attempts return HTTP 403 Forbidden.
    
</dd>
</dl>

<dl>
<dd>

**version:** `String` — Version of the code system
    
</dd>
</dl>

<dl>
<dd>

**revision:** `Optional<Double>` — Optional revision number
    
</dd>
</dl>

<dl>
<dd>

**format:** `UploadRequestFormat` — Upload format
    
</dd>
</dl>

<dl>
<dd>

**file:** `Optional<String>` 

The file contents as a base64-encoded string.
For CSV format, this is the CSV file contents.
For JSON format, this is a base64-encoded JSON array; prefer using 'codes' instead.
    
</dd>
</dl>

<dl>
<dd>

**codeCol:** `Optional<String>` — Column name containing codes (required for CSV format)
    
</dd>
</dl>

<dl>
<dd>

**descCol:** `Optional<String>` — Column name containing descriptions (required for CSV format)
    
</dd>
</dl>

<dl>
<dd>

**defnCol:** `Optional<String>` — Optional column name containing long definitions (for CSV format)
    
</dd>
</dl>

<dl>
<dd>

**codes:** `Optional<List<CodeResponse>>` 

The codes to upload as a JSON array (JSON format only).
This is the preferred way to upload JSON codes, as it avoids unnecessary base64 encoding.
If both 'codes' and 'file' are provided, 'codes' takes precedence.
    
</dd>
</dl>

<dl>
<dd>

**replace:** `Optional<Boolean>` 

If true, replaces an existing code system with the same name and version.
Builtin systems cannot be replaced; attempts to do so return HTTP 403 Forbidden.
When false (default), uploading a duplicate returns 409 Conflict.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.codeSystems.list() -> ListCodeSystemsResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Returns the terminology server's catalog of available code systems, including both built-in standard terminologies and custom uploaded systems.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.construe().codeSystems().list();
```
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.codeSystems.find(codesystem) -> GetCodeSystemDetailResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Returns full metadata for a single code system, including timestamps and builtin status.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.construe().codeSystems().find(
    "ICD-10-CM",
    FindRequest
        .builder()
        .version("2025")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**codesystem:** `String` — Code system name (e.g., "ICD-10-CM", "SNOMED_CT_US_LITE")
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<String>` — Specific version of the code system, such as `umls-2026aa`.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.codeSystems.delete(codesystem) -> DeleteCodeSystemResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes a custom (non-builtin) code system and all its codes. Builtin systems cannot be deleted.
Only available on dedicated instances. Large systems may take up to a minute to delete.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.construe().codeSystems().delete(
    "CUSTOM_CODES",
    DeleteRequest
        .builder()
        .version("version")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**codesystem:** `String` — Code system name
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<String>` — Specific version of the custom code system.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.codeSystems.export(codesystem) -> ExportCodeSystemResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Exports a custom (non-builtin) code system as a JSON file compatible with the upload format.
The exported file can be re-uploaded directly via POST /construe/upload with format "json".
Only available on dedicated instances. Builtin systems cannot be exported.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.construe().codeSystems().export(
    "CUSTOM_CODES",
    ExportRequest
        .builder()
        .version("version")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**codesystem:** `String` — Code system name
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<String>` — Specific version of the custom code system.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Construe
<details><summary><code>client.construe.submitFeedback(request) -> FeedbackResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Submits user feedback on results from the Construe extraction endpoint.
Feedback includes the full extraction result received and the result the user expected.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.construe().submitFeedback(
    FeedbackRequest
        .builder()
        .text("Patient has type 2 diabetes with hyperglycemia")
        .receivedResult(
            ExtractCodesResult
                .builder()
                .system(
                    ExtractRequestSystem
                        .builder()
                        .name("ICD-10-CM")
                        .version("2025")
                        .build()
                )
                .codes(
                    Arrays.asList(
                        ExtractedCodeResult
                            .builder()
                            .code("E11.9")
                            .description("Type 2 diabetes mellitus without complications")
                            .valid(true)
                            .build()
                    )
                )
                .build()
        )
        .expectedResult(
            ExtractCodesResult
                .builder()
                .system(
                    ExtractRequestSystem
                        .builder()
                        .name("ICD-10-CM")
                        .version("2025")
                        .build()
                )
                .codes(
                    Arrays.asList(
                        ExtractedCodeResult
                            .builder()
                            .code("E11.65")
                            .description("Type 2 diabetes mellitus with hyperglycemia")
                            .valid(true)
                            .build()
                    )
                )
                .build()
        )
        .detail("Expected code E11.65 because the text mentions hyperglycemia")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**text:** `String` — The natural language text that was used for code extraction
    
</dd>
</dl>

<dl>
<dd>

**receivedResult:** `ExtractCodesResult` 
    
</dd>
</dl>

<dl>
<dd>

**expectedResult:** `ExtractCodesResult` 
    
</dd>
</dl>

<dl>
<dd>

**detail:** `Optional<String>` — Optional details explaining the feedback
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Codes
<details><summary><code>client.construe.codes.extract(request) -> ExtractCodesResult</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Converts natural language text into structured medical codes.

Pass `system.version` to select a specific code system version, for example
`umls-2026aa` for UMLS 2026AA-backed systems.

Usage of CPT is subject to AMA requirements: see PhenoML Terms of Service.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.construe().codes().extract(
    ExtractRequest
        .builder()
        .text("Patient is a 14-year-old female, previously healthy, who is here for evaluation of abnormal renal ultrasound with atrophic right kidney.")
        .system(
            ExtractRequestSystem
                .builder()
                .name("ICD-10-CM")
                .version("2025")
                .build()
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**text:** `String` — Natural language text to extract codes from
    
</dd>
</dl>

<dl>
<dd>

**system:** `Optional<ExtractRequestSystem>` 
    
</dd>
</dl>

<dl>
<dd>

**config:** `Optional<ExtractRequestConfig>` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.codes.phenocr(request) -> ExtractCodesResult</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

**Alpha:** phenocr is an alpha feature. The API contract — request
parameters and response shape — may change as its internals evolve, and
results may vary between releases. Do not depend on it for production
workloads yet.

Extracts medical codes from natural language clinical text using phenocr.

Supported code systems: HPO, ICD-10-CM, and SNOMED_CT_US. The code
system name and version are both required.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.construe().codes().phenocr(
    PhenoCrRequest
        .builder()
        .text("5-year-old male with seizures, severe intellectual disability, microcephaly, and hypotonia.")
        .system(
            PhenocrExtractRequestSystem
                .builder()
                .name("HPO")
                .version("umls-2026AA")
                .build()
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**text:** `String` — Natural language text to extract codes from
    
</dd>
</dl>

<dl>
<dd>

**system:** `PhenocrExtractRequestSystem` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.codes.list(codesystem) -> ListCodesResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Returns a paginated list of all codes in the specified code system from the terminology server.

Usage of CPT is subject to AMA requirements: see PhenoML Terms of Service.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.construe().codes().list(
    "ICD-10-CM",
    CodesListRequest
        .builder()
        .version("2025")
        .cursor("cursor")
        .limit(1)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**codesystem:** `String` — Code system name (e.g., "ICD-10-CM", "SNOMED_CT_US_LITE")
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<String>` — Specific version of the code system, such as `umls-2026aa`.
    
</dd>
</dl>

<dl>
<dd>

**cursor:** `Optional<String>` — Pagination cursor from previous response
    
</dd>
</dl>

<dl>
<dd>

**limit:** `Optional<Integer>` — Maximum number of codes to return (default 20)
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.codes.lookup(codesystem, codeId) -> GetCodeResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Looks up a specific code in the terminology server and returns its details.

Usage of CPT is subject to AMA requirements: see PhenoML Terms of Service.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.construe().codes().lookup(
    "ICD-10-CM",
    "E1165",
    LookupRequest
        .builder()
        .version("version")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**codesystem:** `String` — Code system name
    
</dd>
</dl>

<dl>
<dd>

**codeId:** `String` 

The code identifier. ICD-10-CM codes are stored without their
cosmetic dot (use "E1165", not "E11.65").
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<String>` — Specific version of the code system, such as `umls-2026aa`.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.codes.searchSemantic(codesystem) -> SemanticSearchResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Performs semantic similarity search using vector embeddings.

**Availability**: This endpoint works for both **built-in and custom** code systems.

**When to use**: Best for natural language queries where you want to find conceptually
related codes, even when different terminology is used. The search understands meaning,
not just keywords.

**Examples**:
- Query "trouble breathing at night" finds codes like "Sleep apnea", "Orthopnea",
  "Nocturnal dyspnea" — semantically related but no exact keyword matches
- Query "heart problems" finds "Myocardial infarction", "Cardiac arrest", "Arrhythmia"

**Trade-offs**: Slower than text search (requires embedding generation), but finds
conceptually similar results that keyword search would miss.

See also: `/search/text` for faster keyword-based lookup with typo tolerance.

Usage of CPT is subject to AMA requirements: see PhenoML Terms of Service.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.construe().codes().searchSemantic(
    "ICD-10-CM",
    SearchSemanticRequest
        .builder()
        .text("patient has trouble breathing at night and wakes up gasping")
        .version("version")
        .limit(1)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**codesystem:** `String` — Code system name
    
</dd>
</dl>

<dl>
<dd>

**text:** `String` — Natural language text to find semantically similar codes for
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<String>` — Specific version of the code system, such as `umls-2026aa`.
    
</dd>
</dl>

<dl>
<dd>

**limit:** `Optional<Integer>` — Maximum number of results (default 10, max 50)
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.codes.searchText(codesystem) -> TextSearchResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Performs fast full-text search over code IDs and descriptions.

**Availability**: This endpoint is only available for **built-in code systems**.
Custom code systems uploaded via `/construe/upload` are not indexed for full-text search
and will return empty results. Use `/search/semantic` to search custom code systems.

**When to use**: Best for autocomplete UIs, code lookup, or when users know part of
the code ID or specific keywords. Fast response times suitable for typeahead interfaces.

**Features**:
- Substring matching on code IDs (e.g., "11.65" finds "E11.65")
- Typo tolerance on descriptions (not on code IDs)
- Fast response times (~10-50ms)

**Examples**:
- Query "E11" finds all codes starting with E11 (diabetes codes)
- Query "diabtes" (typo) still finds "diabetes" codes

**Trade-offs**: Faster than semantic search, but only matches keywords/substrings.
Won't find conceptually related codes with different terminology.

See also: `/search/semantic` for finding conceptually similar codes.

Usage of CPT is subject to AMA requirements: see PhenoML Terms of Service.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.construe().codes().searchText(
    "ICD-10-CM",
    SearchTextRequest
        .builder()
        .q("E11.65")
        .version("version")
        .limit(1)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**codesystem:** `String` — Code system name
    
</dd>
</dl>

<dl>
<dd>

**q:** `String` — Search query (searches code IDs and descriptions)
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<String>` — Specific version of the code system, such as `umls-2026aa`.
    
</dd>
</dl>

<dl>
<dd>

**limit:** `Optional<Integer>` — Maximum number of results (default 20, max 100)
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Fhir
<details><summary><code>client.fhir.search(fhirProviderId, fhirPath) -> Object</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves FHIR resources from the specified provider. Supports both individual resource retrieval (e.g. `Patient/123` via the path) and search operations.

FHIR search parameters are passed through to the upstream server verbatim as native query-string parameters; this proxy does not model, validate, or transform them. Append standard FHIR search parameters directly to the request URL. Supported parameters include:
- Resource-specific search parameters (e.g. `name` for Patient, `status` for Observation)
- Common search parameters (`_id`, `_lastUpdated`, `_tag`, `_profile`, `_security`, `_text`, `_content`, `_filter`)
- Result parameters (`_count`, `_offset`, `_sort`, `_include`, `_revinclude`, `_summary`, `_elements`)
- Search prefixes for dates, numbers, and quantities (`eq`, `ne`, `gt`, `ge`, `lt`, `le`, `sa`, `eb`, `ap`)

Examples:
- `Patient?name=John%20Doe&_count=10&_sort=family`
- `Observation?patient=Patient/123&date=ge2023-01-01&category=vital-signs&_sort=-date`

When using a generated SDK, supply these via the client's request-level query-parameter option (the SDK escape hatch) rather than a typed argument.

The request is proxied to the configured FHIR server with appropriate authentication headers.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhir().search(
    "550e8400-e29b-41d4-a716-446655440000",
    "Patient",
    SearchRequest
        .builder()
        .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
        .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**fhirProviderId:** `String` 

The ID of the FHIR provider to use. Can be either:
- A UUID representing the provider ID
- A provider name (legacy support - will just use the most recently updated provider with this name)
    
</dd>
</dl>

<dl>
<dd>

**fhirPath:** `String` 

The FHIR resource path to operate on. This follows FHIR RESTful API conventions.
Examples:
- "Patient" (for resource type operations)
- "Patient/123" (for specific resource operations)
- "Patient/123/_history" (for history operations)
    
</dd>
</dl>

<dl>
<dd>

**phenomlOnBehalfOf:** `Optional<String>` 

Optional header for on-behalf-of authentication. Used when making requests on behalf of another user or entity.
Must be in the format: Patient/{uuid} or Practitioner/{uuid}
    
</dd>
</dl>

<dl>
<dd>

**phenomlFhirProvider:** `Optional<String>` 

Optional header for FHIR provider authentication. Contains credentials in the format {fhir_provider_id}:{oauth2_token}.
Multiple FHIR provider integrations can be provided as comma-separated values.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhir.create(fhirProviderId, fhirPath, request) -> Object</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Creates a new FHIR resource on the specified provider. The request body should contain a valid FHIR resource in JSON format.

The request is proxied to the configured FHIR server with appropriate authentication headers.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhir().create(
    "550e8400-e29b-41d4-a716-446655440000",
    "Patient",
    CreateRequest
        .builder()
        .body(new 
            HashMap<String, Object>() {{put("resourceType", "Patient");
            }})
        .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
        .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**fhirProviderId:** `String` 

The ID of the FHIR provider to use. Can be either:
- A UUID representing the provider ID
- A provider name (legacy support - will just use the most recently updated provider with this name)
    
</dd>
</dl>

<dl>
<dd>

**fhirPath:** `String` 

The FHIR resource path to operate on. This follows FHIR RESTful API conventions.
Examples:
- "Patient" (for resource type operations)
- "Patient/123" (for specific resource operations)
- "Patient/123/_history" (for history operations)
    
</dd>
</dl>

<dl>
<dd>

**phenomlOnBehalfOf:** `Optional<String>` 

Optional header for on-behalf-of authentication. Used when making requests on behalf of another user or entity.
Must be in the format: Patient/{uuid} or Practitioner/{uuid}
    
</dd>
</dl>

<dl>
<dd>

**phenomlFhirProvider:** `Optional<String>` 

Optional header for FHIR provider authentication. Contains credentials in the format {fhir_provider_id}:{oauth2_token}.
Multiple FHIR provider integrations can be provided as comma-separated values.
    
</dd>
</dl>

<dl>
<dd>

**request:** `Object` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhir.upsert(fhirProviderId, fhirPath, request) -> Object</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Creates or updates a FHIR resource on the specified provider. If the resource exists, it will be updated; otherwise, it will be created.

The request is proxied to the configured FHIR server with appropriate authentication headers.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhir().upsert(
    "550e8400-e29b-41d4-a716-446655440000",
    "Patient",
    UpsertRequest
        .builder()
        .body(new 
            HashMap<String, Object>() {{put("resourceType", "Patient");
                put("id", "123");
            }})
        .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
        .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**fhirProviderId:** `String` 

The ID of the FHIR provider to use. Can be either:
- A UUID representing the provider ID
- A provider name (legacy support - will just use the most recently updated provider with this name)
    
</dd>
</dl>

<dl>
<dd>

**fhirPath:** `String` 

The FHIR resource path to operate on. This follows FHIR RESTful API conventions.
Examples:
- "Patient" (for resource type operations)
- "Patient/123" (for specific resource operations)
- "Patient/123/_history" (for history operations)
    
</dd>
</dl>

<dl>
<dd>

**phenomlOnBehalfOf:** `Optional<String>` 

Optional header for on-behalf-of authentication. Used when making requests on behalf of another user or entity.
Must be in the format: Patient/{uuid} or Practitioner/{uuid}
    
</dd>
</dl>

<dl>
<dd>

**phenomlFhirProvider:** `Optional<String>` 

Optional header for FHIR provider authentication. Contains credentials in the format {fhir_provider_id}:{oauth2_token}.
Multiple FHIR provider integrations can be provided as comma-separated values.
    
</dd>
</dl>

<dl>
<dd>

**request:** `Object` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhir.delete(fhirProviderId, fhirPath) -> Object</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes a FHIR resource from the specified provider.

The request is proxied to the configured FHIR server with appropriate authentication headers.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhir().delete(
    "550e8400-e29b-41d4-a716-446655440000",
    "Patient",
    DeleteRequest
        .builder()
        .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
        .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**fhirProviderId:** `String` 

The ID of the FHIR provider to use. Can be either:
- A UUID representing the provider ID
- A provider name (legacy support - will just use the most recently updated provider with this name)
    
</dd>
</dl>

<dl>
<dd>

**fhirPath:** `String` 

The FHIR resource path to operate on. This follows FHIR RESTful API conventions.
Examples:
- "Patient" (for resource type operations)
- "Patient/123" (for specific resource operations)
- "Patient/123/_history" (for history operations)
    
</dd>
</dl>

<dl>
<dd>

**phenomlOnBehalfOf:** `Optional<String>` 

Optional header for on-behalf-of authentication. Used when making requests on behalf of another user or entity.
Must be in the format: Patient/{uuid} or Practitioner/{uuid}
    
</dd>
</dl>

<dl>
<dd>

**phenomlFhirProvider:** `Optional<String>` 

Optional header for FHIR provider authentication. Contains credentials in the format {fhir_provider_id}:{oauth2_token}.
Multiple FHIR provider integrations can be provided as comma-separated values.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhir.patch(fhirProviderId, fhirPath, request) -> Object</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Partially updates a FHIR resource on the specified provider.

Two body formats are supported, selected by request content type:
- `application/json-patch+json` — an array of JSON Patch operations as defined in RFC 6902. Each operation specifies:
  - `op`: The operation type (add, remove, replace, move, copy, test)
  - `path`: JSON Pointer to the target location in the resource
  - `value`: The value to use (required for add, replace, and test operations)
- `application/fhir+json` — a partial FHIR resource for merge-patch semantics.

**Note:** This proxy currently forwards the request body to the upstream FHIR server with `Content-Type: application/fhir+json` regardless of the declared request content type. JSON Patch (RFC 6902) therefore only succeeds against upstream servers that accept patch arrays under `application/fhir+json`; servers that strictly enforce patch media types may reject or misinterpret it. Support for either format ultimately depends on the upstream FHIR server.

The request is proxied to the configured FHIR server with appropriate authentication headers.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhir().patch(
    "550e8400-e29b-41d4-a716-446655440000",
    "Patient",
    PatchRequest
        .builder()
        .body(
            Arrays.asList(
                PatchRequestBodyItem
                    .builder()
                    .op(PatchRequestBodyItemOp.REPLACE)
                    .path("/name/0/family")
                    .value("NewFamilyName")
                    .build()
            )
        )
        .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
        .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**fhirProviderId:** `String` 

The ID of the FHIR provider to use. Can be either:
- A UUID representing the provider ID
- A provider name (legacy support - will just use the most recently updated provider with this name)
    
</dd>
</dl>

<dl>
<dd>

**fhirPath:** `String` 

The FHIR resource path to operate on. This follows FHIR RESTful API conventions.
Examples:
- "Patient" (for resource type operations)
- "Patient/123" (for specific resource operations)
- "Patient/123/_history" (for history operations)
    
</dd>
</dl>

<dl>
<dd>

**phenomlOnBehalfOf:** `Optional<String>` 

Optional header for on-behalf-of authentication. Used when making requests on behalf of another user or entity.
Must be in the format: Patient/{uuid} or Practitioner/{uuid}
    
</dd>
</dl>

<dl>
<dd>

**phenomlFhirProvider:** `Optional<String>` 

Optional header for FHIR provider authentication. Contains credentials in the format {fhir_provider_id}:{oauth2_token}.
Multiple FHIR provider integrations can be provided as comma-separated values.
    
</dd>
</dl>

<dl>
<dd>

**request:** `List<PatchRequestBodyItem>` — Array of JSON Patch operations following RFC 6902
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhir.executeBundle(fhirProviderId, request) -> Object</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Executes a FHIR Bundle transaction or batch operation on the specified provider. This allows multiple FHIR resources to be processed in a single request.

The request body should contain a valid FHIR Bundle resource with transaction or batch type.

The request is proxied to the configured FHIR server with appropriate authentication headers.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhir().executeBundle(
    "550e8400-e29b-41d4-a716-446655440000",
    ExecuteBundleRequest
        .builder()
        .body(new 
            HashMap<String, Object>() {{put("resourceType", "Bundle");
                put("type", "transaction");
                put("entry", new ArrayList<Object>(Arrays.asList(new 
                HashMap<String, Object>() {{put("request", new 
                    HashMap<String, Object>() {{put("method", "POST");
                        put("url", "Patient");
                    }});
                    put("resource", new 
                    HashMap<String, Object>() {{put("resourceType", "Patient");
                        put("name", new ArrayList<Object>(Arrays.asList(new 
                        HashMap<String, Object>() {{put("family", "Doe");
                            put("given", new ArrayList<Object>(Arrays.asList("John")));
                        }})));
                    }});
                }}, new 
                HashMap<String, Object>() {{put("request", new 
                    HashMap<String, Object>() {{put("method", "POST");
                        put("url", "Observation");
                    }});
                    put("resource", new 
                    HashMap<String, Object>() {{put("resourceType", "Observation");
                        put("status", "final");
                        put("subject", new 
                        HashMap<String, Object>() {{put("reference", "Patient/123");
                        }});
                    }});
                }})));
            }})
        .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
        .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**fhirProviderId:** `String` 

The ID of the FHIR provider to use. Can be either:
- A UUID representing the provider ID
- A provider name (legacy support - will just use the most recently updated provider with this name)
    
</dd>
</dl>

<dl>
<dd>

**phenomlOnBehalfOf:** `Optional<String>` 

Optional header for on-behalf-of authentication. Used when making requests on behalf of another user or entity.
Must be in the format: Patient/{uuid} or Practitioner/{uuid}
    
</dd>
</dl>

<dl>
<dd>

**phenomlFhirProvider:** `Optional<String>` 

Optional header for FHIR provider authentication. Contains credentials in the format {fhir_provider_id}:{oauth2_token}.
Multiple FHIR provider integrations can be provided as comma-separated values.
    
</dd>
</dl>

<dl>
<dd>

**request:** `Object` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Fhir2Omop
<details><summary><code>client.fhir2Omop.create(request) -> CreateOmopResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Maps a FHIR R4 resource or Bundle into OMOP Common Data Model v5.4 rows
(person, visit_occurrence, condition_occurrence, drug_exposure,
procedure_occurrence, measurement, observation).

Resource support is intentionally limited to the OMOP tables returned by
this endpoint:
- `Patient` -> `person`
- `Encounter` -> `visit_occurrence`
- `Condition` -> `condition_occurrence`
- `Procedure` -> `procedure_occurrence`
- `MedicationRequest`, `MedicationStatement`, and
  `MedicationAdministration` -> `drug_exposure`
- `Immunization` -> `drug_exposure`
- `Observation` with a numeric `valueQuantity`, `valueInteger`, or
  numeric-looking `valueString` (for example `"<2"`) -> `measurement`
- non-numeric `Observation` -> `observation`
- `AllergyIntolerance` -> `observation`

`Medication` is supported only as reference data for medication
resources; it is not emitted as its own row because OMOP CDM has no
Medication table. Other reference/admin resources such as `Practitioner`,
`Organization`, `Location`, `Coverage`, and `Claim`, and clinical
workflow/document resources such as `DiagnosticReport`, `ServiceRequest`,
`CarePlan`, `DocumentReference`, `Composition`, `Specimen`, and
`DeviceUseStatement`, are currently accepted in a Bundle but are not
shaped into OMOP rows. Unsupported resource types are ignored rather than
listed under `dropped`; `dropped` is reserved for supported resource types
that were missing the subject/patient, code, or medication reference data
needed to produce a valid row.

Each resource's primary clinical coding is resolved to a standard OMOP
`concept_id`. Alongside the OMOP rows grouped by table (`tables`), the
response carries `mappings` (how each source coding resolved, linked back
to the row it produced), `dropped` (resources that could not be shaped
into a row), `vocab_version` (the OMOP vocabulary release codes were
resolved against), and a small `summary` of the resolution outcomes.

A `concept_id` of `0` is reported, not omitted (OMOP "no matching
concept" semantics): it covers both a coding with no standard match
(`UNMAPPED`) and an unverified suggestion for a text-only resource
(`UNCHECKED`). Only the primary clinical coding is resolved, so
`gender`/`race`/`ethnicity`/`visit`/`value`/`unit` `concept_id`s are
always `0`; the one populated non-resolved concept is measurement
`operator_concept_id`, set from a value comparator (`<`, `<=`, `>`, `>=`)
rather than the resolver. Each `*_source_value` carries the verbatim FHIR
coding (`system#code`), and `*_type_concept_id` is set to `32817` (EHR).

Medication codes are resolved whether they appear inline
(`medicationCodeableConcept`) or via a `medicationReference` to a contained,
relative (`Type/id`), or bundle-entry (`urn:uuid`) `Medication` resource.
Resources that cannot be shaped into a row — a medication with no usable
code, resolvable reference, or display, or any clinical resource whose
subject/patient reference cannot be tied to a person — are reported under
`dropped` rather than emitted as blank rows. The
bundle must contain at least one Patient resource.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhir2Omop().create(
    CreateOmopRequest
        .builder()
        .fhirResources(
            new HashMap<String, Object>() {{
                put("resourceType", "Bundle");
                put("type", "collection");
                put("entry", new ArrayList<Object>(Arrays.asList(new 
                HashMap<String, Object>() {{put("resource", new 
                    HashMap<String, Object>() {{put("resourceType", "Patient");
                        put("id", "patient-1");
                        put("gender", "female");
                        put("birthDate", "1985-07-22");
                    }});
                }}, new 
                HashMap<String, Object>() {{put("resource", new 
                    HashMap<String, Object>() {{put("resourceType", "Condition");
                        put("id", "condition-1");
                        put("subject", new 
                        HashMap<String, Object>() {{put("reference", "Patient/patient-1");
                        }});
                        put("code", new 
                        HashMap<String, Object>() {{put("coding", new ArrayList<Object>(Arrays.asList(new 
                            HashMap<String, Object>() {{put("system", "http://snomed.info/sct");
                                put("code", "44054006");
                                put("display", "Type 2 diabetes mellitus");
                            }})));
                        }});
                        put("onsetDateTime", "2024-01-15");
                    }});
                }}, new 
                HashMap<String, Object>() {{put("resource", new 
                    HashMap<String, Object>() {{put("resourceType", "MedicationRequest");
                        put("id", "medreq-1");
                        put("status", "active");
                        put("subject", new 
                        HashMap<String, Object>() {{put("reference", "Patient/patient-1");
                        }});
                        put("medicationReference", new 
                        HashMap<String, Object>() {{put("reference", "#med0");
                        }});
                        put("authoredOn", "2024-01-16");
                        put("contained", new ArrayList<Object>(Arrays.asList(new 
                        HashMap<String, Object>() {{put("resourceType", "Medication");
                            put("id", "med0");
                            put("code", new 
                            HashMap<String, Object>() {{put("coding", new ArrayList<Object>(Arrays.asList(new 
                                HashMap<String, Object>() {{put("system", "http://www.nlm.nih.gov/research/umls/rxnorm");
                                    put("code", "860975");
                                    put("display", "metformin hydrochloride 500 MG");
                                }})));
                            }});
                        }})));
                    }});
                }})));
            }}
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**fhirResources:** `Map<String, Object>` 

FHIR resources (single resource or Bundle). Must contain at least one
Patient resource. Supported row-producing resources are Patient,
Encounter, Condition, Procedure, MedicationRequest,
MedicationStatement, MedicationAdministration, Immunization,
Observation, and AllergyIntolerance. Standalone Medication resources
are consumed by medication references rather than mapped to their own
table. Other resource types are accepted but ignored.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## FhirProvider
<details><summary><code>client.fhirProvider.create(request) -> FhirProviderResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Creates a new FHIR provider configuration with authentication credentials.

Note: The "sandbox" provider type cannot be created via this API - it is managed internally.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhirProvider().create(
    FhirProviderCreateRequest
        .builder()
        .name("Epic Sandbox")
        .provider(Provider.EPIC)
        .baseUrl("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4")
        .auth(
            FhirProviderCreateRequestAuth.clientSecret(
                ClientSecretAuth
                    .builder()
                    .clientId("your-client-id")
                    .clientSecret("your-client-secret")
                    .build()
            )
        )
        .description("Epic sandbox environment for testing")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**name:** `String` — Display name for the FHIR provider
    
</dd>
</dl>

<dl>
<dd>

**description:** `Optional<String>` — Optional description of the FHIR provider
    
</dd>
</dl>

<dl>
<dd>

**provider:** `Provider` 
    
</dd>
</dl>

<dl>
<dd>

**baseUrl:** `String` — Base URL of the FHIR server
    
</dd>
</dl>

<dl>
<dd>

**auth:** `FhirProviderCreateRequestAuth` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhirProvider.list() -> FhirProviderListResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves a list of all active FHIR providers for the authenticated user.

On shared instances, only sandbox providers are returned.
Sandbox providers return FhirProviderSandboxInfo.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhirProvider().list();
```
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhirProvider.get(fhirProviderId) -> FhirProviderResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves a specific FHIR provider configuration by its ID.

Sandbox providers return FhirProviderSandboxInfo.
On shared instances, only sandbox providers can be accessed.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhirProvider().get("fhir_provider_id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**fhirProviderId:** `String` — ID of the FHIR provider to retrieve
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhirProvider.delete(fhirProviderId) -> DeleteResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes a FHIR provider.

Note: Sandbox providers cannot be deleted.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhirProvider().delete("fhir_provider_id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**fhirProviderId:** `String` — ID of the FHIR provider to delete
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Auth Config
<details><summary><code>client.fhirProvider.authConfig.add(fhirProviderId, request) -> FhirProviderResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Adds a new authentication configuration to an existing FHIR provider.
This enables key rotation and multiple auth configurations per provider.

Note: Sandbox providers cannot be modified.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhirProvider().authConfig().add(
    "1716d214-de93-43a4-aa6b-a878d864e2ad",
    FhirProviderAddAuthConfigRequest.clientSecret(
        ClientSecretAuth
            .builder()
            .clientId("your-client-id")
            .clientSecret("your-client-secret")
            .build()
    )
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**fhirProviderId:** `String` — ID of the FHIR provider to add auth config to
    
</dd>
</dl>

<dl>
<dd>

**request:** `FhirProviderAddAuthConfigRequest` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhirProvider.authConfig.setActive(fhirProviderId, request) -> FhirProviderResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Sets which authentication configuration should be active for a FHIR provider.
Only one auth config can be active at a time.

If the specified auth config is already active, the request succeeds without
making any changes and returns a message indicating the config is already active.

Note: Sandbox providers cannot be modified.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhirProvider().authConfig().setActive(
    "1716d214-de93-43a4-aa6b-a878d864e2ad",
    FhirProviderSetActiveAuthConfigRequest
        .builder()
        .authConfigId("auth-config-456")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**fhirProviderId:** `String` — ID of the FHIR provider
    
</dd>
</dl>

<dl>
<dd>

**authConfigId:** `String` — ID of the auth configuration to set as active
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhirProvider.authConfig.remove(fhirProviderId, request) -> RemoveResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Removes an authentication configuration from a FHIR provider.
Cannot remove the currently active auth configuration.

Note: Sandbox providers cannot be modified.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.fhirProvider().authConfig().remove(
    "1716d214-de93-43a4-aa6b-a878d864e2ad",
    FhirProviderRemoveAuthConfigRequest
        .builder()
        .authConfigId("auth-config-456")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**fhirProviderId:** `String` — ID of the FHIR provider
    
</dd>
</dl>

<dl>
<dd>

**authConfigId:** `String` — ID of the auth configuration to remove
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Implementation Guides
<details><summary><code>client.implementationGuides.implementationGuides.list() -> ImplementationGuideListResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Returns every implementation guide on this instance — both guides that
have stored metadata (a profile_context) and guides referenced by at
least one custom profile — with the number of profiles in each.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.implementationGuides().implementationGuides().list();
```
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.implementationGuides.implementationGuides.get(name) -> ImplementationGuideDetail</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Returns a single implementation guide, including its profile_context and
the ids of the profiles that belong to it.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.implementationGuides().implementationGuides().get("acme-cardiology");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**name:** `String` — The implementation guide name.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.implementationGuides.implementationGuides.update(name, request) -> ImplementationGuideSummary</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Sets (or clears, with an empty value) the natural-language profile_context
for an implementation guide. The context is injected into the LLM during
resource detection to help select the right profiles from this guide.
It applies to every profile in the guide.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.implementationGuides().implementationGuides().update(
    "acme-cardiology",
    UpdateImplementationGuideRequest
        .builder()
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**name:** `String` — The implementation guide name.
    
</dd>
</dl>

<dl>
<dd>

**profileContext:** `Optional<String>` — Natural-language context that helps the LLM select the right profiles from this implementation guide during resource detection. An empty value clears the context. Max 2000 characters.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.implementationGuides.implementationGuides.delete(name)</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes the stored metadata for an implementation guide — its
profile_context and timestamps. Member profiles keep their
implementation_guide assignment, so a guide still referenced by at least
one profile continues to appear in listings, just without context or
timestamps.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.implementationGuides().implementationGuides().delete("acme-cardiology");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**name:** `String` — The implementation guide name.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Lang2Fhir
<details><summary><code>client.lang2Fhir.create(request) -> Map&amp;lt;String, Object&amp;gt;</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Converts natural language text into a structured FHIR resource.

**Patient identifier handling.** When generating a `patient` (or `patient-canvas`) resource, US Core requires `Patient.identifier` (a business identifier such as an MRN). When the source text contains an identifier, it is extracted with an appropriate URI system. When the source text does not contain a detectable identifier, a synthetic one is generated with `system: "urn:phenoml:lang2fhir-generated-id"` and a UUID `value` so the resource remains FHIR-valid and US Core conformant. Callers who need a tenant-specific namespace should rewrite the synthetic system after extraction.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.lang2Fhir().create(
    CreateRequest
        .builder()
        .version("R4")
        .resource(CreateRequestResource.CONDITION_ENCOUNTER_DIAGNOSIS)
        .text("Patient has severe persistent asthma with acute exacerbation")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**version:** `String` — FHIR version to use
    
</dd>
</dl>

<dl>
<dd>

**resource:** `CreateRequestResource` — Type of FHIR resource to create. Use 'auto' for automatic resource type detection, or specify a supported US Core profile. Recommended to use the supported US Core Profiles for validated results but you can also use any custom profile you've uploaded (if you're a develop or launch customer) 
    
</dd>
</dl>

<dl>
<dd>

**text:** `String` — Natural language text to convert
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.lang2Fhir.createMulti(request) -> CreateMultiResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Analyzes natural language text and extracts multiple FHIR resources, returning them as a transaction Bundle.
Automatically detects Patient, Condition, MedicationRequest, Observation, and other resource types from the text.
Resources are linked with proper references (e.g., Conditions reference the Patient).

**Patient identifier handling.** US Core requires `Patient.identifier` (a business identifier such as an MRN). When the source text contains an identifier, it is extracted with an appropriate URI system. When the source text does not contain a detectable identifier, a synthetic one is generated with `system: "urn:phenoml:lang2fhir-generated-id"` and a UUID `value` so the bundle remains FHIR-valid and US Core conformant. Callers who need a tenant-specific namespace should rewrite the synthetic system after extraction.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.lang2Fhir().createMulti(
    CreateMultiRequest
        .builder()
        .text("John Smith, 45-year-old male, diagnosed with Type 2 Diabetes. Prescribed Metformin 500mg twice daily. Blood pressure 140/90.")
        .version("R4")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**text:** `String` — Natural language text containing multiple clinical concepts to extract
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<String>` — FHIR version to use
    
</dd>
</dl>

<dl>
<dd>

**provider:** `Optional<String>` — Optional FHIR provider name for provider-specific profiles
    
</dd>
</dl>

<dl>
<dd>

**implementationGuide:** `Optional<String>` — Custom Implementation Guide name. When specified, profiles from this IG are included alongside US Core profiles during resource detection. US Core is always the base layer; custom IG profiles are additive.
    
</dd>
</dl>

<dl>
<dd>

**detectionEffort:** `Optional<CreateMultiRequestDetectionEffort>` — Detection effort. 'standard' runs detection once, 'deep' runs detection multiple times for higher recall.
    
</dd>
</dl>

<dl>
<dd>

**validationMethod:** `Optional<CreateMultiRequestValidationMethod>` — FHIR validation method to apply to the generated bundle. 'none' skips validation (default). 'check' runs the bundle through a FHIR structure validator and includes the results in the response. 'fix' runs validation and attempts to auto-correct errors using an LLM (up to 3 validation passes). The response includes results from each pass. Warning: 'fix' can significantly increase latency due to multiple LLM and validation round-trips.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.lang2Fhir.search(request) -> SearchResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Converts natural language text into FHIR search parameters.
Automatically identifies the appropriate FHIR resource type and generates valid search query parameters.

Supported resource types include: AllergyIntolerance, Appointment, CarePlan, CareTeam, Condition,
Coverage, Device, DiagnosticReport, DocumentReference, Encounter, Goal, Immunization, Location,
Medication, MedicationRequest, Observation, Organization, Patient, PlanDefinition, Practitioner,
PractitionerRole, Procedure, Provenance, Questionnaire, QuestionnaireResponse, RelatedPerson,
Schedule, ServiceRequest, Slot, and Specimen.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.lang2Fhir().search(
    SearchRequest
        .builder()
        .text("Appointments between March 2-9, 2025")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**text:** `String` 

Natural language text to convert into FHIR search parameters.
The system will automatically identify the appropriate resource type and generate valid search parameters.

Examples:
- "Appointments between March 2-9, 2025" → Appointment search with date range
- "Patients with diabetes" → Condition search with code parameter
- "Active medication requests for metformin" → MedicationRequest search
- "Lab results for creatinine" → DiagnosticReport search
- "Dr. Smith's schedule" → Practitioner or Schedule search
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.lang2Fhir.uploadProfile(request) -> UploadProfileResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

**Deprecated — use `POST /fhir/profiles` instead.** This route continues to work
and operates on the same custom profiles, so no migration is required; it
will be removed in a future release. Note that `POST /fhir/profiles` does not
accept `profile_context`; set implementation-guide context with
`PUT /fhir/implementation-guides/{name}`.

Upload a custom FHIR StructureDefinition profile for use with the lang2fhir service.

All metadata is derived from the StructureDefinition JSON itself. The lowercase `id` field
from the StructureDefinition is used as the profile's unique identifier and lookup key.
To use the uploaded profile with `/lang2fhir/create`, pass this id as the `resource` parameter.

Uploads will be rejected if:
- A built-in US Core or R4 base profile already exists with the same id
- A custom profile with the same id has already been uploaded
- A custom profile with the same url has already been uploaded
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.lang2Fhir().uploadProfile(
    ProfileUploadRequest
        .builder()
        .profile("eyJyZXNvdXJjZVR5cGUiOiJTdHJ1Y3R1cmVEZWZpbml0aW9uIiwiaWQiOiJjdXN0b20tcGF0aWVudCIsInVybCI6Imh0dHA6Ly9waGVub21sLmNvbS9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vY3VzdG9tLXBhdGllbnQiLCJuYW1lIjoiQ3VzdG9tUGF0aWVudCIsInN0YXR1cyI6ImFjdGl2ZSIsImZoaXJWZXJzaW9uIjoiNC4wLjEiLCJraW5kIjoicmVzb3VyY2UiLCJhYnN0cmFjdCI6ZmFsc2UsInR5cGUiOiJQYXRpZW50IiwiYmFzZURlZmluaXRpb24iOiJodHRwOi8vaGw3Lm9yZy9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vUGF0aWVudCIsImRlcml2YXRpb24iOiJjb25zdHJhaW50Iiwic25hcHNob3QiOnsiZWxlbWVudCI6W3siaWQiOiJQYXRpZW50IiwicGF0aCI6IlBhdGllbnQiLCJtaW4iOjAsIm1heCI6IioifSx7ImlkIjoiUGF0aWVudC5uYW1lIiwicGF0aCI6IlBhdGllbnQubmFtZSIsIm1pbiI6MSwibWF4IjoiKiJ9XX19Cg==")
        .implementationGuide("acme-cardiology")
        .profileContext("When clinical text describes cardiology-specific findings, prefer this profile over the generic US Core Condition.")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**request:** `ProfileUploadRequest` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.lang2Fhir.document(request) -> Map&amp;lt;String, Object&amp;gt;</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Extracts text from a document (PDF or image) and converts it into a structured FHIR resource.

**Patient identifier handling.** When generating a `patient` (or `patient-canvas`) resource, US Core requires `Patient.identifier` (a business identifier such as an MRN). When the source text contains an identifier, it is extracted with an appropriate URI system. When the source text does not contain a detectable identifier, a synthetic one is generated with `system: "urn:phenoml:lang2fhir-generated-id"` and a UUID `value` so the resource remains FHIR-valid and US Core conformant. Callers who need a tenant-specific namespace should rewrite the synthetic system after extraction.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.lang2Fhir().document(
    DocumentRequest
        .builder()
        .version("R4")
        .resource("questionnaire")
        .content("JVBERi0xLjQKJeLjz9MK...(base64-encoded PDF or image bytes)")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**version:** `String` — FHIR version to use
    
</dd>
</dl>

<dl>
<dd>

**resource:** `String` — Type of FHIR resource to create. Accepts any FHIR resource type or US Core profile name.
    
</dd>
</dl>

<dl>
<dd>

**content:** `String` 

Base64 encoded file content.
Supported file types: PDF (application/pdf), PNG (image/png), JPEG (image/jpeg).
File type is auto-detected from content magic bytes.
    
</dd>
</dl>

<dl>
<dd>

**config:** `Optional<DocumentConfig>` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.lang2Fhir.documentMulti(request) -> DocumentMultiResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Extracts text from a document (PDF or image) and converts it into multiple FHIR resources,
returned as a transaction Bundle. Combines document text extraction with multi-resource detection.
Automatically detects Patient, Condition, MedicationRequest, Observation, and other resource types.
Resources are linked with proper references (e.g., Conditions reference the Patient).

**Patient identifier handling.** US Core requires `Patient.identifier` (a business identifier such as an MRN). When the source text contains an identifier, it is extracted with an appropriate URI system. When the source text does not contain a detectable identifier, a synthetic one is generated with `system: "urn:phenoml:lang2fhir-generated-id"` and a UUID `value` so the bundle remains FHIR-valid and US Core conformant. Callers who need a tenant-specific namespace should rewrite the synthetic system after extraction.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.lang2Fhir().documentMulti(
    DocumentMultiRequest
        .builder()
        .version("R4")
        .content("JVBERi0xLjQKJeLjz9MK...(base64-encoded PDF or image bytes)")
        .provider("medplum")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**version:** `String` — FHIR version to use
    
</dd>
</dl>

<dl>
<dd>

**content:** `String` 

Base64 encoded file content.
Supported file types: PDF (application/pdf), PNG (image/png), JPEG (image/jpeg).
File type is auto-detected from content magic bytes.
    
</dd>
</dl>

<dl>
<dd>

**provider:** `Optional<String>` — Optional FHIR provider name for provider-specific profiles
    
</dd>
</dl>

<dl>
<dd>

**implementationGuide:** `Optional<String>` — Custom Implementation Guide name. When specified, profiles from this IG are included alongside US Core profiles during resource detection. US Core is always the base layer; custom IG profiles are additive.
    
</dd>
</dl>

<dl>
<dd>

**detectionEffort:** `Optional<DocumentMultiRequestDetectionEffort>` — Detection effort. 'standard' runs detection once, 'deep' runs detection multiple times for higher recall.
    
</dd>
</dl>

<dl>
<dd>

**validationMethod:** `Optional<DocumentMultiRequestValidationMethod>` — FHIR validation method to apply to the generated bundle. 'none' skips validation (default). 'check' runs the bundle through a FHIR structure validator and includes the results in the response. 'fix' runs validation and attempts to auto-correct errors using an LLM (up to 3 validation passes). The response includes results from each pass. Warning: 'fix' can significantly increase latency due to multiple LLM and validation round-trips.
    
</dd>
</dl>

<dl>
<dd>

**config:** `Optional<DocumentConfig>` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Profiles
<details><summary><code>client.profiles.profiles.list() -> ProfileListResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Returns metadata for every custom (uploaded) FHIR profile on this
instance, across all implementation guides. The full StructureDefinition
JSON is omitted from each entry; fetch a single profile by id to retrieve it.

The `url` query parameter filters by canonical URL. The canonical URL is the
stable key other platform features use to reference a profile (FHIR's
`meta.profile`, `baseDefinition`), since StructureDefinition ids are only
unique within a package. A non-matching filter returns an empty list, not a 404.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.profiles().profiles().list(
    ListRequest
        .builder()
        .url("http://phenoml.com/fhir/StructureDefinition/custom-patient|1.0.0")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**url:** `Optional<String>` — Filter by canonical URL. Accepts the FHIR pinned form `url|version` (split on the last `|`); the bare form matches the current version.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.profiles.profiles.create(request) -> ProfileSummary</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Creates a custom profile from a FHIR StructureDefinition supplied as a JSON
object. All metadata (version, resource type, id, url) is derived from the
StructureDefinition; the lowercase StructureDefinition id becomes the
profile's lookup key. Code system configuration is auto-extracted from the
snapshot. Optionally group the profile under a named implementation guide.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.profiles().profiles().create(
    ProfileUploadRequest
        .builder()
        .structureDefinition(
            new HashMap<String, Object>() {{
                put("key", "value");
            }}
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**request:** `ProfileUploadRequest` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.profiles.profiles.get(id) -> ProfileGetResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Returns a single custom profile by id, including its full StructureDefinition JSON.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.profiles().profiles().get("custom-patient");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — The lowercase StructureDefinition id of the custom profile.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.profiles.profiles.update(id, request) -> ProfileSummary</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Replaces an existing custom profile with a new StructureDefinition. The
`id` path parameter is authoritative: if the StructureDefinition includes
an `id` it must match the path parameter, and if it omits one the path
parameter is used. The FHIR resource type of the profile cannot change.
Code system configuration is
re-derived from the new StructureDefinition. When `implementation_guide` is
omitted, the profile keeps its existing implementation guide. The instance
stores a single version per canonical URL, so this replaces it in place.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.profiles().profiles().update(
    "custom-patient",
    ProfileUploadRequest
        .builder()
        .structureDefinition(
            new HashMap<String, Object>() {{
                put("key", "value");
            }}
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — The lowercase StructureDefinition id of the custom profile.
    
</dd>
</dl>

<dl>
<dd>

**request:** `ProfileUploadRequest` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.profiles.profiles.delete(id)</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Permanently deletes a custom profile by id.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.profiles().profiles().delete("custom-patient");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — The lowercase StructureDefinition id of the custom profile.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Templates
<details><summary><code>client.summary.templates.list() -> ListResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves all summary templates for the authenticated user
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.summary().templates().list();
```
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.summary.templates.create(request) -> CreateSummaryTemplateResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Creates a summary template from an example using LLM function calling
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.summary().templates().create(
    CreateSummaryTemplateRequest
        .builder()
        .name("Discharge Summary")
        .exampleSummary("Patient John Doe, age 45, was admitted on 2024-01-10 with Type 2 Diabetes. Discharged on 2024-01-15 with Metformin 500mg BID.")
        .mode("narrative")
        .targetResources(
            Arrays.asList("Patient", "Condition", "MedicationRequest")
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**name:** `String` — Name of the template
    
</dd>
</dl>

<dl>
<dd>

**description:** `Optional<String>` — Description of the template
    
</dd>
</dl>

<dl>
<dd>

**exampleSummary:** `String` — Example summary note to generate template from
    
</dd>
</dl>

<dl>
<dd>

**targetResources:** `List<String>` — List of target FHIR resources
    
</dd>
</dl>

<dl>
<dd>

**exampleFhirData:** `Optional<Map<String, Object>>` — Optional example FHIR data that corresponds to the example summary
    
</dd>
</dl>

<dl>
<dd>

**mode:** `String` — Template mode (stored with the template)
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.summary.templates.get(id) -> GetResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves a specific summary template
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.summary().templates().get("id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — Template ID
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.summary.templates.update(id, request) -> UpdateResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Updates an existing summary template
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.summary().templates().update(
    "id",
    UpdateSummaryTemplateRequest
        .builder()
        .name("Discharge Summary")
        .template("Patient {{Patient.name[0].text}} was discharged on {{Encounter[0].period.end}} with {{MedicationRequest[0].medicationCodeableConcept.coding[0].display}} {{MedicationRequest[0].dosageInstruction[0].text}}.")
        .mode("narrative")
        .targetResources(
            Arrays.asList("Patient", "Encounter", "MedicationRequest")
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — Template ID
    
</dd>
</dl>

<dl>
<dd>

**name:** `String` 
    
</dd>
</dl>

<dl>
<dd>

**description:** `Optional<String>` 
    
</dd>
</dl>

<dl>
<dd>

**template:** `String` — Updated template with placeholders
    
</dd>
</dl>

<dl>
<dd>

**targetResources:** `List<String>` 
    
</dd>
</dl>

<dl>
<dd>

**mode:** `String` — Template mode
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.summary.templates.delete(id) -> DeleteResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes a summary template
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.summary().templates().delete("id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — Template ID
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Summary
<details><summary><code>client.summary.create(request) -> CreateSummaryResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Creates a summary from FHIR resources using one of three modes:
- **narrative**: Uses a template to substitute FHIR data into placeholders (requires template_id)
- **flatten**: Flattens FHIR resources into a searchable format for RAG/search (no template needed)
- **ips**: Generates an International Patient Summary (IPS) narrative per ISO 27269/HL7 FHIR IPS IG. Requires a Bundle with exactly one Patient resource (returns 400 error if no Patient or multiple Patients are present). Automatically filters resources to those referencing the patient and generates sections for allergies, medications, problems, immunizations, procedures, and vital signs.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.summary().create(
    CreateSummaryRequest
        .builder()
        .fhirResources(
            new HashMap<String, Object>() {{
                put("resourceType", "Bundle");
                put("type", "collection");
                put("entry", new ArrayList<Object>(Arrays.asList(new 
                HashMap<String, Object>() {{put("resource", new 
                    HashMap<String, Object>() {{put("resourceType", "Patient");
                        put("name", new ArrayList<Object>(Arrays.asList(new 
                        HashMap<String, Object>() {{put("given", new ArrayList<Object>(Arrays.asList("John")));
                            put("family", "Doe");
                        }})));
                        put("gender", "male");
                        put("birthDate", "1979-03-15");
                    }});
                }}, new 
                HashMap<String, Object>() {{put("resource", new 
                    HashMap<String, Object>() {{put("resourceType", "Condition");
                        put("code", new 
                        HashMap<String, Object>() {{put("text", "Type 2 Diabetes Mellitus");
                        }});
                        put("onsetDateTime", "2024-01-15");
                    }});
                }})));
            }}
        )
        .mode(CreateSummaryRequestMode.NARRATIVE)
        .templateId("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**mode:** `Optional<CreateSummaryRequestMode>` 

Summary generation mode:
- narrative: Substitute FHIR data into a template (requires template_id)
- flatten: Flatten FHIR resources for RAG/search (no template needed)
- ips: Generate International Patient Summary (IPS) narrative per ISO 27269/HL7 FHIR IPS IG
    
</dd>
</dl>

<dl>
<dd>

**templateId:** `Optional<String>` — ID of the template to use (required for narrative mode)
    
</dd>
</dl>

<dl>
<dd>

**fhirResources:** `Map<String, Object>` 

FHIR resources (single resource or Bundle).
For IPS mode, must be a Bundle containing exactly one Patient resource with at least one
identifier (id, fullUrl, or identifier field). Returns an error if no Patient is found,
if multiple Patients are present, or if the Patient has no identifiers. Resources are
automatically filtered to only include those referencing the patient.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Tools
<details><summary><code>client.tools.createFhirResource(request) -> Lang2FhirAndCreateResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Converts natural language to FHIR resource and optionally stores it in a FHIR server
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.tools().createFhirResource(
    Lang2FhirAndCreateRequest
        .builder()
        .resource(Lang2FhirAndCreateRequestResource.CONDITION_ENCOUNTER_DIAGNOSIS)
        .text("Patient has severe persistent asthma with acute exacerbation")
        .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
        .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
        .provider("550e8400-e29b-41d4-a716-446655440000")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**phenomlOnBehalfOf:** `Optional<String>` 

Optional header for on-behalf-of authentication. Used when making requests on behalf of another user or entity.
Must be in the format: Patient/{uuid} or Practitioner/{uuid}
    
</dd>
</dl>

<dl>
<dd>

**phenomlFhirProvider:** `Optional<String>` 

Optional header for FHIR provider authentication. Contains credentials in the format {fhir_provider_id}:{oauth2_token}.
Multiple FHIR provider integrations can be provided as comma-separated values.
    
</dd>
</dl>

<dl>
<dd>

**resource:** `Lang2FhirAndCreateRequestResource` — Type of FHIR resource to create. Use 'auto' for automatic resource type detection, or specify a supported US Core profile.
    
</dd>
</dl>

<dl>
<dd>

**text:** `String` — Natural language text to convert to FHIR resource
    
</dd>
</dl>

<dl>
<dd>

**provider:** `Optional<String>` — FHIR provider ID - must be a valid UUID from existing FHIR providers. also supports provider by name (e.g. medplum)
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.tools.createFhirResourcesMulti(request) -> Lang2FhirAndCreateMultiResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Extracts multiple FHIR resources from natural language text and stores them in a FHIR server.
Automatically detects Patient, Condition, MedicationRequest, Observation, and other resource types.
Resources are linked with proper references and submitted as a transaction bundle.
For FHIR servers that don't auto-resolve urn:uuid references, this endpoint will automatically
resolve them via PUT requests after the initial bundle creation.
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.tools().createFhirResourcesMulti(
    Lang2FhirAndCreateMultiRequest
        .builder()
        .text("John Smith, 45-year-old male, diagnosed with Type 2 Diabetes. Prescribed Metformin 500mg twice daily.")
        .provider("medplum")
        .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
        .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
        .version("R4")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**phenomlOnBehalfOf:** `Optional<String>` 

Optional header for on-behalf-of authentication. Used when making requests on behalf of another user or entity.
Must be in the format: Patient/{uuid} or Practitioner/{uuid}
    
</dd>
</dl>

<dl>
<dd>

**phenomlFhirProvider:** `Optional<String>` 

Optional header for FHIR provider authentication. Contains credentials in the format {fhir_provider_id}:{oauth2_token}.
Multiple FHIR provider integrations can be provided as comma-separated values.
    
</dd>
</dl>

<dl>
<dd>

**text:** `String` — Natural language text containing multiple clinical concepts to extract
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<String>` — FHIR version to use
    
</dd>
</dl>

<dl>
<dd>

**provider:** `String` — FHIR provider ID or name
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.tools.searchFhirResources(request) -> Lang2FhirAndSearchResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Converts natural language to FHIR search parameters and executes search in FHIR server
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.tools().searchFhirResources(
    Lang2FhirAndSearchRequest
        .builder()
        .text("Find all appointments for patient John Doe next week")
        .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
        .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
        .count(10)
        .provider("550e8400-e29b-41d4-a716-446655440000")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**phenomlOnBehalfOf:** `Optional<String>` 

Optional header for on-behalf-of authentication. Used when making requests on behalf of another user or entity.
Must be in the format: Patient/{uuid} or Practitioner/{uuid}
    
</dd>
</dl>

<dl>
<dd>

**phenomlFhirProvider:** `Optional<String>` 

Optional header for FHIR provider authentication. Contains credentials in the format {fhir_provider_id}:{oauth2_token}.
Multiple FHIR provider integrations can be provided as comma-separated values.
    
</dd>
</dl>

<dl>
<dd>

**text:** `String` — Natural language text to convert to FHIR search parameters
    
</dd>
</dl>

<dl>
<dd>

**patientId:** `Optional<String>` — Patient ID to filter results
    
</dd>
</dl>

<dl>
<dd>

**count:** `Optional<Integer>` — Maximum number of results to return
    
</dd>
</dl>

<dl>
<dd>

**provider:** `Optional<String>` — FHIR provider ID - must be a valid UUID from existing FHIR providers. also supports provider by name (e.g. medplum)
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.tools.analyzeCohort(request) -> CohortResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Uses LLM to extract search concepts from natural language and builds patient cohorts with inclusion/exclusion criteria
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.tools().analyzeCohort(
    CohortRequest
        .builder()
        .text("female patients over 20 with diabetes but not hypertension")
        .provider("550e8400-e29b-41d4-a716-446655440000")
        .phenomlOnBehalfOf("Patient/550e8400-e29b-41d4-a716-446655440000")
        .phenomlFhirProvider("550e8400-e29b-41d4-a716-446655440000:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c...")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**phenomlOnBehalfOf:** `Optional<String>` 

Optional header for on-behalf-of authentication. Used when making requests on behalf of another user or entity.
Must be in the format: Patient/{uuid} or Practitioner/{uuid}
    
</dd>
</dl>

<dl>
<dd>

**phenomlFhirProvider:** `Optional<String>` 

Optional header for FHIR provider authentication. Contains credentials in the format {fhir_provider_id}:{oauth2_token}.
Multiple FHIR provider integrations can be provided as comma-separated values.
    
</dd>
</dl>

<dl>
<dd>

**text:** `String` — Natural language text describing the patient cohort criteria
    
</dd>
</dl>

<dl>
<dd>

**provider:** `String` — FHIR provider ID - must be a valid UUID from existing FHIR providers. also supports provider by name (e.g. medplum)
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## MCP Servers
<details><summary><code>client.tools.mcpServers.create(request) -> McpServerResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Creates a new MCP server
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.tools().mcpServers().create(
    McpServerCreateRequest
        .builder()
        .name("My MCP Server")
        .mcpServerUrl("https://mcp.example.com")
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**name:** `String` — Name of the MCP server
    
</dd>
</dl>

<dl>
<dd>

**mcpServerUrl:** `String` — URL of the MCP server
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.tools.mcpServers.list() -> McpServerResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Lists all MCP servers for a specific user
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.tools().mcpServers().list();
```
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.tools.mcpServers.get(mcpServerId) -> McpServerResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Gets a MCP server by ID
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.tools().mcpServers().get("mcp_server_id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**mcpServerId:** `String` — ID of the MCP server to retrieve
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.tools.mcpServers.delete(mcpServerId) -> McpServerResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes a MCP server by ID
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.tools().mcpServers().delete("mcp_server_id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**mcpServerId:** `String` — ID of the MCP server to delete
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Tools McpTools
<details><summary><code>client.tools.mcpTools.list(mcpServerId) -> McpServerToolResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Lists all MCP server tools for a specific MCP server
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.tools().mcpTools().list("mcp_server_id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**mcpServerId:** `String` — ID of the MCP server to list tools for
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.tools.mcpTools.get(mcpServerToolId) -> McpServerToolResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Gets a MCP server tool by ID
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.tools().mcpTools().get("mcp_server_tool_id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**mcpServerToolId:** `String` — ID of the MCP server tool to retrieve
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.tools.mcpTools.delete(mcpServerToolId) -> McpServerToolResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes a MCP server tool by ID
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.tools().mcpTools().delete("mcp_server_tool_id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**mcpServerToolId:** `String` — ID of the MCP server tool to delete
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Voice
## Workflows
<details><summary><code>client.workflows.list() -> ListWorkflowsResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves all workflow definitions for the authenticated user
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.workflows().list(
    ListRequest
        .builder()
        .verbose(true)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**verbose:** `Optional<Boolean>` — If true, includes full workflow implementation details in workflow_details field
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.workflows.create(request) -> CreateWorkflowResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Creates a new workflow definition with graph generation from workflow instructions
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.workflows().create(
    CreateWorkflowRequest
        .builder()
        .name("Patient Data Mapping Workflow")
        .workflowInstructions("Given diagnosis data, find the patient and create a condition record linked to their encounter")
        .fhirProviderId(
            CreateWorkflowRequestFhirProviderId.of("550e8400-e29b-41d4-a716-446655440000")
        )
        .verbose(true)
        .sampleData(
            new HashMap<String, Object>() {{
                put("patient_last_name", "Rippin");
                put("patient_first_name", "Clay");
                put("diagnosis_code", "I10");
                put("encounter_date", "2024-01-15");
            }}
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**verbose:** `Optional<Boolean>` — If true, includes full workflow implementation details in workflow_details field
    
</dd>
</dl>

<dl>
<dd>

**name:** `String` — Human-readable name for the workflow
    
</dd>
</dl>

<dl>
<dd>

**workflowInstructions:** `String` — Natural language instructions that define the workflow logic
    
</dd>
</dl>

<dl>
<dd>

**sampleData:** `Map<String, Object>` — Sample data to use for workflow graph generation
    
</dd>
</dl>

<dl>
<dd>

**fhirProviderId:** `CreateWorkflowRequestFhirProviderId` — FHIR provider ID(s) - must be valid UUID(s) from existing FHIR providers
    
</dd>
</dl>

<dl>
<dd>

**dynamicGeneration:** `Optional<Boolean>` — Enable dynamic lang2fhir calls instead of pre-populated templates
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.workflows.get(id) -> GetResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves a workflow definition by its ID
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.workflows().get(
    "id",
    GetRequest
        .builder()
        .verbose(true)
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — ID of the workflow to retrieve
    
</dd>
</dl>

<dl>
<dd>

**verbose:** `Optional<Boolean>` — If true, includes full workflow implementation details in workflow_details field
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.workflows.update(id, request) -> UpdateResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Updates an existing workflow definition
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.workflows().update(
    "id",
    UpdateWorkflowRequest
        .builder()
        .name("Patient Data Mapping Workflow (v2)")
        .workflowInstructions("Given diagnosis data, find the patient and create a condition record linked to their encounter")
        .fhirProviderId(
            UpdateWorkflowRequestFhirProviderId.of("550e8400-e29b-41d4-a716-446655440000")
        )
        .verbose(true)
        .sampleData(
            new HashMap<String, Object>() {{
                put("patient_last_name", "Rippin");
                put("patient_first_name", "Clay");
                put("diagnosis_code", "I10");
                put("encounter_date", "2024-01-15");
            }}
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — ID of the workflow to update
    
</dd>
</dl>

<dl>
<dd>

**verbose:** `Optional<Boolean>` — If true, includes full workflow implementation details in workflow_details field
    
</dd>
</dl>

<dl>
<dd>

**name:** `String` — Human-readable name for the workflow
    
</dd>
</dl>

<dl>
<dd>

**workflowInstructions:** `String` — Natural language instructions that define the workflow logic
    
</dd>
</dl>

<dl>
<dd>

**sampleData:** `Map<String, Object>` — Sample data to use for workflow graph generation
    
</dd>
</dl>

<dl>
<dd>

**fhirProviderId:** `UpdateWorkflowRequestFhirProviderId` — FHIR provider ID(s) - must be valid UUID(s) from existing FHIR providers
    
</dd>
</dl>

<dl>
<dd>

**dynamicGeneration:** `Optional<Boolean>` — Enable dynamic lang2fhir calls instead of pre-populated templates
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.workflows.delete(id) -> DeleteResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Deletes a workflow definition by its ID
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.workflows().delete("id");
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — ID of the workflow to delete
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.workflows.execute(id, request) -> ExecuteWorkflowResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Executes a workflow with provided input data and returns results
</dd>
</dl>
</dd>
</dl>

#### 🔌 Usage

<dl>
<dd>

<dl>
<dd>

```java
client.workflows().execute(
    "7a8b9c0d-1234-5678-abcd-ef9876543210",
    ExecuteWorkflowRequest
        .builder()
        .inputData(
            new HashMap<String, Object>() {{
                put("patient_last_name", "Johnson");
                put("patient_first_name", "Mary");
                put("diagnosis_code", "M79.3");
                put("encounter_date", "2024-03-20");
            }}
        )
        .build()
);
```
</dd>
</dl>
</dd>
</dl>

#### ⚙️ Parameters

<dl>
<dd>

<dl>
<dd>

**id:** `String` — ID of the workflow to execute
    
</dd>
</dl>

<dl>
<dd>

**inputData:** `Map<String, Object>` — Input data for workflow execution
    
</dd>
</dl>

<dl>
<dd>

**preview:** `Optional<Boolean>` — If true, create operations return mock resources instead of persisting to the FHIR server
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

