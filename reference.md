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
        .name("name")
        .prompts(
            new ArrayList<String>(
                Arrays.asList("prompt_123", "prompt_456")
            )
        )
        .provider(
            AgentCreateRequestProvider.of("provider")
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

<details><summary><code>client.agent.list() -> AgentListResponse</code></summary>
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
    AgentListRequest
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
        .name("name")
        .prompts(
            new ArrayList<String>(
                Arrays.asList("prompt_123", "prompt_456")
            )
        )
        .provider(
            AgentCreateRequestProvider.of("provider")
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

<details><summary><code>client.agent.delete(id) -> AgentDeleteResponse</code></summary>
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
    new ArrayList<JsonPatchOperation>(
        Arrays.asList(
            JsonPatchOperation
                .builder()
                .op(JsonPatchOperationOp.REPLACE)
                .path("/name")
                .value("Updated Agent Name")
                .build(),
            JsonPatchOperation
                .builder()
                .op(JsonPatchOperationOp.ADD)
                .path("/tags/-")
                .value("new-tag")
                .build(),
            JsonPatchOperation
                .builder()
                .op(JsonPatchOperationOp.REMOVE)
                .path("/description")
                .build()
        )
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

<details><summary><code>client.agent.chat(request) -> AgentChatResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Send a message to an agent and receive a response
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
client.agent().chat(
    AgentChatRequest
        .builder()
        .message("What is the patient's current condition?")
        .agentId("agent-123")
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

**sessionId:** `Optional<String>` — Optional session ID for conversation continuity
    
</dd>
</dl>

<dl>
<dd>

**agentId:** `String` — The ID of the agent to chat with
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.agent.getChatMessages() -> AgentGetChatMessagesResponse</code></summary>
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
client.agent().getChatMessages(
    AgentGetChatMessagesRequest
        .builder()
        .chatSessionId("chat_session_id")
        .numMessages(1)
        .role(AgentGetChatMessagesRequestRole.USER)
        .order(AgentGetChatMessagesRequestOrder.ASC)
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

**role:** `Optional<AgentGetChatMessagesRequestRole>` 

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

**order:** `Optional<AgentGetChatMessagesRequestOrder>` — Order of messages
    
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
        .content("You are a helpful medical assistant specialized in FHIR data processing...")
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
    new ArrayList<JsonPatchOperation>(
        Arrays.asList(
            JsonPatchOperation
                .builder()
                .op(JsonPatchOperationOp.REPLACE)
                .path("/name")
                .value("Updated Agent Name")
                .build(),
            JsonPatchOperation
                .builder()
                .op(JsonPatchOperationOp.ADD)
                .path("/tags/-")
                .value("new-tag")
                .build(),
            JsonPatchOperation
                .builder()
                .op(JsonPatchOperationOp.REMOVE)
                .path("/description")
                .build()
        )
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

<details><summary><code>client.agent.prompts.loadDefaults() -> SuccessResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Loads default agent prompts for the authenticated user
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
client.agent().prompts().loadDefaults();
```
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Authtoken Auth
<details><summary><code>client.authtoken.auth.generateToken(request) -> AuthGenerateTokenResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Obtain an access token using client credentials
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
client.authtoken().auth().generateToken(
    AuthGenerateTokenRequest
        .builder()
        .username("username")
        .password("password")
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

**username:** `String` — The user's username or email
    
</dd>
</dl>

<dl>
<dd>

**password:** `String` — The user's password
    
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

## Construe
<details><summary><code>client.construe.uploadCodeSystem(request) -> ConstrueUploadCodeSystemResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Upload a custom medical code system with codes and descriptions for use in code extraction. Requires a paid plan.
Upon upload, construe generates embeddings for all of the codes in the code system and stores them in the vector database so you can
subsequently use the code system for construe/extract and lang2fhir/create (coming soon!)
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
client.construe().uploadCodeSystem(
    UploadRequest.csv(
        UploadRequestCsv
            .builder()
            .name("CUSTOM_CODES")
            .version("1.0")
            .file("file")
            .codeCol("code")
            .descCol("description")
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

**request:** `UploadRequest` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.extractCodes(request) -> ExtractCodesResult</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Converts natural language text into structured medical codes.

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
client.construe().extractCodes(
    ExtractRequest
        .builder()
        .text("Patient is a 14-year-old female, previously healthy, who is here for evaluation of abnormal renal ultrasound with atrophic right kidney")
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

<details><summary><code>client.construe.listAvailableCodeSystems() -> ListCodeSystemsResponse</code></summary>
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
client.construe().listAvailableCodeSystems();
```
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.getCodeSystemDetail(codesystem) -> GetCodeSystemDetailResponse</code></summary>
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
client.construe().getCodeSystemDetail(
    "ICD-10-CM",
    GetConstrueCodesSystemsCodesystemRequest
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

**version:** `Optional<String>` — Specific version of the code system. Required if multiple versions exist.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.deleteCustomCodeSystem(codesystem) -> DeleteCodeSystemResponse</code></summary>
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
client.construe().deleteCustomCodeSystem(
    "CUSTOM_CODES",
    DeleteConstrueCodesSystemsCodesystemRequest
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

**version:** `Optional<String>` — Specific version of the code system. Required if multiple versions exist.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.listCodesInACodeSystem(codesystem) -> ListCodesResponse</code></summary>
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
client.construe().listCodesInACodeSystem(
    "ICD-10-CM",
    GetConstrueCodesCodesystemRequest
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

**version:** `Optional<String>` — Specific version of the code system. Required if multiple versions exist.
    
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

<details><summary><code>client.construe.getASpecificCode(codesystem, codeId) -> GetCodeResponse</code></summary>
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
client.construe().getASpecificCode(
    "ICD-10-CM",
    "E11.65",
    GetConstrueCodesCodesystemCodeIdRequest
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

**codeId:** `String` — The code identifier
    
</dd>
</dl>

<dl>
<dd>

**version:** `Optional<String>` — Specific version of the code system
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.construe.semanticSearchEmbeddingBased(codesystem) -> SemanticSearchResponse</code></summary>
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
client.construe().semanticSearchEmbeddingBased(
    "ICD-10-CM",
    GetConstrueCodesCodesystemSearchSemanticRequest
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

**version:** `Optional<String>` — Specific version of the code system
    
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

<details><summary><code>client.construe.terminologyServerTextSearch(codesystem) -> TextSearchResponse</code></summary>
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
client.construe().terminologyServerTextSearch(
    "ICD-10-CM",
    GetConstrueCodesCodesystemSearchTextRequest
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

**version:** `Optional<String>` — Specific version of the code system
    
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
<details><summary><code>client.fhir.search(fhirProviderId, fhirPath) -> FhirSearchResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Retrieves FHIR resources from the specified provider. Supports both individual resource retrieval and search operations based on the FHIR path and query parameters.

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
    FhirSearchRequest
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

**queryParameters:** `Optional<Map<String, Optional<String>>>` 

FHIR-compliant query parameters for search operations. Supports standard FHIR search parameters including:
- Resource-specific search parameters (e.g., name for Patient, status for Observation)
- Common search parameters (_id, _lastUpdated, _tag, _profile, _security, _text, _content, _filter)
- Result parameters (_count, _offset, _sort, _include, _revinclude, _summary, _elements)
- Search prefixes for dates, numbers, quantities (eq, ne, gt, ge, lt, le, sa, eb, ap)
    
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

<details><summary><code>client.fhir.create(fhirProviderId, fhirPath, request) -> FhirResource</code></summary>
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
    FhirCreateRequest
        .builder()
        .body(
            FhirResource
                .builder()
                .resourceType("Patient")
                .build()
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

**request:** `FhirResource` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhir.upsert(fhirProviderId, fhirPath, request) -> FhirResource</code></summary>
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
    FhirUpsertRequest
        .builder()
        .body(
            FhirResource
                .builder()
                .resourceType("Patient")
                .id("123")
                .build()
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

**request:** `FhirResource` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhir.delete(fhirProviderId, fhirPath) -> Map&amp;lt;String, Object&amp;gt;</code></summary>
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
    FhirDeleteRequest
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

<details><summary><code>client.fhir.patch(fhirProviderId, fhirPath, request) -> FhirResource</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Partially updates a FHIR resource on the specified provider using JSON Patch operations as defined in RFC 6902.

The request body should contain an array of JSON Patch operations. Each operation specifies:
- `op`: The operation type (add, remove, replace, move, copy, test)
- `path`: JSON Pointer to the target location in the resource
- `value`: The value to use (required for add, replace, and test operations)

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
    FhirPatchRequest
        .builder()
        .body(
            new ArrayList<FhirPatchRequestBodyItem>(
                Arrays.asList(
                    FhirPatchRequestBodyItem
                        .builder()
                        .op(FhirPatchRequestBodyItemOp.REPLACE)
                        .path("/name/0/family")
                        .value("NewFamilyName")
                        .build()
                )
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

**request:** `List<FhirPatchRequestBodyItem>` — Array of JSON Patch operations following RFC 6902
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhir.executeBundle(fhirProviderId, request) -> FhirBundle</code></summary>
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
    FhirExecuteBundleRequest
        .builder()
        .body(
            FhirBundle
                .builder()
                .resourceType("Bundle")
                .entry(
                    new ArrayList<FhirBundleEntryItem>(
                        Arrays.asList(
                            FhirBundleEntryItem
                                .builder()
                                .resource(
                                    new HashMap<String, Object>() {{
                                        put("resourceType", "Patient");
                                        put("name", new
                                        ArrayList<Object>() {Arrays.asList(new 
                                            HashMap<String, Object>() {{put("family", "Doe");
                                                put("given", new
                                                ArrayList<Object>() {Arrays.asList("John")
                                                });
                                            }})
                                        });
                                    }}
                                )
                                .request(
                                    FhirBundleEntryItemRequest
                                        .builder()
                                        .method(FhirBundleEntryItemRequestMethod.POST)
                                        .url("Patient")
                                        .build()
                                )
                                .build(),
                            FhirBundleEntryItem
                                .builder()
                                .resource(
                                    new HashMap<String, Object>() {{
                                        put("resourceType", "Observation");
                                        put("status", "final");
                                        put("subject", new 
                                        HashMap<String, Object>() {{put("reference", "Patient/123");
                                        }});
                                    }}
                                )
                                .request(
                                    FhirBundleEntryItemRequest
                                        .builder()
                                        .method(FhirBundleEntryItemRequestMethod.POST)
                                        .url("Observation")
                                        .build()
                                )
                                .build()
                        )
                    )
                )
                .build()
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

**request:** `FhirBundle` 
    
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
        .provider(Provider.ATHENAHEALTH)
        .authMethod(AuthMethod.CLIENT_SECRET)
        .baseUrl("https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/R4")
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

**authMethod:** `AuthMethod` 
    
</dd>
</dl>

<dl>
<dd>

**baseUrl:** `String` — Base URL of the FHIR server
    
</dd>
</dl>

<dl>
<dd>

**clientId:** `Optional<String>` — OAuth client ID (required for most auth methods)
    
</dd>
</dl>

<dl>
<dd>

**clientSecret:** `Optional<String>` — OAuth client secret (required for client_secret and on_behalf_of auth methods)
    
</dd>
</dl>

<dl>
<dd>

**serviceAccountKey:** `Optional<ServiceAccountKey>` 
    
</dd>
</dl>

<dl>
<dd>

**role:** `Optional<Role>` 
    
</dd>
</dl>

<dl>
<dd>

**scopes:** `Optional<String>` — OAuth scopes to request. Cannot be specified with role. If neither role nor scopes are specified, the provider-specific default role will be used. You are solely responsible for ensuring the scopes are valid options for the provider being created or updated.
    
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

<details><summary><code>client.fhirProvider.delete(fhirProviderId) -> FhirProviderDeleteResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Soft deletes a FHIR provider by setting is_active to false.

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

<details><summary><code>client.fhirProvider.addAuthConfig(fhirProviderId, request) -> FhirProviderResponse</code></summary>
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
client.fhirProvider().addAuthConfig(
    "1716d214-de93-43a4-aa6b-a878d864e2ad",
    FhirProviderAddAuthConfigRequest
        .builder()
        .authMethod(AuthMethod.CLIENT_SECRET)
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

**fhirProviderId:** `String` — ID of the FHIR provider to add auth config to
    
</dd>
</dl>

<dl>
<dd>

**authMethod:** `AuthMethod` 
    
</dd>
</dl>

<dl>
<dd>

**clientSecret:** `Optional<String>` — OAuth client secret (required for client_secret and on_behalf_of auth methods)
    
</dd>
</dl>

<dl>
<dd>

**serviceAccountKey:** `Optional<ServiceAccountKey>` 
    
</dd>
</dl>

<dl>
<dd>

**credentialExpiry:** `Optional<OffsetDateTime>` — Expiry time for JWT credentials (only applicable for JWT auth method)
    
</dd>
</dl>

<dl>
<dd>

**role:** `Optional<Role>` 
    
</dd>
</dl>

<dl>
<dd>

**scopes:** `Optional<String>` — OAuth scopes to request. Cannot be specified with role. If neither role nor scopes are specified, the provider-specific default role will be used. You are solely responsible for ensuring the scopes are valid options for the provider being created or updated.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.fhirProvider.setActiveAuthConfig(fhirProviderId, request) -> FhirProviderResponse</code></summary>
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
client.fhirProvider().setActiveAuthConfig(
    "1716d214-de93-43a4-aa6b-a878d864e2ad",
    FhirProviderSetActiveAuthConfigRequest
        .builder()
        .authConfigId("auth-config-123")
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

<details><summary><code>client.fhirProvider.removeAuthConfig(fhirProviderId, request) -> FhirProviderRemoveAuthConfigResponse</code></summary>
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
client.fhirProvider().removeAuthConfig(
    "1716d214-de93-43a4-aa6b-a878d864e2ad",
    FhirProviderRemoveAuthConfigRequest
        .builder()
        .authConfigId("auth-config-123")
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

## Lang2Fhir
<details><summary><code>client.lang2Fhir.create(request) -> Map&amp;lt;String, Object&amp;gt;</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Converts natural language text into a structured FHIR resource
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
        .resource(CreateRequestResource.AUTO)
        .text("Patient has severe asthma with acute exacerbation")
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
        .text("John Smith, 45-year-old male, diagnosed with Type 2 Diabetes. Prescribed Metformin 500mg twice daily.")
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

<details><summary><code>client.lang2Fhir.uploadProfile(request) -> Lang2FhirUploadProfileResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

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
        .profile("(base64 encoded FHIR StructureDefinition JSON)")
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

**profile:** `String` — Base64 encoded JSON string of a FHIR StructureDefinition. The profile must include id, url, type, and a snapshot with elements. All metadata (version, resource type, identifier) is derived from the StructureDefinition itself. The lowercase id from the StructureDefinition becomes the profile's lookup key.
    
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

Extracts text from a document (PDF or image) and converts it into a structured FHIR resource
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
        .resource(DocumentRequestResource.QUESTIONNAIRE)
        .content("content")
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

**resource:** `DocumentRequestResource` — Type of FHIR resource to create (questionnaire and US Core questionnaireresponse profiles currently supported)
    
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
</dd>
</dl>


</dd>
</dl>
</details>

## Summary
<details><summary><code>client.summary.listTemplates() -> SummaryListTemplatesResponse</code></summary>
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
client.summary().listTemplates();
```
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.summary.createTemplate(request) -> CreateSummaryTemplateResponse</code></summary>
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
client.summary().createTemplate(
    CreateSummaryTemplateRequest
        .builder()
        .name("name")
        .exampleSummary("Patient John Doe, age 45, presents with hypertension diagnosed on 2024-01-15.")
        .targetResources(
            new ArrayList<String>(
                Arrays.asList("Patient", "Condition", "Observation")
            )
        )
        .mode("mode")
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

<details><summary><code>client.summary.getTemplate(id) -> SummaryGetTemplateResponse</code></summary>
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
client.summary().getTemplate("id");
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

<details><summary><code>client.summary.updateTemplate(id, request) -> SummaryUpdateTemplateResponse</code></summary>
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
client.summary().updateTemplate(
    "id",
    UpdateSummaryTemplateRequest
        .builder()
        .name("name")
        .template("template")
        .targetResources(
            new ArrayList<String>(
                Arrays.asList("target_resources")
            )
        )
        .mode("mode")
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

<details><summary><code>client.summary.deleteTemplate(id) -> SummaryDeleteTemplateResponse</code></summary>
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
client.summary().deleteTemplate("id");
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
            CreateSummaryRequestFhirResources.ofFhirResource(
                FhirResource
                    .builder()
                    .resourceType("resourceType")
                    .build()
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

**fhirResources:** `CreateSummaryRequestFhirResources` 

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
        .resource(Lang2FhirAndCreateRequestResource.AUTO)
        .text("Patient John Doe has severe asthma with acute exacerbation")
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

**practitionerId:** `Optional<String>` — Practitioner ID to filter results
    
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

## Tools McpServer
<details><summary><code>client.tools.mcpServer.create(request) -> McpServerResponse</code></summary>
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
client.tools().mcpServer().create(
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

<details><summary><code>client.tools.mcpServer.list() -> McpServerResponse</code></summary>
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
client.tools().mcpServer().list();
```
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.tools.mcpServer.get(mcpServerId) -> McpServerResponse</code></summary>
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
client.tools().mcpServer().get("mcp_server_id");
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

<details><summary><code>client.tools.mcpServer.delete(mcpServerId) -> McpServerResponse</code></summary>
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
client.tools().mcpServer().delete("mcp_server_id");
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

## Tools McpServer Tools
<details><summary><code>client.tools.mcpServer.tools.list(mcpServerId) -> McpServerToolResponse</code></summary>
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
client.tools().mcpServer().tools().list("mcp_server_id");
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

<details><summary><code>client.tools.mcpServer.tools.get(mcpServerToolId) -> McpServerToolResponse</code></summary>
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
client.tools().mcpServer().tools().get("mcp_server_tool_id");
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

<details><summary><code>client.tools.mcpServer.tools.delete(mcpServerToolId) -> McpServerToolResponse</code></summary>
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
client.tools().mcpServer().tools().delete("mcp_server_tool_id");
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

<details><summary><code>client.tools.mcpServer.tools.call(mcpServerToolId, request) -> McpServerToolCallResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Calls a MCP server tool
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
client.tools().mcpServer().tools().call(
    "mcp_server_tool_id",
    McpServerToolCallRequest
        .builder()
        .arguments(
            new HashMap<String, Object>() {{
                put("title", "PhenoML Agent API");
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

**mcpServerToolId:** `String` — ID of the MCP server tool to call
    
</dd>
</dl>

<dl>
<dd>

**arguments:** `Map<String, Object>` — Arguments to pass to the MCP server tool
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

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
    WorkflowsListRequest
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
        .workflowInstructions("Given diagnosis data, find the patient and create condition record")
        .sampleData(
            new HashMap<String, Object>() {{
                put("patient_last_name", "Rippin");
                put("patient_first_name", "Clay");
                put("diagnosis_code", "I10");
            }}
        )
        .fhirProviderId(
            CreateWorkflowRequestFhirProviderId.of("550e8400-e29b-41d4-a716-446655440000")
        )
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

<details><summary><code>client.workflows.get(id) -> WorkflowsGetResponse</code></summary>
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
    WorkflowsGetRequest
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

<details><summary><code>client.workflows.update(id, request) -> WorkflowsUpdateResponse</code></summary>
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
        .name("Updated Patient Data Mapping Workflow")
        .workflowInstructions("Given diagnosis data, find the patient and create condition record")
        .sampleData(
            new HashMap<String, Object>() {{
                put("patient_last_name", "Smith");
                put("patient_first_name", "John");
                put("diagnosis_code", "E11");
            }}
        )
        .fhirProviderId(
            UpdateWorkflowRequestFhirProviderId.of("550e8400-e29b-41d4-a716-446655440000")
        )
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

<details><summary><code>client.workflows.delete(id) -> WorkflowsDeleteResponse</code></summary>
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
    "id",
    ExecuteWorkflowRequest
        .builder()
        .inputData(
            new HashMap<String, Object>() {{
                put("patient_last_name", "Johnson");
                put("patient_first_name", "Mary");
                put("diagnosis_code", "M79.3");
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

**id:** `String` — ID of the workflow to execute
    
</dd>
</dl>

<dl>
<dd>

**inputData:** `Map<String, Object>` — Input data for workflow execution
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>
