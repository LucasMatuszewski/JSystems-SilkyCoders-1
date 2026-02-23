# MCP Servers & Tools for Searching Java Library Source Code

## Executive Summary

Several MCP servers already exist that give AI agents the ability to search inside Java library source code — most notably **Maven Indexer MCP** and **JAR Indexer MCP**. These tools index local Maven repositories and Gradle caches, allowing agents to search classes, retrieve method signatures, read Javadocs, and access full source code of dependencies like Spring Boot, Spring AI, or any JAR in the local repository. For libraries where source JARs are unavailable, decompilation via CFR provides a fallback. Building a custom MCP server from scratch is also straightforward using Spring AI's MCP starter or the official Java MCP SDK, and the licensing landscape for major Java frameworks (Spring, Apache Commons, Guava) is permissive under Apache 2.0.

***

## Existing MCP Servers for Java Source Code Search

### Maven Indexer MCP (Recommended)

The **maven-indexer-mcp** by tangcent is the most mature and directly relevant solution. It indexes the local Maven repository (`~/.m2/repository`) and Gradle cache (`~/.gradle/caches/modules-2/files-2.1`) to provide AI agents with tools for searching Java classes, method signatures, and source code.[1][2][3][4]

**Key Use Case**: AI models are generally well-versed in popular libraries like Spring or Guava, but they struggle with internal/private packages, niche libraries, or verifying exact versions of APIs. This server bridges that gap by letting the agent "read" local dependencies directly.[4]

**Available Tools**:

| Tool | Purpose | Input |
|------|---------|-------|
| `search_classes` | Find Java classes by name or purpose | `className` (e.g., "StringUtils") |
| `get_class_details` | Decompile and read source code of external library classes | `className` (required), `artifactId` (optional), `type` ("signatures", "docs", "source") |
| `search_artifacts` | Search artifacts by Maven coordinates | groupId, artifactId |
| `search_implementations` | Find all implementations of an interface or subclasses | `className` (e.g., "java.util.List") |
| `refresh_index` | Trigger re-scan of Maven repository | — |

The `get_class_details` tool is the core feature: it retrieves full source code if a `-sources.jar` is available, and falls back to **CFR decompilation** of compiled bytecode when source JARs are missing.[1]

**Installation** is a single line via npx:[4]

```json
{
  "mcpServers": {
    "maven-indexer": {
      "command": "npx",
      "args": ["-y", "maven-indexer-mcp@latest"]
    }
  }
}
```

**Configuration** supports environment variables for customization:[1]

- `MAVEN_REPO` — custom path to local Maven repository
- `GRADLE_REPO_PATH` — custom path to Gradle cache
- `INCLUDED_PACKAGES` — filter which packages to index (e.g., `org.springframework.*`)
- `MAVEN_INDEXER_CFR_PATH` — path to a specific CFR decompiler JAR
- `VERSION_RESOLUTION_STRATEGY` — choose between `semver`, `latest-published`, or `latest-used`

It works with **Cursor, Cline, JetBrains AI Assistant, Junie, Kiro, Claude Desktop**, and any MCP-compatible client.[4]

### JAR Indexer MCP

The **mcp-jar-indexer** by studykit is a Python-based MCP server that indexes JAR files and Git repositories to give LLMs access to Java/Kotlin library source code. It uses a `register_source` tool to add new sources dynamically. While less documented than Maven Indexer, it offers an alternative approach that combines JAR analysis with Git repository indexing.[5][6]

### MCP Java Doc (BeamLiu)

This project takes a different approach: a Maven plugin (`java-docs-json-doclet`) generates structured JSON documentation from Java source code, and an MCP server provides search capabilities over that JSON. It supports three data flows:[7][8][9]

- Direct source code analysis
- HTML Javadoc crawling (for JDK 9+ format)
- Lombok project support via delombok

This is more documentation-focused than raw source code browsing but can be useful for structured API exploration.

### General Code Indexing MCP Servers

Several language-agnostic MCP servers also support Java:

- **Code-Index-MCP** (ViperJuice) — Local-first indexer with 48-language support via tree-sitter, semantic search with Voyage AI, sub-100ms queries[10][11]
- **Git Repo Research MCP** (AWS Labs) — Semantic search on Git repositories using Amazon Bedrock and FAISS embeddings. Can index the Spring Framework GitHub repo directly[12]
- **Docs MCP** (probelabs) — Point to any GitHub repo and make it searchable: `npx -y @probelabs/docs-mcp@latest --gitUrl https://github.com/spring-projects/spring-framework`[13]
- **GitHub MCP Server** (official) — Browse code, search files, and analyze commits across any accessible repository[14]

***

## Comparison of Approaches

| Feature | Maven Indexer MCP | JAR Indexer MCP | Git Repo Research (AWS) | Docs MCP |
|---------|-------------------|-----------------|-------------------------|----------|
| **Source** | Local Maven/Gradle cache | JAR files + Git repos | Git repos (local/remote) | Git repos |
| **Java-specific** | Yes[4] | Yes[5] | No[12] | No[13] |
| **Source code retrieval** | Yes (source JAR + decompilation)[1] | Yes | Via file access | Via search |
| **Semantic search** | By class name/purpose[4] | Via register_source[6] | Amazon Bedrock embeddings[12] | Probe search engine[13] |
| **Decompilation fallback** | CFR built-in[1] | Unknown | No | No |
| **Inheritance search** | Yes[4] | Unknown | No | No |
| **Setup complexity** | Low (npx one-liner)[4] | Medium (Python/uv)[6] | Medium (AWS credentials)[12] | Low (npx)[13] |
| **Runtime** | Node.js | Python | Python | Node.js |

***

## How Source Code Availability Works in Java

### Maven Central Publishing Requirements

Maven Central mandates that all published artifacts include source JARs (`-sources.jar`) and Javadoc JARs (`-javadoc.jar`). This means the vast majority of open-source Java libraries have their source code readily available.[15][16]

To download all source JARs for a project's dependencies:[17][18]

```bash
mvn dependency:sources
mvn dependency:resolve -Dclassifier=javadoc
```

Source JARs are stored alongside compiled JARs in `~/.m2/repository` with the `-sources` classifier. This is exactly what Maven Indexer MCP reads.[4]

### Spring Ecosystem Licensing

All major Spring projects use the **Apache License 2.0**, which is fully permissive for this use case:[19]

- **Spring Framework** — Apache 2.0, full source on GitHub[19]
- **Spring Boot** — Apache 2.0
- **Spring AI** — Apache 2.0[20]
- **Spring AI MCP SDK** — Apache 2.0[20]

The Apache 2.0 license explicitly permits commercial use, modification, distribution, and private use. The only conditions are preserving copyright/license notices and stating changes. Reading source code for AI agent context is well within these permissions.[19]

### When Source Code Is Not Available

Some libraries (proprietary or poorly maintained) may not publish source JARs. In these cases:

- **CFR Decompiler** — Handles modern Java features (Java 9–14+), produces readable output from compiled `.class` files. Maven Indexer MCP has CFR built-in.[21][22][23][1]
- **Procyon** — Another decompiler, particularly good with lambdas, enums, and Java 8+ features. Licensed under Apache 2.0.[24]
- **FernFlower / Vineflower** — JetBrains' decompiler, used in IntelliJ IDEA, also available standalone.

**Legal considerations for decompilation**: In the EU, the Software Directive (2009/24/EC) explicitly allows decompilation for interoperability purposes. In the US, decompilation for interoperability has been upheld under fair use. However, proprietary licenses may include specific anti-reverse-engineering clauses that should be reviewed.

***

## Building a Custom MCP Server from Scratch

If existing solutions don't fully meet the requirements — for example, needing deeper integration with the Spring AI project setup, custom indexing strategies, or embedding-based semantic search — building a custom MCP server is feasible.

### Option 1: Using Spring AI MCP Starter (Recommended for Spring Teams)

Spring AI provides dedicated MCP server starters that make building MCP servers in Java straightforward:[25][26][27]

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

Tools are exposed by annotating methods with `@Tool` and registering them via `MethodToolCallbackProvider`:[28][29]

```java
@Service
public class SourceCodeSearchService {

    @Tool(description = "Search for a Java class by name in project dependencies")
    public List<ClassInfo> searchClass(String className) {
        // Scan ~/.m2/repository for matching classes
    }

    @Tool(description = "Get full source code of a dependency class")
    public String getSourceCode(String className, String artifactId) {
        // Return source from -sources.jar or decompile with CFR
    }

    @Tool(description = "Find implementations of an interface")
    public List<ClassInfo> findImplementations(String interfaceName) {
        // Scan JARs for implementing classes
    }
}

@Bean
public ToolCallbackProvider sourceCodeTools(SourceCodeSearchService service) {
    return MethodToolCallbackProvider.builder()
        .toolObjects(service)
        .build();
}
```

The MCP client side connects to this server automatically:[25]

```properties
spring.ai.mcp.client.toolcallback.enabled=true
spring.ai.mcp.client.streamablehttp.connections.source-search.url=http://localhost:8082
```

### Option 2: Using the MCP Java SDK Directly

For non-Spring projects or minimal setups, the official MCP Java SDK is available. Microsoft also provides a tutorial for building Java-based MCP servers with Quarkus and LangChain4j.[30][31][32]

### Architecture for a Custom Source Code Search MCP Server

A custom server would need these components:

1. **Source Resolution Layer**
   - Scan `~/.m2/repository` and `~/.gradle/caches` for JARs
   - Identify `-sources.jar` files (preferred) vs compiled JARs
   - Parse `pom.xml` files for metadata (groupId, artifactId, version, license)

2. **Indexing Layer**
   - Extract class names, method signatures, and package structures from JARs
   - Use ASM or JavaParser for bytecode/source analysis
   - Optionally create embeddings (via OpenAI, Bedrock, or local models) for semantic search

3. **Source Retrieval Layer**
   - Extract specific files from `-sources.jar` archives
   - Fall back to CFR decompilation for JARs without source
   - Cache decompiled results for performance

4. **MCP Tool Definitions**
   - `search_classes(query)` — keyword/semantic search across indexed classes
   - `get_source(className, artifactId?)` — full source code retrieval
   - `get_method_signatures(className)` — method-level detail
   - `find_implementations(interfaceName)` — inheritance tree search
   - `search_annotations(annotationType)` — find usages of specific annotations (useful for Spring)
   - `get_dependency_tree(artifactId)` — dependency graph exploration

5. **License Awareness Layer** (optional but recommended)
   - Parse license information from POM files
   - Flag proprietary or restrictively-licensed dependencies
   - Provide license metadata alongside source code results

### Handling Closed-Source / Proprietary Libraries

For libraries without open-source licenses:

- **Decompilation for internal use** is generally acceptable, but distributing decompiled code may violate licenses
- The MCP server itself only provides source to the AI agent at runtime — it doesn't redistribute code
- Consider adding a license check that warns the agent when accessing proprietary code
- For truly restricted code, limit the tool to method signatures and Javadoc (which are factual/functional and less restricted)

***

## Practical Recommendations for a Spring Boot Project

### Quick Start (Immediate Solution)

Install Maven Indexer MCP and ensure source JARs are downloaded:

```bash
# In your Spring Boot project
mvn dependency:sources

# Add Maven Indexer MCP to your AI tool (Cursor, Claude, etc.)
# Use the npx configuration shown above
```

Then prompt the agent: *"Find the class `StringUtils` in my local Maven repository and show me its methods"*.[4]

### For Spring-Specific Source Code Exploration

Combine approaches for maximum coverage:

1. **Maven Indexer MCP** for all local dependencies (Spring Boot, Spring AI, etc.)[1]
2. **Docs MCP** pointed at Spring's GitHub repos for browsing the latest source[13]
3. **GitHub MCP Server** for searching across the entire Spring ecosystem on GitHub[14]

### For Building a Custom Solution

If existing servers lack features needed (e.g., Spring annotation-aware search, custom semantic indexing):

- Use **Spring AI MCP Server Starter** to build the server in Java[26][27]
- Use **JavaParser** for source-level AST analysis
- Use **ASM** for bytecode-level analysis
- Use **CFR** for decompilation fallback[22]
- Expose via Streamable HTTP transport for easy integration with any MCP client[25])
