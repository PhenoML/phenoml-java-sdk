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
        .isActive(true)
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

**name:** `String` — Agent name
    
</dd>
</dl>

<dl>
<dd>

**description:** `Optional<String>` — Agent description
    
</dd>
</dl>

<dl>
<dd>

**prompts:** `List<String>` — Array of prompt IDs to use for this agent
    
</dd>
</dl>

<dl>
<dd>

**isActive:** `Boolean` — Whether the agent is active
    
</dd>
</dl>

<dl>
<dd>

**tags:** `Optional<List<String>>` — Tags for categorizing the agent
    
</dd>
</dl>

<dl>
<dd>

**provider:** `Optional<AgentProvider>` — FHIR provider type - can be a single provider or array of providers
    
</dd>
</dl>

<dl>
<dd>

**meta:** `Optional<AgentFhirConfig>` 
    
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

**isActive:** `Optional<Boolean>` — Filter by active status
    
</dd>
</dl>

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
    AgentUpdateRequest
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

**id:** `String` — Agent ID
    
</dd>
</dl>

<dl>
<dd>

**name:** `Optional<String>` — Agent name
    
</dd>
</dl>

<dl>
<dd>

**description:** `Optional<String>` — Agent description
    
</dd>
</dl>

<dl>
<dd>

**prompts:** `Optional<List<String>>` — Array of prompt IDs to use for this agent
    
</dd>
</dl>

<dl>
<dd>

**isActive:** `Optional<Boolean>` — Whether the agent is active
    
</dd>
</dl>

<dl>
<dd>

**tags:** `Optional<List<String>>` — Tags for categorizing the agent
    
</dd>
</dl>

<dl>
<dd>

**provider:** `Optional<AgentProvider>` — FHIR provider type - can be a single provider or array of providers
    
</dd>
</dl>

<dl>
<dd>

**meta:** `Optional<AgentFhirConfig>` 
    
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

<dl>
<dd>

**meta:** `Optional<ChatFhirClientConfig>` — Optional user-specific FHIR configuration overrides
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Agent Prompts
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
        .isActive(true)
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

**isActive:** `Boolean` — Whether the prompt is active
    
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

**isActive:** `Optional<Boolean>` — Whether the prompt is active
    
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

Soft deletes a prompt by setting is_active to false
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
        .identity("identity")
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

**identity:** `String` — The user's username or email
    
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

Upload a custom medical code system with codes and descriptions for use in code extraction.
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
    UploadRequest
        .builder()
        .name("CUSTOM_CODES")
        .version("1.0")
        .format(UploadRequestFormat.JSON)
        .file("file")
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

**name:** `String` — Name of the code system
    
</dd>
</dl>

<dl>
<dd>

**version:** `String` — Version of the code system
    
</dd>
</dl>

<dl>
<dd>

**revision:** `Optional<Float>` — Optional revision number
    
</dd>
</dl>

<dl>
<dd>

**format:** `UploadRequestFormat` — Format of the uploaded file
    
</dd>
</dl>

<dl>
<dd>

**file:** `String` — The file contents as a base64-encoded string
    
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

Converts natural language text into structured medical codes
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

<details><summary><code>client.construe.cohort(request) -> ConstrueCohortResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Creates a patient cohort based on a natural language description.
Translates the description into FHIR search queries and optional SQL queries.
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
client.construe().cohort(
    ConstrueCohortRequest
        .builder()
        .text("Between 20 and 40 years old with hyperlipidemia")
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

**config:** `Optional<ConstrueCohortRequestConfig>` 
    
</dd>
</dl>

<dl>
<dd>

**text:** `String` — Natural language description of the desired patient cohort.
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

## Lang2Fhir
<details><summary><code>client.lang2Fhir.create(request) -> Map&lt;String, Object&gt;</code></summary>
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

<details><summary><code>client.lang2Fhir.search(request) -> SearchResponse</code></summary>
<dl>
<dd>

#### 📝 Description

<dl>
<dd>

<dl>
<dd>

Converts natural language text into FHIR search parameters
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

**text:** `String` — Natural language text to convert into FHIR search parameters
    
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

Upload a custom FHIR StructureDefinition profile for use with the lang2fhir service
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
        .version("version")
        .resource("custom-patient")
        .profile("profile")
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

**version:** `String` — FHIR version that this profile implements
    
</dd>
</dl>

<dl>
<dd>

**resource:** `String` — Name for the custom resource profile (will be converted to lowercase)
    
</dd>
</dl>

<dl>
<dd>

**profile:** `String` — Base64 encoded JSON string of the FHIR StructureDefinition profile
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>

<details><summary><code>client.lang2Fhir.document(request) -> Map&lt;String, Object&gt;</code></summary>
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
        .fileType(DocumentRequestFileType.APPLICATION_PDF)
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

**content:** `String` — Base64 encoded file content
    
</dd>
</dl>

<dl>
<dd>

**fileType:** `DocumentRequestFileType` — MIME type of the file
    
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

**provider:** `Optional<Lang2FhirAndCreateRequestProvider>` — FHIR provider to use for storing the resource
    
</dd>
</dl>

<dl>
<dd>

**meta:** `Optional<FhirClientConfig>` 
    
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

**provider:** `Optional<Lang2FhirAndSearchRequestProvider>` — FHIR provider to use for searching
    
</dd>
</dl>

<dl>
<dd>

**meta:** `Optional<FhirClientConfig>` 
    
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
        .provider(CohortRequestProvider.MEDPLUM)
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

**text:** `String` — Natural language text describing the patient cohort criteria
    
</dd>
</dl>

<dl>
<dd>

**provider:** `CohortRequestProvider` — FHIR provider to use for searching
    
</dd>
</dl>

<dl>
<dd>

**meta:** `Optional<FhirClientConfig>` 
    
</dd>
</dl>
</dd>
</dl>


</dd>
</dl>
</details>
