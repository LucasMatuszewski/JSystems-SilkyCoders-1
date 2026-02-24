# RAG with re-ranking and hybrid search in Java-based frameworks.

## Technology overview

**OpenAI Java SDK**

- The official OpenAI API supports embeddings, chat/completions and tools, but it does not provide a built‑in RAG or hybrid search layer—you implement retrieval, storage and ranking yourself in Java using the SDK’s embeddings and chat endpoints.[^1_7]
- Embeddings are vectors of floats capturing semantic similarity, typically used for search, clustering and RAG; OpenAI’s embeddings documentation explicitly cites search and ranking by relevance to a query as primary use‑cases.[^1_7]

**Spring AI**

- Spring AI adds higher‑level abstractions around LLMs (including OpenAI), embeddings and retrieval, including a “Retrieval Augmented Generation” module (retrievers, vector stores, document search interfaces) integrated into the Spring ecosystem.[^1_3][^1_8]
- Spring AI supports pluggable vector stores (MongoDB Atlas, PostgreSQL/pgvector, etc.) and defines retrieval modules that query vector stores and return `Document` objects to a `ChatClient` or “RAG” service; hybrid search support is being discussed and partially experimented with but is not yet a first‑class, turnkey feature as of the hybrid search GitHub issue.[^1_9][^1_10][^1_3]

**sqlite‑vec / SQLite‑Vector**

- `sqlite-vec` is a C extension that adds vector search via `vec0` virtual tables to SQLite; vectors are stored as `float`, `int8` or binary arrays and can be queried with k‑NN style similarity functions.[^1_5][^1_6]
- Tutorials show using `sqlite-vec` in Java via the standard `org.xerial:sqlite-jdbc` driver plus `LOAD_EXTENSION` SQL, enabling fully embedded vector search in a local SQLite database from Java code without a separate vector DB server.[^1_4][^1_5]
- SQLite‑Vector (sqliteai/sqlite-vector) is an alternative extension that stores vectors as BLOBs in ordinary tables and offers highly optimized distance functions for vector search in edge/embedded scenarios.[^1_11][^1_12]

**Chroma**

- Chroma is an open‑source vector database with HTTP APIs and client libraries; it can run locally or in a server mode on your machine.[^1_13]
- Java client libraries for Chroma exist (e.g. `amikos-tech/chromadb-java-client`, `acepero13/chromadb-client`), exposing features such as creating collections, adding documents, generating embeddings via OpenAI or sentence‑transformers, and querying by similarity from Java applications.[^1_14][^1_15][^1_16]

**Other Java RAG frameworks**

- LangChain4j wraps OpenAI and other models and provides RAG abstractions (embedding model, embedding store, content retriever, chat model) to build Java RAG apps; tutorials explicitly use OpenAI embeddings and an embedding store to implement RAG.[^1_17]
- RAG4J is a dedicated Java RAG framework with modular components for indexing, embedding, retrieval strategies (TopN, Window, Hierarchical), content stores and QA services; it’s designed as a didactic and extensible base for RAG projects.[^1_18]

***

## RAG with OpenAI Java SDK

You treat the OpenAI Java SDK as your “LLM + embeddings” backend and build the rest in plain Java (or with helper libs like LangChain4j/RAG4J).

### Core pipeline steps

1. **Ingestion \& chunking**
    - Parse PDFs, DOCX, HTML and text into normalized text blocks; use headings, numbered steps and sections in policy/procedure docs as natural chunk boundaries to preserve structure.
    - For clothing‑retail documentation (complaint procedures, store regulations, pricing policies), chunk by: section heading → paragraphs → bullet lists, with overlaps of 50–100 tokens to avoid cutting in the middle of procedural steps.
2. **Embedding generation (OpenAI)**
    - Use the OpenAI embeddings API to convert each chunk into a vector.[^1_7]
    - Store the vector plus metadata (document id, section title, policy type, effective date, jurisdiction, etc.) into sqlite‑vec, SQLite‑Vector or Chroma.[^1_6][^1_4][^1_11][^1_5]
3. **Hybrid retrieval**
    - At query time, run:
        - A **keyword/BM25 search** over an inverted index (e.g. SQLite FTS5, PostgreSQL full‑text, or a search engine) using the raw text (complaint type, reference to policy ID, regulation number, etc.).
        - A **vector search** over the embedding table (sqlite‑vec / SQLite‑Vector or Chroma) using the embedded query vector.[^1_2][^1_1][^1_5][^1_6]
    - Merge the two result sets with a reciprocal rank fusion (RRF) or weighted score scheme, as done in Azure AI Search and Spanner hybrid search: each result gets a rank from text search and vector search and the combined score is computed using an RRF‑like function.[^1_1][^1_2]
4. **Re‑ranking layer**

Common re‑ranking strategies compatible with a Java + OpenAI stack:
    - **Multi‑Vector Scoring (cheap, local)**
        - Combine: keyword score, cosine similarity score, and metadata features (e.g. match on policy id, store region) in a heuristic or learned linear model.[^1_2][^1_1]
    - **Cross‑encoder / LLM re‑ranker (expensive, precise)**
        - For the top N candidates (e.g. 20–50), call a re‑ranking model:
            - Use OpenAI chat/completion or another LLM with a prompt like “Rate from 0–1 how well this passage answers the query about store return policy,” once per passage, and sort by score.[^1_7]
            - Or call a third‑party re‑rank API (Cohere, Jina, etc.) from Java via HTTP, passing query + candidate texts and obtaining new relevance scores.
    - **Domain‑specific rules**
        - Boost passages that explicitly mention the exact policy code, complaint type, or regulation identifier (e.g. “Complaint Procedure 3.2”) for high‑precision queries.
5. **Answer generation**
    - Build a system prompt that explicitly instructs the model to answer **only based on provided context**, quote relevant policy snippets, and admit when the answer is not present.
    - Feed the top K re‑ranked chunks as context (e.g. as a list of “documents” with titles and section numbers) to a chat completion call.[^1_7]
    - For step‑by‑step guidance queries (“How should I handle a return without receipt?”), prompt the model to extract procedural steps from the retrieved chunks and format them as numbered steps.

### Example: minimal OpenAI + sqlite‑vec retrieval

Sketch of how this might look (simplified, pseudo‑Java):

```java
// 1. Embed query with OpenAI
Embedding queryEmbedding = openAiEmbeddings.createEmbedding("customer returned worn item without receipt ...");

// 2. Keyword search in SQLite FTS table
List<SearchResult> keywordHits = keywordSearchDao.search("worn item without receipt");

// 3. Vector search in sqlite-vec virtual table
List<VectorHit> vectorHits = sqliteVecDao.knnSearch(queryEmbedding.getVector(), 100);

// 4. Fuse and re-rank
List<RagCandidate> fused = reranker.fuseAndRerank(keywordHits, vectorHits, queryText);

// 5. Build context for answer
List<String> topPassages = fused.stream().limit(8).map(RagCandidate::getPassage).toList();

String answer = openAiChat.generateAnswer(queryText, topPassages);
```

The concrete SQLite queries for `sqlite-vec` follow patterns from the extension docs: `CREATE VIRTUAL TABLE ... USING vec0(...)` and then a k‑NN query using `ORDER BY` on a distance function or a helper function like `vec_search`.[^1_19][^1_5][^1_6]

***

## RAG with Spring AI

Spring AI gives you higher‑level primitives for RAG and integrates nicely with Spring Boot.

### Spring AI RAG components

- The Spring AI RAG reference describes **retrieval modules** that query vector stores and return documents; these are combined with the generation modules (LLM clients) to implement RAG.[^1_3]
- Spring AI provides dedicated APIs for embeddings using the official OpenAI Java SDK behind the scenes, exposing an `EmbeddingClient` configured with OpenAI models.[^1_8]

Typical Spring AI building blocks for RAG:

- `EmbeddingClient` – encapsulates OpenAI or another embedding service.[^1_8]
- `VectorStore` – abstraction over a concrete vector database (MongoDB Atlas search, pgvector, etc.).[^1_10][^1_3]
- `DocumentRetriever` – retrieves documents from the `VectorStore` (and, in a custom implementation, from a keyword store as well).[^1_3]
- `ChatClient` or `RagClient` – orchestrates retrieval then calls the LLM.[^1_3]


### Spring AI RAG flow (for policies)

For your clothing‑retail policy RAG app:

1. **Configure OpenAI in Spring AI**
    - Use the OpenAI embeddings integration in Spring AI (`OpenAIEmbeddingModel` / `EmbeddingClient`) as described in the OpenAI SDK embeddings reference.[^1_8]
    - Set model, API key and other parameters via Spring Boot configuration (`application.yml` / `application.properties`).[^1_8]
2. **Implement or plug a `VectorStore`**
    - For production, the reference and example articles show vector stores like **MongoDB Atlas** (with vector search) or PostgreSQL with pgvector used via Spring AI’s vector store abstraction.[^1_10][^1_3]
    - For local vector DBs that Spring AI does not natively support (sqlite‑vec, Chroma), you create a **custom `VectorStore` implementation** that wraps your Java client (sqlite‑vec via JDBC, Chroma via HTTP).[^1_15][^1_16][^1_4][^1_5][^1_3]
3. **Hybrid retrieval in Spring AI**
    - There is an open Spring AI issue proposing hybrid search that combines full‑text and vector retrieval, especially for PostgreSQL; it underlines that hybrid search isn’t yet a built‑in feature and requires manual composition.[^1_9]
    - You can implement a `HybridDocumentRetriever` bean that:
        - Queries a full‑text index (e.g. PostgreSQL FTS, Elasticsearch, or SQLite FTS via a repository) for keyword matches.
        - Calls your `VectorStore` for semantic neighbors.
        - Merges and re‑ranks results (RRF or weighted scoring) and returns a final ordered list of `Document`s.[^1_1][^1_2][^1_9]
4. **RAG endpoint**
    - Implement a Spring MVC or WebFlux controller that accepts the user question, calls the Spring AI `ChatClient` or a custom RAG service, which internally calls `HybridDocumentRetriever`, then passes the re‑ranked documents as context to an LLM call.[^1_20][^1_10][^1_3]
    - The InfoQ article on building a RAG app with Spring Boot, Spring AI and MongoDB Atlas shows this pattern—documents ingested into MongoDB, embeddings stored, then Spring AI’s retrieval and generation pipeline used to answer questions; you can mirror that with sqlite‑vec or Chroma as the backing store.[^1_10]

***

## Hybrid search technical approaches in Java

Hybrid search means combining dense vector retrieval with classic keyword retrieval in a single pipeline.

### Parallel text + vector queries

- Azure AI Search and Spanner hybrid search articles describe a pattern where full‑text search (BM25) and vector search run in parallel, then results are merged via Reciprocal Rank Fusion (RRF).[^1_2][^1_1]
- You can replicate this pattern in Java:

1. Embed the query.[^1_7]
2. Run full‑text search using:
        - SQLite FTS5 (for local SQLite).
        - PostgreSQL `tsvector` / `tsquery`.
        - Lucene/Elasticsearch/OpenSearch.
3. Run vector search in sqlite‑vec / SQLite‑Vector / Chroma using the query embedding.[^1_4][^1_11][^1_5][^1_6]
4. Normalize ranks and combine using RRF or a weighted average of scores, similar to Azure AI Search guidance.[^1_1][^1_2]


### When hybrid helps for policies

- For queries like “What is the maximum refund amount without manager approval?” vector search is helpful because wording may differ but semantic intent is clear.
- For queries like “Show regulation 5.3.2 for employee discounts in outlet stores” or “Where is policy CP‑103 defined?” keyword and exact term matching (IDs, section numbers, policy codes) are critical; hybrid ensures these IDs are not drowned out by semantic similarity noise.[^1_2][^1_1]

***

## Re‑ranking strategies in Java

For policy and procedure documents where exact terminology and section IDs matter, re‑ranking is crucial.

### Score fusion and heuristic re‑ranking

- Hybrid search guidance from Azure and Spanner emphasizes combining scores from full‑text and vector search and optionally using an ML ranker.[^1_1][^1_2]
- A pragmatic Java implementation:
    - Compute:
        - `score_text`: normalized BM25 or search engine score.
        - `score_vec`: 1 – normalized cosine distance or direct similarity score.
        - `boost_id`: +X if the document contains exact policy/section ID, store code, or complaint code.
    - Final score = `w1*score_text + w2*score_vec + w3*boost_id`.

This can be implemented as a simple function inside your retrieval service.

### LLM‑based re‑ranking

- For the top K results (e.g. 20), you can use OpenAI chat/completion to score each candidate passage on relevance to the query.[^1_7]
- Prompt pattern (pseudo):

> “Given the question: `Q` and the following passage: `P`, rate from 0 to 5 how directly this passage answers the question based only on policy content.”
- This approach is slow and costly but can be applied only to a handful of documents per query, particularly important for ambiguous customer‑service scenarios (“customer claims price mismatch with online sale”) where the right document must match both policy type and conditions.


### External re‑rank APIs

- You can integrate external re‑ranker APIs (e.g. from a vector DB or a third‑party ranking service) from Java via simple HTTP clients (OkHttp, WebClient).
- In Chroma, you typically bring your own embeddings and retrieval, but you can add a re‑ranking layer on top of Chroma query results in Java, as the Java clients return documents and metadata that can be rescored.[^1_16][^1_14][^1_15]

***

## Integrating OpenAI Java SDK with SQLite vectors and Chroma

### OpenAI + sqlite‑vec / SQLite‑Vector

1. **Set up SQLite with vector extension**
    - Build or download the `sqlite-vec` shared library and load it in SQLite; docs show using `.load ./vec0` and `CREATE VIRTUAL TABLE ... USING vec0(...)` to create vector tables.[^1_5][^1_6][^1_19]
    - In Java, use the `org.xerial:sqlite-jdbc` driver, load the extension with `SELECT load_extension('vec0');` and then create/query vector tables via JDBC, as shown in the sqlite‑vec + Java tutorial.[^1_4][^1_5]
2. **Schema design for policies**

Example table using `vec0`:

```sql
CREATE VIRTUAL TABLE policy_chunks_vec USING vec0(
    chunk_embedding FLOAT[^1_1536], -- match your embedding dim
    policy_id TEXT,
    section_id TEXT,
    title TEXT,
    chunk_text TEXT,
    policy_type TEXT,       -- complaint, returns, pricing, etc.
    jurisdiction TEXT,
    effective_date TEXT
);
```

    - Use separate relational tables for document‑level metadata and join via `policy_id` if needed.
3. **Ingest embeddings**
    - For each chunk, call OpenAI embeddings via the Java SDK, then insert into the `vec0` table using JDBC, passing the embedding as JSON or binary depending on the extension’s accepted formats (sqlite‑vec supports JSON arrays or binary blobs).[^1_6][^1_19][^1_5]
4. **Query embeddings**
    - Use `SELECT ... FROM policy_chunks_vec WHERE ... ORDER BY distance(chunk_embedding, ?queryEmbedding?) LIMIT k` or the extension’s `vec_search`/`vec_knn` helper, following the sqlite‑vec examples.[^1_19][^1_5][^1_6]
    - Combine this with a keyword search over a separate FTS table `policy_chunks_fts` on `chunk_text` and metadata.
5. **SQLite‑Vector alternative**
    - If you use SQLite‑Vector, store embeddings in a normal table as BLOBs using helper functions like `vector_convert_f32` and query via its distance functions.[^1_12][^1_11]
    - This can be simpler to integrate with existing schemas and sometimes more performant on edge devices.[^1_11][^1_12]

### OpenAI + Chroma from Java

1. **Run Chroma locally**
    - Chroma docs describe running it locally via Python or a Docker container, exposing an HTTP server (e.g. on port 8000).[^1_13]
2. **Use a Java client**
    - `amikos-tech/chromadb-java-client` and `acepero13/chromadb-client` are Java clients implementing Chroma’s HTTP API; they support collection management, add/query, and multiple embedding backends (OpenAI, sentence‑transformers).[^1_14][^1_15][^1_16]
    - Example from `acepero13/chromadb-client`: create a client pointing to `http://localhost:8000`, create a collection, then `add` documents with IDs, texts and metadata; you can then query with a natural language string, and the client handles embedding and retrieval.[^1_15]
3. **Organizing documentation in Chroma**
    - Create separate Chroma collections for major domains: `complaints`, `store-regulations`, `customer-service-procedures`, `pricing`, etc.[^1_16][^1_15]
    - Within each collection, store metadata fields such as `policy_id`, `section_id`, `doc_type`, `locale`, `store_type` (outlet, flagship, franchise) to filter queries.[^1_15][^1_16]
    - For cross‑domain retrieval, you can either:
        - Use a single `policies` collection with a `domain` metadata field, or
        - Query multiple collections and merge scores in Java.
4. **Hybrid search with Chroma**
    - Chroma itself focuses on vector search; you combine it with a separate keyword index (e.g. relational DB + FTS) in Java and fuse results similarly to the sqlite‑vec approach.[^1_13][^1_16][^1_15]

***

## Integrating Spring AI with SQLite and Chroma

Spring AI doesn’t currently have first‑class adapters for sqlite‑vec or Chroma, but the abstractions allow custom implementations.

### Custom `VectorStore` for sqlite‑vec / SQLite‑Vector

- Implement Spring AI’s `VectorStore` interface backed by JDBC calls to SQLite with `sqlite-jdbc`.[^1_5][^1_4][^1_3][^1_8]
- Methods like `add(List<Document>)` and `similaritySearch(String query, int k)` would:
    - Call an `EmbeddingClient` (OpenAI embeddings) to embed texts.[^1_8]
    - Insert embeddings into your `vec0` virtual table or SQLite‑Vector table.[^1_11][^1_6][^1_4][^1_5]
    - For `similaritySearch`, embed the query and run a k‑NN query as shown above.


### Custom `VectorStore` for Chroma

- Wrap a Chroma Java client (`chromadb-java-client` or `chromadb-client`) so `add` and `similaritySearch` delegate to Chroma’s `add` and `query` endpoints.[^1_14][^1_16][^1_15]
- Use metadata filters to restrict by policy type (pricing vs complaints) and store segment.[^1_16][^1_15]


### Hybrid retriever in Spring AI

- Implement `DocumentRetriever` that:
    - Calls your custom `VectorStore.similaritySearch` for semantic results.[^1_9][^1_3]
    - Calls a Spring Data repository backed by an FTS index for keyword matches.
    - Merges and re‑ranks using RRF/heuristics and returns a list of `Document` objects ordered by fused score.[^1_9][^1_2][^1_1]
- This pattern is consistent with the Spring AI RAG docs and the hybrid search issue, which anticipates combining full‑text and vector search for certain backends.[^1_3][^1_9]

***

## Architecture and data flow

### High‑level pipeline (both stacks)

1. **Document ingestion service**
    - Watches a document store (file shares, CMS, Git repo) for PDFs, DOCX, HTML, markdown and text.
    - Uses libraries like Apache Tika or PDFBox (not specific in docs, but standard) to extract text and metadata.
2. **Preprocessing and chunking**
    - Normalizes text, resolves headings, and chunking by logical sections with overlaps.
3. **Embedding and storage**
    - Embeds chunks with OpenAI or another embedding model.[^1_8][^1_7]
    - Stores embeddings and metadata in sqlite‑vec/SQLite‑Vector/Chroma via Java or Spring AI vector store abstractions.[^1_6][^1_4][^1_11][^1_5][^1_3][^1_8]
4. **Query handling service**
    - For each user query:
        - Runs keyword search over FTS index.
        - Embeds and runs vector search.[^1_2][^1_1][^1_7]
        - Fuses and re‑ranks results.[^1_1][^1_2]
        - Calls LLM with top N passages.[^1_3][^1_8][^1_7]
5. **Response layer**
    - Returns:
        - A natural language answer.
        - Cited passages and policy IDs.
        - Optional “view policy” links into the original documents.

### OpenAI SDK–centric vs Spring AI–centric

- **OpenAI SDK + custom plumbing**: you own all wiring (HTTP clients, data access, RAG orchestration); use when you want minimal dependencies and precise control or when integrating with existing non‑Spring stacks.[^1_7]
- **Spring AI**: you get Spring Boot auto‑configuration, property‑based config, and integration with other Spring components; ideal for a greenfield Spring Boot back‑office service.[^1_20][^1_10][^1_3][^1_8]

***

## Document processing pipeline for corporate docs

Although not all steps are in the docs, they are standard for enterprise RAG and align with examples using MongoDB Atlas and Spring AI.[^1_17][^1_20][^1_10][^1_3]

1. **Format‑specific extraction**
    - **PDF**: Use PDFBox/Tika to extract text, page breaks, headings; capture page numbers for citations.
    - **DOCX**: Use Apache POI to get paragraphs, headings and numbered lists.
    - **HTML / Intranet pages**: Whitelist specific tags (`h1`–`h4`, `p`, `li`, `table`) and remove navigation/boilerplate.
    - **Spreadsheets / pricing tables**: For pricing docs, flatten rows into text like “Product: X, Region: Y, Price: Z, Policy: P”.
2. **Chunking strategies for policies \& procedures**
    - **Section‑based chunking**: For complaint handling and store regulations, use each numbered section or sub‑section as a base chunk (e.g. “3.2 Returns without receipt”).
    - **Windowed expansion**: For very short sections, create windows that include the previous and next sections to provide context (e.g. a “3.2” chunk might also include parts of “3.1” and “3.3”).
    - **Length control**: Aim for 300–600 tokens per chunk for retrieval; for detailed procedures with many bullet points, allow up to ~800 tokens to keep steps together.
    - **Metadata**: Attach `policy_id`, `section_number`, `document_version`, `effective_date`, `region`, `language` to each chunk; this is critical for precise retrieval and for queries about “current vs archived” policies.
3. **Versioning and updates**
    - Maintain `version` and `is_current` flags in your vector store metadata; on policy update, mark old chunks as inactive and insert new ones.
    - For sqlite‑vec/SQLite‑Vector, you can either delete old rows or add a filter `WHERE is_current = 1` to all retrieval queries.[^1_12][^1_11][^1_5][^1_6]
    - In Chroma, use metadata filters to exclude non‑current policies.[^1_15][^1_16]

***

## Code example fragments

Because most docs show only pieces, here are representative snippets synthesized from the patterns described in the sources.

### Java + sqlite‑vec setup (inspired by JDBC usage guide)

```java
Class.forName("org.sqlite.JDBC");  // sqlite-jdbc driver [web:16]

try (Connection conn = DriverManager.getConnection("jdbc:sqlite:policies.db")) {
    try (Statement stmt = conn.createStatement()) {
        // Load the sqlite-vec extension
        stmt.execute("SELECT load_extension('vec0')"); // path to vec0.so/.dll [web:16][web:19]

        // Create vector table for policy chunks
        stmt.execute("""
            CREATE VIRTUAL TABLE IF NOT EXISTS policy_chunks_vec USING vec0(
                chunk_embedding float[^1_1536],
                policy_id TEXT,
                section_id TEXT,
                title TEXT,
                chunk_text TEXT,
                policy_type TEXT,
                jurisdiction TEXT,
                effective_date TEXT
            )
        """);
    }
}
```


### Chroma Java client usage (based on chromadb‑client readme)

```java
DbClient client = DbClient.create("http://localhost:8000"); // Chroma server [web:12][web:15]
Collection collection = client.createCollection("policies");

collection.add(
    List.of("chunk-1", "chunk-2"),
    AddCriteria.builder()
        .withDocuments(
            "Section 3.2: Returns without receipt...",
            "Section 4.1: Price adjustments policy..."
        )
        .withMetadata(
            Metadata.of("policy_id", "RETURNS-2025"),
            Metadata.of("policy_id", "PRICING-2025")
        )
        .build()
);

QueryResponse<QueryResult> response =
    collection.query("How do I process a return without a receipt?");
```


***

## Comparison matrix

High‑level comparison for your use case (customer‑service policies in clothing retail):


| Dimension | OpenAI Java SDK + custom RAG | Spring AI (with custom vector store) | LangChain4j | RAG4J |
| :-- | :-- | :-- | :-- | :-- |
| Abstraction level | Low; you manage retrieval, ranking, orchestration | Medium/High; retrieval and LLM orchestration abstractions | High; chain‑oriented abstractions around OpenAI and stores | Medium; RAG‑specific modules and interfaces |
| RAG support | No built‑in; you implement | Built‑in RAG pattern (retrievers + vector stores + chat) but must implement hybrid manually | Built‑in RAG components and content retrievers | Built‑in RAG architecture for learning and extension |
| Hybrid search | Custom; parallel FTS + vector with RRF | Custom `DocumentRetriever` + hybrid logic; hybrid support discussed in issues | Custom; easier via retrievers and multiple stores | Custom; implement retrievers using multiple stores |
| Re‑ranking | Custom (score fusion, LLM judge, third‑party rerank) | Same, but easier to insert into Spring beans | Same; can plug custom re‑rankers in chains | Same; explicit retriever and tracker interfaces help experimentation |
| Vector DB integration | Direct JDBC/HTTP to sqlite‑vec, SQLite‑Vector, Chroma | Custom `VectorStore` implementations | Community integrations (e.g. MongoDB, others) and you can add more [^1_17] | Start from simple in‑memory / Weaviate and extend [^1_18] |
| Ease of bootstrapping | Simple for small prototypes; more boilerplate in prod | Very good if you already use Spring Boot | Good if you’re comfortable with chain concepts | Good for learning and workshops; may require adaptation for prod [^1_18] |
| Community \& docs | Strong OpenAI docs; Java client docs evolving [^1_7] | Growing Spring AI community and official reference [^1_3][^1_8][^1_10] | Active but smaller than Python LangChain; good MongoDB + RAG examples [^1_17] | Niche but focused on RAG for Java [^1_18] |
| Fit for enterprise docs | High but requires more custom work; flexible stack | Excellent if your platform is Spring; good centralization of config and observability [^1_3][^1_20][^1_10] | Good for POCs and advanced pipelines; less “enterprise” conventions | Good as a base for workshops and custom RAG experiments |


***

## Hybrid search strategy for your scenarios

For a clothing retailer, typical query types:

- **Specific policy lookup** (“What is the refund policy for online purchases picked up in store?”)
- **Procedural guidance** (“Step‑by‑step, how do I handle a damaged‑on‑arrival complaint?”)
- **Pricing inquiries** (“Can I match the online discount in store?”)
- **Regulation clarifications** (“What are age restrictions for selling certain items?”)

Recommended hybrid strategy:

1. **Index structure**
    - Maintain an FTS index over `chunk_text` and important fields (`section_title`, `policy_id`, `tags`) in SQLite/PostgreSQL.
    - Maintain a vector index (sqlite‑vec / SQLite‑Vector / Chroma) on the embedding of the chunk.[^1_4][^1_11][^1_5][^1_6]
2. **Query analysis**
    - If the query contains obvious identifiers (policy IDs, section numbers, “CP‑103”, “3.2”), give higher weight to FTS and exact term matches.
    - For more natural language or ambiguous questions, weight vector similarity higher.
3. **Fusion**
    - Run both full‑text and vector queries for all queries, as Azure AI Search hybrid docs recommend (single logical query).[^1_1]
    - Compute a fused score; start with RRF and then tweak weights, similar to Spanner’s hybrid search guidance.[^1_2][^1_1]
4. **Post‑filtering**
    - Use metadata filters for region, store type, sales channel (online vs in‑store), and version status to avoid returning outdated or irrelevant policies.[^1_12][^1_11]

***

## Re‑ranking considerations for policy documents

To make sure the right policy section surfaces at the top:

1. **Terminology‑sensitive scoring**
    - Count occurrences of key policy terms, codes and roles (“store manager”, “assistant manager”, “cashier”) in each candidate chunk and add bonuses.
    - Prefer exact phrase matches (“without receipt”, “price adjustment”, “defective product”) rather than bag‑of‑words for final ordering.
2. **Hierarchy‑aware re‑ranking**
    - If a sub‑section is retrieved but its parent section defines scope or exceptions, consider re‑ranking to keep those together or elevate the parent section.
    - Use metadata such as `section_level` and `parent_section_id` to cluster and then pick the most representative chunk for final context.
3. **Answer type‑aware ranking**
    - If the question explicitly asks for “steps” or “procedure”, prioritize chunks tagged as `doc_type = procedure` or containing ordered/bulleted lists.
    - For “limits” or “amounts”, prioritize pricing tables or sections with numbers, and use the LLM to extract specific values while citing the full context.
4. **Guardrails to reduce hallucinations**
    - Only feed the top N retrieved and re‑ranked chunks; instruct the LLM **not** to invent new rules and to phrase answers as “According to policy X, section Y…”.
    - If no chunk surpasses a relevance threshold, have the service return “no answer found; please contact support” rather than letting the model guess.

***

## Performance, limitations and best practices

### Performance considerations

- **Vector DB choice**
    - sqlite‑vec and SQLite‑Vector are very lightweight and ideal for embedded/local scenarios but may require more manual tuning for very large corpora; they are “fast enough” for many use cases but don’t provide distributed scaling out of the box.[^1_11][^1_5][^1_6][^1_12]
    - Chroma offers a more “database‑like” experience, with HTTP APIs and better tooling, and can be scaled using containers/k8s as in the Java client blog (minikube + Helm).[^1_14][^1_13]
- **Indexing and updates**
    - In sqlite‑vec, each insert is a normal SQLite transaction; for large policy sets, batch inserts and run periodic maintenance if needed.[^1_5][^1_6]
    - In Chroma, ingestion is straightforward but large numbers of documents may require batching and careful selection of embedding size to balance recall and performance.[^1_13][^1_15][^1_16]
- **Hybrid search overhead**
    - Running both FTS and vector search adds latency; mitigate via:
        - Limiting vector search to top N by filter (e.g. filter to `policy_type` derived from the query).
        - Caching query embeddings and hot results.
        - Using approximate nearest neighbor settings if/when vector extension supports it (some DBs use HNSW / ScaNN).[^1_2][^1_1]


### Limitations and gaps

- **OpenAI Java SDK**
    - No first‑class RAG or re‑ranking API—just primitives (embeddings, chat, tools); all retrieval, hybrid logic and re‑ranking are on you.[^1_7]
- **Spring AI**
    - No out‑of‑the‑box support for sqlite‑vec or Chroma; you must implement custom `VectorStore` and `DocumentRetriever`.[^1_9][^1_3][^1_8]
    - Hybrid search is not yet standardized; an open issue shows interest but not a complete reference implementation.[^1_9]
- **sqlite‑vec / SQLite‑Vector**
    - Pre‑v1 and evolving, so APIs and performance characteristics may change and you must manage extension binaries and loading yourself.[^1_6][^1_12][^1_11][^1_5]
- **Chroma Java clients**
    - Some Java clients are early‑stage, not yet on Maven Central, and may require cloning and building locally.[^1_14][^1_16]

***

## Recommendations for your clothing‑retail use case

Given you are comfortable with Java/Spring and want local vector DBs:

1. **Stack choice**
    - If your backend is Spring Boot:
        - Use **Spring AI** with OpenAI embeddings and chat.[^1_20][^1_10][^1_3][^1_8]
        - Implement:
            - A custom `VectorStore` for sqlite‑vec or Chroma.[^1_15][^1_16][^1_4][^1_5][^1_3][^1_8]
            - A `HybridDocumentRetriever` that queries both FTS and the `VectorStore` and fuses results.[^1_9][^1_1][^1_2]
    - If you prefer more control or a non‑Spring service:
        - Use **OpenAI Java SDK directly** plus either LangChain4j or RAG4J as a helper framework; LangChain4j gives you content retrievers and embedding stores, RAG4J offers a clear RAG architecture.[^1_18][^1_17]
2. **Vector DB**
    - For an on‑prem, self‑contained deployment with modest scale, **sqlite‑vec or SQLite‑Vector** embedded via `sqlite-jdbc` is attractive and avoids running a separate vector DB service.[^1_12][^1_4][^1_11][^1_5][^1_6]
    - If you want a dedicated vector DB service with better tooling and potential horizontal scaling, **Chroma** plus a Java client is a good fit.[^1_13][^1_16][^1_14][^1_15]
3. **Hybrid + re‑ranking**
    - Implement a hybrid pipeline modeled on Azure AI Search and Spanner guidance: parallel FTS + vector queries, RRF or weighted score fusion, then optional LLM‑based re‑ranking for the top 20 results.[^1_1][^1_2]
    - Encode policy IDs, section numbers, store types and regions in metadata and incorporate them into your scoring heuristics for high‑precision policy lookups.
4. **Safety and compliance**
    - Keep all customer complaint details and internal procedures in local SQLite/Chroma and **never send full raw documents** to the LLM; send only relevant chunks.[^1_11][^1_5][^1_12]
    - Use OpenAI’s data‑usage and privacy settings appropriate for enterprise use and document them for compliance teams.[^1_7]
    - Implement audit logging (which chunks were retrieved, which policy versions used) for later review and compliance checks.

***

## Resources

Key references and examples you can dig into:

- OpenAI embeddings docs (Java‑agnostic but core to RAG): [https://developers.openai.com/api/docs/guides/embeddings](https://developers.openai.com/api/docs/guides/embeddings)[^1_7]
- Spring AI RAG reference: [https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html)[^1_3]
- Spring AI OpenAI embeddings: [https://docs.spring.io/spring-ai/reference/api/embeddings/openai-sdk-embeddings.html](https://docs.spring.io/spring-ai/reference/api/embeddings/openai-sdk-embeddings.html)[^1_8]
- Spring AI hybrid search issue: [https://github.com/spring-projects/spring-ai/issues/579](https://github.com/spring-projects/spring-ai/issues/579)[^1_9]
- InfoQ article: RAG with Spring Boot, Spring AI and MongoDB Atlas:[^1_10]
- Azure AI Search hybrid search overview (great conceptual reference):[^1_1]
- Spanner hybrid search blog (vector + FTS + ML rerank):[^1_2]
- `sqlite-vec` project and blog:[^1_19][^1_5][^1_6]
- SQLite‑Vector project:[^1_12][^1_11]
- Java + sqlite‑vec JDBC tutorial:[^1_4]
- Chroma docs: [https://docs.trychroma.com/docs/overview/getting-started](https://docs.trychroma.com/docs/overview/getting-started)[^1_13]
- Chroma Java clients:[^1_16][^1_14][^1_15]
- LangChain4j RAG tutorial with OpenAI:[^1_17]
- RAG4J framework:[^1_18]
- Azure OpenAI + Spring Boot RAG tutorial (Java):[^1_20]

If you want, I can next sketch a concrete Spring Boot module layout (packages, beans, and interfaces) specifically for “policy RAG” with sqlite‑vec vs Chroma so you can drop it into a project.
<span style="display:none">[^1_21][^1_22][^1_23][^1_24][^1_25][^1_26][^1_27][^1_28][^1_29][^1_30]</span>

<div align="center">⁂</div>

[^1_1]: https://learn.microsoft.com/en-us/azure/search/hybrid-search-overview

[^1_2]: https://cloud.google.com/blog/topics/developers-practitioners/hybrid-search-in-spanner-combine-full-text-and-vector-search

[^1_3]: https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html

[^1_4]: https://blog.csdn.net/gitblog_00691/article/details/151346113

[^1_5]: https://github.com/asg017/sqlite-vec

[^1_6]: https://alexgarcia.xyz/blog/2024/sqlite-vec-stable-release/index.html

[^1_7]: https://developers.openai.com/api/docs/guides/embeddings/

[^1_8]: https://docs.spring.io/spring-ai/reference/api/embeddings/openai-sdk-embeddings.html

[^1_9]: https://github.com/spring-projects/spring-ai/issues/579

[^1_10]: https://www.infoq.com/articles/rag-with-spring-mongo-open-ai/

[^1_11]: https://github.com/sqliteai/sqlite-vector

[^1_12]: https://www.sqlite.ai/sqlite-vector

[^1_13]: https://docs.trychroma.com/docs/overview/getting-started

[^1_14]: https://blog.amikos.tech/introducing-the-java-client-for-chroma-vector-db-ba5502af44b3?gi=ed73537a6f64

[^1_15]: https://github.com/acepero13/chromadb-client

[^1_16]: https://github.com/amikos-tech/chromadb-java-client

[^1_17]: https://dev.to/mongodb/how-to-make-a-rag-application-with-langchain4j-1mad

[^1_18]: https://github.com/RAG4J/rag4j

[^1_19]: https://towardsdatascience.com/retrieval-augmented-generation-in-sqlite/

[^1_20]: https://learn.microsoft.com/en-us/azure/app-service/tutorial-ai-openai-search-java

[^1_21]: https://wenku.csdn.net/answer/59hg1vvddj

[^1_22]: https://stackoverflow.com/questions/1525444/how-to-connect-sqlite-with-java

[^1_23]: https://stackoverflow.com/questions/41233/java-and-sqlite

[^1_24]: https://github.com/asg017/sqlite-vss

[^1_25]: https://github.com/xerial/sqlite-jdbc/issues/1212

[^1_26]: https://github.com/asg017/sqlite-vec-go-bindings

[^1_27]: https://news.ycombinator.com/item?id=45347619

[^1_28]: https://docs.sqlitecloud.io/docs/sqlite-vector

[^1_29]: https://www.reddit.com/r/sqlite/comments/1dznc2z/vectorlite_a_fast_vector_search_extension_for/

[^1_30]: https://www.reddit.com/r/LocalLLaMA/comments/1ehlazq/introducing_sqlitevec_v010_a_vector_search_sqlite/


---

# explain in more detail what is BM25‑style keyword

BM25‑style keyword scoring is a classic way search engines rank documents purely based on keyword occurrences, adjusted by how rare the terms are and how long each document is.[^2_1][^2_2][^2_3]

## Core idea

BM25 (“Best Matching 25”) is a **bag‑of‑words** ranking function: it ignores word order and just looks at which query terms appear in each document and how often.[^2_3][^2_1]
For a query with terms $q_1, ..., q_n$, BM25 gives each document a relevance score by summing contributions from each query term.[^2_1][^2_3]

In words, a document scores higher if:

- It contains the query terms many times (but with diminishing returns).[^2_4][^2_3][^2_1]
- It contains rarer query terms (those that appear in fewer documents overall).[^2_2][^2_4][^2_3][^2_1]
- It is not excessively long, so matches are more “concentrated”.[^2_2][^2_3][^2_1]


## Main components

BM25’s score for a document combines three signals:[^2_5][^2_3][^2_1][^2_2]

- **Term frequency (TF)** – how often a query term appears in the document.
    - More occurrences → higher score, but **saturates**: the 10th occurrence adds less than the 1st.[^2_6][^2_4][^2_3][^2_1]
- **Inverse document frequency (IDF)** – how rare the term is in the corpus.
    - Terms that appear in many documents (e.g. “the”, “policy”) have low or even negative weight; rare terms (“complaint escalation”, “CP‑103”) have high weight.[^2_4][^2_3][^2_6][^2_1]
- **Document length normalization** – preference for shorter, focused documents.
    - BM25 penalizes long documents where a term might appear only incidentally and boosts shorter docs where the term is a larger proportion of the text.[^2_3][^2_6][^2_1][^2_2]

Two hyperparameters control behavior:

- `k1` (~1.2–2.0) controls how quickly term frequency saturates.[^2_6][^2_1][^2_2]
- `b` (~0.75) controls how strongly scores are normalized by document length.[^2_1][^2_2][^2_3]


## Intuition vs TF‑IDF and vectors

Compared to plain TF‑IDF, BM25:

- Adds **term frequency saturation**, so repeating the same keyword 100 times doesn’t make a document unbeatable.[^2_5][^2_3][^2_6][^2_1]
- Handles **document length** explicitly, so long manuals don’t automatically dominate short, focused policy notes.[^2_3][^2_6][^2_1]

Compared to dense/embedding search, BM25‑style keyword search is:

- **Purely lexical** – it only works when query words (or close variants) actually appear in the document.[^2_7][^2_2][^2_5]
- **Very fast and interpretable** – scores can be explained in terms of counts, rarity and lengths.[^2_2][^2_6][^2_5][^2_3]

That’s why in hybrid RAG systems, you’ll often see:

- **BM25 (or similar) as the keyword leg** for exact IDs, policy codes, regulation numbers and specific phrases.[^2_8][^2_9][^2_5][^2_2]
- **Vector search** as the semantic leg to handle paraphrases, synonyms, and “conceptual” queries.[^2_7][^2_5][^2_2]

In that context, “BM25‑style keyword” just means “a traditional, lexical keyword scorer that behaves like BM25: term counts + rarity + length normalization, with diminishing returns on repeats,” usually implemented in an FTS engine (Lucene, Elasticsearch, SQLite FTS + a BM25‑like scoring function, etc.).[^2_9][^2_8][^2_5][^2_1][^2_2][^2_3]
<span style="display:none">[^2_10][^2_11][^2_12][^2_13][^2_14][^2_15]</span>

<div align="center">⁂</div>

[^2_1]: https://en.wikipedia.org/wiki/Okapi_BM25

[^2_2]: https://docs.weaviate.io/weaviate/concepts/search/keyword-search

[^2_3]: https://www.paradedb.com/learn/search-concepts/bm25

[^2_4]: https://zilliz.com/ai-faq/what-is-the-role-of-bm25-in-fulltext-search

[^2_5]: https://www.geeksforgeeks.org/nlp/what-is-bm25-best-matching-25-algorithm/

[^2_6]: https://mbrenndoerfer.com/writing/bm25-search-algorithm-elasticsearch-implementation

[^2_7]: https://www.linkedin.com/posts/anmolbansal2142_informationretrieval-bm25-rag-activity-7334910021375328256-fYrX

[^2_8]: https://docs.opensearch.org/latest/search-plugins/keyword-search/

[^2_9]: https://docs.vespa.ai/en/ranking/bm25.html

[^2_10]: https://www.luigisbox.com/search-glossary/bm25/

[^2_11]: https://docs.weaviate.io/weaviate/search/bm25

[^2_12]: https://www.kopp-online-marketing.com/what-is-bm25

[^2_13]: https://adasci.org/understanding-okapi-bm25-a-guide-to-modern-information-retrieval/

[^2_14]: https://learn.microsoft.com/ja-jp/azure/search/index-similarity-and-scoring

[^2_15]: https://web.stanford.edu/class/cs276/handouts/lecture12-bm25etc.pdf
