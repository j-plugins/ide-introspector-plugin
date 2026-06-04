# Context / Skill-Book — Design

Status: design (2026-06-04). Code not started. This document is the source of truth for the
"context loading" feature; update it as decisions change.

## Goal

Give an MCP client a way to **pull the context it needs before its next actions**: curated
guidance + auto-generated reference about the IntelliJ Platform, about this plugin's own
tools, and worked examples mined from real plugins.

One **corpus** (markdown files) is assembled at build time and shipped two ways:

1. As a **read-only resource in the plugin jar**, queried by new `skill.*` / `context.*` MCP
   tools with progressive disclosure and a token budget.
2. As a **static site on GitHub Pages** for humans — generated from the same files.

Hard constraints (project rules): everything blocking ≤ 10 s, `kotlinx-serialization-json`
stays `compileOnly`, no heavy new runtime deps, no dependence on `exec.*`, no comments,
pure logic kept platform-independent for unit tests.

---

## 1. Corpus layers (decided)

Files only — markdown with a **minimal frontmatter** header (§2). No database, no YAML
documents.

```
corpus/
  manual/                              # source: manual — hand-written skills/recipes/notes
  generated/                           # rewritten by the build; never hand-edited
    sdk-platform/                      #   vendored IntelliJ Platform SDK docs (chunked)
    introspector-docs/                 #   this plugin's own reference (tool cards + docs)
    examples/
      plugins/<pluginId>/<ep>.md       #   real plugin source per extension point, de-junked
```

- **manual** wins over **generated** on merge (by `id`): a hand-written entry can correct or
  supersede a generated one without editing a regenerated file. This replaces a separate
  "overrides" mechanism — keep it simple until it hurts.
- **generated/sdk-platform** is produced from a pinned checkout of
  `github.com/JetBrains/intellij-sdk-docs` (Apache-2.0). Vendored at build, never fetched at
  runtime. Ship `LICENSE`/`NOTICE`, keep `sourceUrl` on each chunk.
- **generated/introspector-docs** is produced from the existing KSP `@McpTool` scan (the same
  pass that writes `docs/MCP_TOOLS.md`) plus hand-maintained plugin docs.
- **generated/examples** is mined from the live IDE (Platform Explorer data: `arch.*` /
  `code.*` / `psi.*`). For now the layout is flat — one file per plugin per extension point,
  `plugins/<pluginId>/<ep>.md`, holding the cleaned implementation source + reconstructed
  registration XML. Later this can be re-sliced by feature; the flat layout is the v1.

### Example file shape (`generated/examples/plugins/<pluginId>/<ep>.md`)

```markdown
---
id: example.com.intellij.java.lineMarkerProvider
title: lineMarkerProvider — com.intellij.java
source: generated
kind: example
plugin: com.intellij.java
ep: com.intellij.codeInsight.lineMarkerProvider
verifiedAgainstBuild: 252.28238.29
tags: [line-marker, gutter, code-insight]
---
## Registration
```xml
<extensions defaultExtensionNs="com.intellij">
  <codeInsight.lineMarkerProvider language="JAVA" implementationClass="..."/>
</extensions>
```
## Implementation
```kotlin
// cleaned source: imports collapsed, license header + decompiler noise stripped
```
```

"De-junking" the harvested source = strip license headers, collapse import blocks, drop
decompiler artifacts (`$1` synthetics, `/* $FF: ... */`), keep the meaningful body.

---

## 2. Minimal frontmatter format

The header block is delimited by `---` like `SKILL.md`, but parsed by a **tiny hand-rolled
Kotlin parser** — no snakeyaml, no nested maps. Grammar:

- `key: value` — string (rest of line, trimmed).
- `key: [a, b, c]` — flat list of strings.
- That's all. No nesting, no multi-line scalars, no anchors. A line that doesn't match fails
  validation with a precise message.

### Field schema

| key | required | applies to | meaning |
| --- | --- | --- | --- |
| `id` | yes | all | stable opaque key; merge/override key; never derived from array index |
| `title` | yes | all | human title |
| `description` | manual | manual | one-line, third person, "what + when" (drives discovery) |
| `when_to_use` | no | manual | comma-separated trigger phrases for retrieval |
| `source` | yes | all | `manual` \| `generated` |
| `kind` | yes | all | `skill` \| `concept` \| `reference` \| `example` |
| `state` | no | all | `draft` \| `verified` \| `stale` \| `deprecated` (default `verified` for generated) |
| `verifiedAgainstBuild` | generated | generated | IDE build the entry was generated/verified against |
| `tags` | no | all | flat list |
| `related_eps` | no | all | flat list of EP names this links to |
| `related_tools` | no | all | flat list of `@McpTool` names |
| `plugin` / `ep` | example | example | provenance for mined examples |

Validation gates (fail the build): missing required key, unknown `source`/`kind`/`state`
enum, unparseable frontmatter, duplicate `id`, `related_tools` naming a non-existent tool,
broken internal link. Soft warnings: snippet compile failure, `verifiedAgainstBuild` older
than the current platform.

---

## 3. Storage & retrieval

- **Format:** flat markdown chunks + a single `manifest.json` (the index), bundled read-only
  in the jar. A writable overlay at `PathManager.getSystemPath()/introspector-corpus/` holds
  user additions; overlay entries win by `id`.
- **Manifest:** list of chunk headers (`id`, `title`, `kind`, `source`, `tags`,
  `tokenEstimate`, top stemmed term map, build range). Bodies live in the `.md` files, read
  on demand. Parsed once behind the existing `TtlCache`.
- **Search:** pure-Kotlin **BM25** over the term maps. No SQLite, no embeddings (corpus is
  small + jargon-heavy; lexical wins; a vector reranker can drop in later behind an
  interface). Ranking signals: tag match, title match, body BM25, graph proximity to a given
  EP/class, manual-priority boost, recency.
- **Versioning:** `manifest.generatedForBuild`; on mismatch with `ApplicationInfo.build`
  surface a soft warning, never fail; live `arch.*` is the always-current fallback.

---

## 4. MCP surface

| tool | purpose | key params | returns |
| --- | --- | --- | --- |
| `skill__list` | list manual skills (id, title, tokens) | `tag?` | `SkillList` |
| `skill__get` | one skill body, budget-clamped | `id, maxTokens=2000` | `SkillSection` |
| `context__search` | BM25 over whole corpus, locators only | `query, limit=10` | `ContextSearchResult` |
| `context__get` | fetch one chunk by id, paginated | `id, maxTokens` | `ContextSection` |
| `context__assemble` | intent → assembled bundle + next-action hints | `ContextQuery` | `ContextBundle` |

Progressive disclosure: `list`/`search` return only ids + `tokenEstimate`; bodies only via
`get`. Every section reports `returnedTokens`, `truncated`, `nextSectionId` for cursor-free
pagination. `context__assemble` returns verbatim corpus fragments with `citationIds` (no
paraphrase → no hallucinated APIs) plus `suggestedActions` (concrete next `@McpTool` calls,
validated at build time against the tool registry). Below a score floor it returns
`confidence=LOW`, empty sections, and generic exploration actions.

---

## 5. Build pipeline

- Keep **KSP** (`doc-processor`) for annotation scanning only; add a `skillCardOutput` arg so
  it emits `generated/introspector-docs` tool cards.
- Add a **`buildSrc` Gradle task `AssembleContextCorpus`** (`@CacheableTask`, typed
  in/out dirs): merge `manual/` + KSP cards + vendored SDK docs + mined examples →
  `build/corpus` + `manifest.json`.
- `validateContextCorpus` runs the §2 gates before `build`.
- `processResources { from(corpusOut) into "context-corpus" }` packs it into the jar.
- `publishCorpusSite` mirrors to `site/` for Pages — NOT wired into `buildPlugin`.
- **Examples need a live IDE**, so they can't run in a pure build: a `runIde`-based capture
  task writes deterministic `plugins/<pluginId>/<ep>.md` files that are committed
  (snapshot-commit). The normal build consumes the committed snapshot.
- Reproducible: no `Date.now()`/hostnames, stable sort, sorted JSON keys.

---

## 6. GitHub Pages

Astro **Starlight** (frontmatter-native, skill cards as components, versioning) + **Pagefind**
(static client-side search, no backend). IA: Skills / IntelliJ Platform reference / Plugin
examples / Tool reference (= `MCP_TOOLS.md`, copied not duplicated) / How the MCP uses this.
Single source: one Gradle model → jar resource + `site/src/content/docs/`. GH Actions:
`./gradlew buildCorpus` → `withastro/action` → `actions/deploy-pages`. Versions keyed by
`IDE-build × plugin-release`. Each example renders provenance (`plugin`, `ep`,
`verifiedAgainstBuild`).

---

## 7. Cross-cutting rules

- **≤10 s:** index precomputed at build → runtime only deserializes (lazy + `TtlCache`);
  search is capped in-memory; any optional fetch wrapped in `withTimeoutOrNull(10_000)` and
  **off by default**.
- **No heavy deps:** hand-rolled frontmatter parser, raw markdown, `Map`-based index;
  `kotlinx-serialization-json` stays `compileOnly`.
- **Privacy:** no user source/PSI/paths in the corpus or site; examples carry only platform
  metadata + the mined plugin's own source; runtime network egress opt-in, allowlisted origin.
- **No `exec.*` dependency.**
- **Testability (pure units, Kover):** `FrontmatterParser`, `FrontmatterValidator`,
  `CorpusMerger`, `Bm25Indexer`, `Bm25Ranker`, `TokenBudgeter`, `CorpusResolver`,
  `SourceCleaner`. IntelliJ/Swing only at the edges (`McpToolset`, settings, IO adapter) →
  `runIde` integration.
- **Graceful degradation:** every tool returns a typed `unavailable` / `degraded` / `error`
  result, never throws to the MCP bridge.

---

## 8. Roadmap

1. **Foundation** (detailed in §9) — corpus tree, frontmatter parser/validator, merger,
   manifest, `AssembleContextCorpus` + `validateContextCorpus`, jar packaging. No content yet.
2. **Search + basic tools** — BM25 indexer/ranker, `skill__list/get`, `context__search/get`.
3. **Auto-reference** — KSP `skillCardOutput`, vendored SDK docs, EP↔doc join.
4. **Examples** — `runIde` capture task, `SourceCleaner`, `plugins/<pluginId>/<ep>.md`.
5. **Assemble** — `context__assemble`, action-hints, graph ranking.
6. **GitHub Pages** — Starlight + Pagefind, deploy.
7. **Lifecycle** — `staleCheck`, states, `newSkill` scaffold.

---

## 9. Phase 1 — Foundation (detailed)

Goal: a build that takes the `corpus/` tree, validates it, merges layers, emits a
`manifest.json` + normalized chunks, and packs them into the plugin jar — with everything
testable as pure logic. No MCP tools, no content beyond a couple of seed `manual/` files.

### 9.1 New pure-logic module (platform-independent)

Lives under `src/main/kotlin/com/github/xepozz/ide/introspector/context/`. No IntelliJ
imports — so it is unit-testable and reusable by the Gradle task.

```
context/
  CorpusEntry.kt          # @Serializable model of one parsed entry
  Frontmatter.kt          # @Serializable parsed header
  FrontmatterParser.kt    # String -> Frontmatter + body (hand-rolled, no snakeyaml)
  FrontmatterValidator.kt # Frontmatter -> List<ValidationIssue> (fail vs warn)
  CorpusMerger.kt         # List<CorpusEntry> -> merged, manual wins by id
  Manifest.kt             # @Serializable index model
  ManifestBuilder.kt      # List<CorpusEntry> -> Manifest (tokenEstimate, term map)
  TokenEstimator.kt       # chars/4 heuristic, conservative round-up
```

Models (fields only):

```kotlin
@Serializable
data class Frontmatter(
    val id: String,
    val title: String,
    val source: Source,                 // MANUAL | GENERATED
    val kind: Kind,                     // SKILL | CONCEPT | REFERENCE | EXAMPLE
    val state: State = State.VERIFIED,  // DRAFT | VERIFIED | STALE | DEPRECATED
    val description: String? = null,
    val whenToUse: String? = null,
    val verifiedAgainstBuild: String? = null,
    val tags: List<String> = emptyList(),
    val relatedEps: List<String> = emptyList(),
    val relatedTools: List<String> = emptyList(),
    val plugin: String? = null,
    val ep: String? = null,
)

@Serializable
data class CorpusEntry(
    val frontmatter: Frontmatter,
    val body: String,
    val relativePath: String,
)

@Serializable
data class ValidationIssue(
    val path: String,
    val severity: Severity,             // ERROR | WARNING
    val message: String,
)

@Serializable
data class Manifest(
    val generatedForBuild: String,
    val entries: List<ManifestHeader>,
)

@Serializable
data class ManifestHeader(
    val id: String,
    val title: String,
    val kind: Kind,
    val source: Source,
    val tags: List<String>,
    val tokenEstimate: Int,
    val relativePath: String,
    val terms: Map<String, Int>,        // stemmed term -> frequency (top ~40)
)
```

### 9.2 FrontmatterParser rules

- Split on the first two `---` lines. No closing `---` and `kind != example`? warn + treat
  whole file as body with a synthesized id from the path.
- Inside the block: `key: value` → string; `key: [a, b, c]` → `split(",").map(trim)`.
- Unknown keys → `WARNING` (forward-compatible), not an error.
- Map enum strings case-insensitively to `Source`/`Kind`/`State`; unknown → `ERROR`.

### 9.3 FrontmatterValidator gates

ERROR (fails the build): missing `id`/`title`/`source`/`kind`; unknown enum value;
duplicate `id` across the merged set; example missing `plugin`/`ep`. WARNING: missing
`description` on a manual skill; `verifiedAgainstBuild` absent on generated; near-duplicate
title. `relatedTools` validation against the live `@McpTool` set is deferred to Phase 3
(needs the KSP tool list); for now it is a WARNING.

### 9.4 CorpusMerger

Input = all parsed entries from every layer. Group by `id`. Precedence: `manual` >
`generated`. Within the same source, later path wins deterministically (sorted). Output =
one entry per id + the full `ValidationIssue` list (duplicate-id errors surface here).

### 9.5 Gradle wiring (buildSrc)

```
buildSrc/src/main/kotlin/AssembleContextCorpus.kt   # @CacheableTask
buildSrc/src/main/kotlin/ValidateContextCorpus.kt   # depends on assemble
```

- `AssembleContextCorpus`: `@InputDirectory corpusDir` (the repo `corpus/`), `@OutputDirectory
  corpusOut` (`build/corpus`). Reads every `.md`, runs `FrontmatterParser` + `CorpusMerger`
  + `ManifestBuilder`, writes normalized `.md` + `manifest.json` (sorted keys, stable order).
  Reuses the same pure-logic classes as runtime — single implementation.
- `ValidateContextCorpus`: runs validators, throws `GradleException` on any `ERROR`, prints a
  report of `WARNING`s.
- `processResources.dependsOn(validateContextCorpus)` + `from(corpusOut) { into("context-corpus") }`.
- `buildPlugin` already depends on `processResources` → transitively gated. No `verifyPlugin`.

### 9.6 Phase 1 acceptance checklist

- [ ] `corpus/manual/` seeded with 2–3 hand-written `.md` (smoke content).
- [ ] `FrontmatterParser`/`Validator`/`Merger`/`ManifestBuilder`/`TokenEstimator` unit-tested,
      no IntelliJ imports, meets Kover threshold.
- [ ] `./gradlew build` assembles `build/corpus/manifest.json` + chunks and packs them into the
      jar under `context-corpus/`.
- [ ] A duplicate `id` or missing required key fails the build with a precise message.
- [ ] No new runtime deps; `kotlinx-serialization-json` still `compileOnly`.
- [ ] No comments, no abbreviations, Kotlin-idiomatic; reuses `TtlCache`/`Utf8Truncation`
      where relevant.

---

## 10. Cross-review outcome & frozen inter-part contract

A 5-part design pass + rotating cross-review (each part reviewed by the author of the part
that consumes it) found the architecture sound but the parts designed against *imagined*
neighbour APIs. The fixes below are the binding contract; implement to these, not to the
per-part prose above where they conflict.

Verdicts: Part 1 (frontmatter) and Part 3 (manifest/index) APPROVE-WITH-CHANGES; Part 2
(corpus/merger), Part 4 (search), Part 5 (tools/gradle) NEEDS-REWORK (interface re-base).

### 10.1 Frozen shared types (single source — `context/`)

```kotlin
enum class IssueCode { MISSING_ID, MISSING_TITLE, MISSING_SOURCE, MISSING_KIND, INVALID_ENUM,
    MALFORMED_LINE, MALFORMED_LIST, UNTERMINATED_FRONTMATTER, EXAMPLE_MISSING_PROVENANCE,
    BLANK_REQUIRED_VALUE, DUPLICATE_ID_IN_TIER, LAYER_SOURCE_MISMATCH, UNKNOWN_LAYER,
    MANUAL_OVERRIDE_NO_TARGET, MISSING_FRONTMATTER, UNKNOWN_KEY, DUPLICATE_KEY,
    GENERATED_MISSING_BUILD, MANUAL_MISSING_DESCRIPTION, RELATED_TOOLS_UNVERIFIED,
    RELATED_TOOLS_UNKNOWN, DEAD_INTERNAL_LINK, SIZE_BUDGET_EXCEEDED }

data class ValidationIssue(val severity: Severity, val code: IssueCode, val message: String,
    val key: String? = null, val sourcePath: String? = null)

data class ParsedDocument(val frontmatter: Frontmatter?, val body: String,
    val parseIssues: List<ValidationIssue>)
```

- `Frontmatter.source` and `Frontmatter.kind` are **nullable** (`Source?`, `Kind?`). The parser
  sets them only when present and valid; absence stays absent. This kills the "constructible
  model" hack — a missing `source` must NEVER silently default to `GENERATED`, because the
  merger uses `source` for precedence and a false `GENERATED` causes a real entry to be
  overwritten (data loss). Validator reports null as `MISSING_SOURCE`/`MISSING_KIND`; merger
  treats null `source` as "unmergeable / must-fix", never as a tier.
- `FrontmatterValidator.validate(frontmatter, sourcePath, knownToolNames: Set<String>? = null)`.
  `null` → `RELATED_TOOLS_UNVERIFIED` (WARNING, runtime/Phase-1). Non-null (build task passes
  the KSP tool-name set) → `RELATED_TOOLS_UNKNOWN` (ERROR) for any `related_tools` entry not in
  the set; no double-emit. Tool names are the literal `group__snake_case` form (e.g.
  `arch__list_extension_points`), NOT the dotted `arch.*` form.

### 10.2 Manifest / index contract (Part 3 ↔ Part 4)

- There is **no `ManifestHeader.terms`**. Per-entry term frequencies live on
  `ManifestEntry.termFrequencies` (**top-40 only**); full document length is `ManifestEntry.length`
  (token count over the *whole* body). Corpus stats live on `CorpusStats`
  (`documentCount`, `averageDocumentLength`, `documentFrequencies`).
- The BM25 ranker walks `manifest.entries`: `f = entry.termFrequencies[t] ?: 0`,
  `dl = entry.length`, `avgdl = corpusStats.averageDocumentLength`,
  `N = corpusStats.documentCount`, `df = corpusStats.documentFrequencies[t] ?: 0`.
- `ScoredEntry`/`SearchHit` carry a `ManifestEntry` (it needs `termFrequencies`+`length`), and
  `kind`/`source` are **`String`** (manifest stores `enum.name`), not the `Kind`/`Source` enums.
- **Top-40 truncation is an accepted approximation:** a query term outside an entry's top-40
  scores `f=0` for the body. Documented in the tool description; `titleScore`/`tagScore`
  (computed live over the full title/tags) rescue misses. `df` MUST be computed over full
  bodies using each document's **distinct** token set (df = doc count, not corpus tf).
- **Determinism:** kotlinx `Json` does not sort map keys. BOTH `termFrequencies` AND
  `documentFrequencies` must be built into ascending-ordered `LinkedHashMap`s. A determinism
  test builds the manifest twice from shuffled input and asserts byte-identical JSON.
- `ManifestEntry.id` = the **top-level** `CorpusEntry.id` (post-merge), not `frontmatter.id`.

### 10.3 Tokenizer contract (frozen; indexing == querying)

camelCase split runs on the original-case run, THEN lowercase the pieces (order is mandatory).
FQN emission is additive (whole dotted token + each segment) and inflates df/tf uniformly —
acceptable, but query tokenization MUST use the identical `Tokenizer` instance. `LightStemmer`
mangles some words (`status`→`statu`); acceptable because symmetric across index and query —
locked by `LightStemmerTest` cases (`status`, `class`, `ss`-words, short-token skip).

### 10.4 Retrieval / overlay contract (Part 4)

- `ContextRetriever.search(query, limit, graph): SearchResult` = `Ok(hits, query)` | `Empty(query)`
  | `Unavailable(reason)`. `Empty` is distinct from `Ok([])` and must be surfaced, not collapsed.
- `ContextRetriever.section(id, maxTokens, offset): SectionResult` =
  `Ok(id,title,text,returnedTokens,truncated,nextOffset)` | `NotFound(id)` | `Unavailable(reason)`.
  There is no `getBody`/`BodyResult`. Pagination is the `nextOffset` round-trip.
- Min-max BM25 normalization must define the degenerate case (single / all-equal candidate →
  `1.0`, never NaN). **Drop the `recency` signal** unless Part 3 emits a real per-entry
  timestamp — manifest array order is not semantically ordered.
- `TtlCache` is `core/internal/TtlCache` (NOT `util/`); its clock is injectable — inject it in
  tests instead of sleeping.
- **Overlay stats are approximate:** merging the writable overlay cannot recompute `df` without
  re-tokenizing bodies. Decision: accept top-40-approximate stats after overlay merge and
  document it; do not claim exact recompute.
- `TokenBudgeter` stays in char space end-to-end: `returnedChars = result.text.length`,
  `nextOffset = offset + returnedChars`; clamp `offset` to `[0, body.length]` before `substring`.

### 10.5 MCP tools / Gradle (Part 5)

- Tools map the sealed Part-4 results: `NotFound` → `ERROR` (or a NOT_FOUND status),
  `Unavailable` → `UNAVAILABLE`, `Empty` → `OK` with empty hits + a flag. `*__get` tools take
  `offset: Int = 0` so `nextOffset` pagination works.
- `SearchHit` keeps `matchedTerms` and `source`; **drop the fabricated `snippet`** (Part 4 does
  not provide one; deriving it per hit costs 10 extra calls and risks the 10 s budget).
- **Avoid the generic `StatusEnvelope<T>`** — a parameterized `@Serializable` is the exact shape
  most likely to trip the IDE's two-classloader serialization bridge (`"Result type X is not
  serializable"`). Use concrete per-payload envelopes, or smoke-test the generic against the
  real bridge before committing.
- Shared pure logic goes in a new **`:corpus-core` subproject** (consumed by both the main
  module and the build task); `buildSrc` cannot depend on a main-module package. It coexists
  with the existing `:doc-processor` / `:kotlin-compiler-wrapper` subprojects — keep it pure-JVM
  (no IntelliJ types). Wire `assemble`/`validate` to the KSP task lazily
  (`tasks.withType<KspTask>()`), not by the string `"kspKotlin"`. `@InputDirectory`/`@InputFiles`
  need `@PathSensitive(RELATIVE)`; `generatedForBuild` must be a stable build id (no timestamp)
  or it defeats caching and reproducibility.

### 10.6 SourceCleaner caveat (Part 2)

The per-line string-literal guard is not reliably implementable with regex (multiline `"""`,
escaped quotes, `foo$1` inside strings). Ship it as **best-effort with an idempotency test** and
a documented "may false-positive inside string literals" caveat, or do a real lexer pass.
Reconsider the `// imports collapsed` sentinel — it injects a comment into generated content
right after rule 1 strips comments; prefer dropping the import run outright.

### 10.7 Action items before Phase 1 coding

1. Freeze §10.1 types in `context/` first; every other part imports them.
2. Re-base Part 2 on `ParsedDocument` (nullable frontmatter) + mandatory
   `FrontmatterValidator.validate(...)` call; fix all `ValidationIssue` constructions.
3. Re-base Part 4 ranker on `ManifestEntry` (no `header.terms`); fix degenerate normalization;
   drop `recency`.
4. Re-base Part 5 seam on `search()/section()`; de-generic the envelope; add `:corpus-core`.
5. Add the determinism (byte-identical JSON) test and the overlay-approximate-stats note.
