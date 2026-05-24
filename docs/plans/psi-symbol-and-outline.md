# `psi.symbol_at` + `psi.get_outline`

## Purpose & motivation

Two complementary "cheap navigation" tools that fill the gap between the leaf-level
`psi.get_structure` (5 000-node AST dumps) and the heavy `psi.find_usages`
(project-wide search). Today an agent that wants to answer "what's the thing under
the cursor?" or "what methods are in this file?" has only `psi.get_structure`,
which walks every token in the file's view provider — typically thousands of
nodes — just to surface the one declaration the user cares about.

`psi.symbol_at` returns ONE compact symbol description for a given position,
disambiguating reference vs. declaration; `psi.get_outline` returns just the
declaration tree (classes, methods, fields, properties — bodies omitted), about
an order of magnitude cheaper than the full structure dump. Both mirror the
day-to-day developer flows IntelliJ already supports — Show Symbol Info /
Structure tool window — and complement, but do not duplicate, JetBrains'
built-in MCP `get_symbol_info` (less rich kind taxonomy, no explicit
reference-vs-declaration flag) and `generate_psi_tree` (takes a raw code string,
not a file — different shape entirely).

**Success criterion**: an agent can answer "what symbol is at line 42?" or
"list the methods of `MyService.kt`" in ONE MCP call returning <100 fields,
without ever fetching the 5 000-node `psi.get_structure` response.

## Tool specification

### `psi.symbol_at`

**Signature:**

```kotlin
@McpTool(name = "psi.symbol_at")
@McpDescription("""…see below…""")
suspend fun psi_symbol_at(
    @McpDescription("VFS URL of the file. null → active editor tab.")
    fileUrl: String? = null,
    @McpDescription("Document offset. Alternative to line+column.")
    offset: Int? = null,
    @McpDescription("1-based line. Alternative to `offset`.")
    line: Int? = null,
    @McpDescription("1-based column. Alternative to `offset`.")
    column: Int? = null,
    @McpDescription("Include KDoc / JavaDoc text from the resolved declaration. Default true.")
    includeDoc: Boolean = true,
    @McpDescription("Max chars of doc text returned. Longer is suffixed with '…'. Default 400.")
    truncateDocAt: Int = 400,
): SymbolAtResponse
```

**`@McpDescription` draft** (verbatim — trim-margin):

```
|Returns ONE compact description of the symbol at the given position. Cheap one-shot:
|no PSI tree walk, no project-wide search. Equivalent to JetBrains' `get_symbol_info`
|but with a richer kind taxonomy and explicit reference-vs-declaration disambiguation.
|
|Use this when:
|  - The user asks "what is this thing?" / "what's under the cursor?" / "what does
|    this identifier mean?".
|  - You want a single FQN before calling psi.find_usages or code.get_class_source.
|  - You need to disambiguate "is the cursor on a usage or on the declaration itself?"
|    (the `isReference` flag answers this — when true, `name`/`kind`/`fqn` describe the
|    resolved DECLARATION, not the use site).
|
|Do NOT use this when:
|  - You need every reference / usage in the file (use psi.get_references with
|    scope="file" or psi.find_usages).
|  - You want the full PSI subtree at this position (use psi.get_structure).
|  - You want the list of methods/fields of the whole file (use psi.get_outline).
|
|Position: pass `offset` OR `line`+`column` (1-based).
|
|Returns: SymbolAtResponse { fileUrl, offset, position {line,column},
|symbol: SymbolInfo? }. SymbolInfo carries:
|  - name                — the simple name ("foo", "MyClass"), null for anonymous
|  - kind                — one of: class | interface | enum | annotation | record |
|                          object | companion | method | constructor | field |
|                          property | parameter | variable | typeAlias | enumConstant |
|                          import | label | unknown
|  - fqn                 — FQN for top-level / member declarations; null for locals
|  - psiClass            — simple PSI class name ("KtNamedFunction", "PsiMethod")
|  - declarationRange    — absolute range of the declaration in its file
|  - containingDeclarationName — enclosing method/class/file name for human grouping
|  - modifiers           — PSI modifier set (subset of public/protected/private/static/
|                          final/abstract/...)
|  - returnType          — only set for method/constructor — text form (e.g. "List<String>")
|  - typeText            — only set for field/variable/parameter/property — text form
|  - isReference         — true if the cursor is on a USAGE; name/kind/fqn describe
|                          the resolved declaration. false if cursor is on the
|                          declaration itself
|  - docText             — KDoc / JavaDoc / docstring of the resolved declaration,
|                          truncated. Null when includeDoc=false or no doc present.
|
|When the position resolves to nothing (caret on whitespace, comment, EOF, binary
|file): symbol = null and a warning is appended.
|
|Examples:
|  fileUrl=null, line=12, column=8     — symbol under caret at row 12 col 8 of active tab
|  fileUrl="file:///…/Foo.kt", offset=420
|  includeDoc=false                    — skip KDoc lookup for speed
```

**Args** — every parameter, type, default, validation:

- `fileUrl: String? = null` — VFS URL; `null` → active editor tab. Same resolution
  helper as `psi_get_structure` (`resolveFile`).
- `offset: Int? = null` / `line: Int? = null` / `column: Int? = null` — exactly the
  same `resolveOffset` helper already in `PsiToolset`. Either `offset` OR
  `line+column` required.
- `includeDoc: Boolean = true` — fetch a KDoc / JavaDoc snippet via
  `JavaDocInfoGenerator` (Java) / `KDocFinder` (Kotlin), falling back to the raw
  comment node text.
- `truncateDocAt: Int = 400` — capped at 0..4096 like every other truncation arg.

**Response model** — append to `model/PsiInfo.kt`:

```kotlin
@Serializable
data class SymbolInfo(
    val name: String?,
    /** class | interface | enum | annotation | record | object | companion | method |
     *  constructor | field | property | parameter | variable | typeAlias |
     *  enumConstant | import | label | unknown */
    val kind: String,
    val fqn: String? = null,
    val psiClass: String,
    /** Absolute range of the DECLARATION in its containing file (NOT the cursor site). */
    val declarationRange: TextRangeInfo,
    /** VFS URL of the declaration's file — may differ from request fileUrl when isReference=true. */
    val declarationFileUrl: String,
    val containingDeclarationName: String? = null,
    val modifiers: List<String> = emptyList(),
    /** Set only for kind="method"/"constructor". */
    val returnType: String? = null,
    /** Set only for kind="field"/"property"/"variable"/"parameter". */
    val typeText: String? = null,
    /** True if the cursor was on a reference. Then name/kind/fqn describe the resolved declaration. */
    val isReference: Boolean = false,
    /** Truncated KDoc / JavaDoc text of the resolved declaration. Null when absent or includeDoc=false. */
    val docText: String? = null,
)

@Serializable
data class SymbolAtResponse(
    val fileUrl: String,
    val offset: Int,
    val position: LineColumn,
    val symbol: SymbolInfo? = null,
    val warnings: List<String> = emptyList(),
)

@Serializable
data class LineColumn(val line: Int, val column: Int)
```

### `psi.get_outline`

**Signature:**

```kotlin
@McpTool(name = "psi.get_outline")
@McpDescription("""…see below…""")
suspend fun psi_get_outline(
    @McpDescription("VFS URL of the file. null → active editor tab.")
    fileUrl: String? = null,
    @McpDescription("Include fields/properties as outline nodes. Default true.")
    includeFields: Boolean = true,
    @McpDescription("Include inherited members (Structure View 'Show Inherited'). Default false.")
    includeInherited: Boolean = false,
    @McpDescription("Max outline tree depth. Default 6.")
    maxDepth: Int = 6,
    @McpDescription("Hard cap on outline nodes. Default 500.")
    maxNodes: Int = 500,
): GetOutlineResponse
```

**`@McpDescription` draft** (verbatim):

```
|Returns the Structure View / Outline of a file — only top-level and nested declarations
|(classes, interfaces, methods, fields, properties, top-level functions) as a tree. Skips
|bodies, statements, expressions, comments — about an order of magnitude cheaper than
|psi.get_structure (which walks every leaf token). Matches what the Structure tool window
|displays.
|
|Use this when:
|  - The user asks "what methods are in this file?" / "show me the structure of foo.kt".
|  - You want a navigable index of declarations before drilling in with
|    code.get_class_source or psi.symbol_at.
|  - You need a per-language outline that respects the language's structure-view
|    contributions (Kotlin's companion-object grouping, Java's nested classes, etc.).
|
|Do NOT use this when:
|  - You need the AST including expressions/tokens (use psi.get_structure).
|  - You need one specific symbol (use psi.symbol_at — single round-trip).
|  - The file isn't open in the editor AND has no language plugin loaded (binary file,
|    plain text without a structure view): the response will be empty with a warning.
|
|Backed by IntelliJ's `StructureViewBuilder` / `StructureViewModel` — the same per-language
|extension that powers the Structure tool window. Each language plugin contributes its own
|treeBuilder, so the outline matches what a developer sees in the IDE.
|
|Returns: GetOutlineResponse { fileUrl, fileType, language, nodes: OutlineNode[],
|nodeCount, truncated, warnings }. Each OutlineNode carries:
|  - name                — display name (e.g. "foo(int): String", "MyClass")
|  - kind                — same taxonomy as psi.symbol_at.kind
|  - fqn                 — top-level / member FQN; null for nested locals
|  - psiClass            — simple PSI class name
|  - declarationRange    — absolute range in the host file
|  - modifiers           — PSI modifier set
|  - returnType          — for methods
|  - typeText            — for fields / properties
|  - children            — nested declarations (inner classes, members of a class)
|
|Cost: O(declarations), typically <100 per source file. Capped at maxNodes (default 500).
|
|Examples:
|  fileUrl=null                              — outline of active tab
|  fileUrl="file:///…/Foo.kt"                — explicit file
|  includeFields=false                       — methods-only outline
|  includeInherited=true                     — fold in superclass members
```

**Args** — `fileUrl`, `includeFields`, `includeInherited`, `maxDepth ∈ 1..50`,
`maxNodes ∈ 1..5_000`.

**Response model** — append to `model/PsiInfo.kt`:

```kotlin
@Serializable
data class OutlineNode(
    val name: String,
    val kind: String,
    val fqn: String? = null,
    val psiClass: String,
    val declarationRange: TextRangeInfo,
    val modifiers: List<String> = emptyList(),
    val returnType: String? = null,
    val typeText: String? = null,
    val children: List<OutlineNode> = emptyList(),
)

@Serializable
data class GetOutlineResponse(
    val fileUrl: String,
    val fileType: String,
    val language: String,
    val nodes: List<OutlineNode> = emptyList(),
    val nodeCount: Int = 0,
    val truncated: Boolean = false,
    val warnings: List<String> = emptyList(),
)
```

## IntelliJ APIs used

### `psi.symbol_at`

- `com.intellij.psi.PsiManager` / `PsiFile.findElementAt(offset)` — leaf at offset.
- `com.intellij.lang.injection.InjectedLanguageManager.findInjectedElementAt` —
  injection-aware variant (same approach `PsiUsageSearcher.resolveTarget` uses).
- `com.intellij.psi.PsiFile.findReferenceAt(offset)` →
  `PsiReference.resolve()` / `PsiPolyVariantReference.multiResolve(true)`.
- `com.intellij.psi.util.PsiTreeUtil.getNonStrictParentOfType` — walk up to the
  nearest `PsiNameIdentifierOwner` / `PsiNamedElement`.
- `com.intellij.psi.PsiNameIdentifierOwner` / `PsiNamedElement` — declaration markers.
- `com.intellij.psi.PsiModifierList` (Java) — modifiers, via existing
  `core/PsiModifiers.kt`.
- Kotlin: `org.jetbrains.kotlin.psi.KtModifierListOwner` / `KtNamedDeclaration`
  for `modifiers` + `name`; FQN via `KtNamedDeclaration.fqName?.asString()`.
  Loaded reflectively (Kotlin module is optional — same pattern as `ExecToolset`)
  and gracefully skipped when not present.
- Docs: `com.intellij.lang.documentation.DocumentationProvider` via
  `com.intellij.lang.documentation.LanguageDocumentation` extension. Cheaper
  fallback: `PsiDocCommentOwner.docComment.text` (works for Java + Kotlin via
  KDoc-as-PsiComment).

### `psi.get_outline`

- `com.intellij.ide.structureView.StructureViewBuilder` —
  `LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(psiFile)`. Returns
  `null` for files without a structure-view extension (binary, plain text).
- `com.intellij.ide.structureView.TreeBasedStructureViewBuilder` — provides
  `createStructureViewModel(editor: Editor?)`. Pass `null` editor — we don't need it.
- `com.intellij.ide.structureView.StructureViewModel.getRoot()` returns a
  `StructureViewTreeElement`; `.children` are `TreeElement[]`.
- Each `TreeElement.value` is the underlying `PsiElement`; `getPresentation()` gives
  the display name + icon hint we don't need.
- For grouping (e.g. Kotlin companion-object groups, "Properties" / "Methods"
  Groupers from `Grouper[]`): exposed by `StructureViewModel.getGroupers()`. See
  open question 1 — default plan is "ignore groupers, flatten into tree by raw
  children".
- **Stability**: `StructureViewBuilder` is stable, documented platform API
  ([JetBrains plugin docs — Structure View](https://plugins.jetbrains.com/docs/intellij/structure-view.html)).

- Fallback per-language walker (Java only) if StructureViewModel is unavailable:
  `com.intellij.psi.PsiJavaFile.classes` → `PsiClass.innerClasses + methods + fields`.
  No Kotlin fallback — when Kotlin plugin is loaded its `KotlinStructureViewFactory`
  is always available.

## Threading & EDT model

Both tools run entirely under `readActionBlocking { … }` — same pattern as the
existing `psi_get_structure` / `psi_get_references`. No EDT bouncing for either:

- `findElementAt`, `findReferenceAt`, `PsiTreeUtil.getNonStrictParentOfType`,
  `PsiReference.resolve()` — pure PSI, require only the platform read action.
- `StructureViewBuilder.createStructureViewModel(null)` — also read-action-safe;
  the contract is that `getRoot()` / `getChildren()` are pure when the model is
  built with a null editor.
- Wrap in `DumbService.computeWithAlternativeResolveEnabled<T, RuntimeException>`
  for `symbol_at` (we resolve references) — same defence as the existing
  `psi_get_references` impl. `get_outline` skips this; structure-view walking
  doesn't touch the resolver.
- Active-editor caret lookup (when `fileUrl=null`) goes through the existing
  `resolveFile` helper which uses `FileEditorManager.selectedFiles` — that part is
  wrapped on the EDT inside `caretOffsetOf` already.

No new cache. Both responses are cheap enough on the per-call hot path that
caching adds complexity without payoff; a TTL cache would risk staleness in the
common edit-then-query loop.

## Timeout strategy

Hard 10 s cap inherited from `readActionBlocking`. Realistic worst case:

- `symbol_at` on a Kotlin file with deep type inference: <50 ms in practice.
  `findReferenceAt` is O(1) per registered contributor for the host element.
- `get_outline` on a 10 000-line generated Java file with 500 methods: <200 ms.
  The structure-view tree builder is cached per-file by the platform; our walk is
  O(declarations) which the `maxNodes` cap (default 500) bounds anyway.

If a pathological structure-view contributor blocks, the read-action's 10 s
timeout aborts the call and the agent sees `McpExpectedError("Timed out…")` —
same surface as the other `psi.*` tools.

## Edge cases

1. **Offset past EOF** — `resolveOffset` already clamps to `document.textLength`.
   `findElementAt(textLength)` returns null; we report `symbol = null` plus a
   `"position past end of file"` warning rather than throwing.
2. **Whitespace-only / comment position** — `findElementAt` returns a leaf
   (`PsiWhiteSpace` / `PsiComment`). `getNonStrictParentOfType(PsiNamedElement)`
   walks up; if nothing named is found within the file, `symbol = null` with a
   `"no declaration at position"` warning.
3. **Injected fragments** — for a position inside an SQL-in-Kotlin string, use
   `InjectedLanguageManager.findInjectedElementAt` first (mirrors
   `PsiUsageSearcher.resolveTarget`). The injected PSI's named ancestors win over
   the host string literal — agent sees the SQL identifier, not the Kotlin
   `KtLiteralStringTemplateEntry`.
4. **Files without a StructureViewBuilder** (binary, plain text, unknown
   extension) — `LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder`
   returns null; `get_outline` returns `nodes=[]` plus a warning
   `"No StructureViewBuilder for fileType=$fileType"`. Same response model, no
   throw.
5. **Anonymous classes / lambdas in outline** — by default the platform's
   `TreeBasedStructureViewBuilder` for Java *excludes* anonymous-class bodies from
   the structure view, and `KotlinStructureViewFactory` mirrors that for
   lambdas. We follow the platform's choice (anonymous = NOT a node). For
   `symbol_at` they're still resolvable as `kind="class"` / `kind="method"` with
   `name=null`.
6. **File closed in editor** (caller passes `fileUrl` of a file not currently
   open) — `VirtualFileManager.findFileByUrl` + `PsiManager.findFile` works
   without a `FileEditor`. No editor selection needed.
7. **No focused project** — handled by existing `requireProject()` in
   `PsiToolset`.
8. **Polyvariant reference at position** — for `symbol_at`, pick the first
   target (`multiResolve(true).firstOrNull()`) and warn
   `"$N other resolutions available — use psi.get_references for the full set"`.
9. **Local variable / parameter** — `fqn = null` (locals have no FQN); `kind` is
   `"variable"` / `"parameter"`; `containingDeclarationName` is the enclosing
   function name so the agent has context.
10. **Light-PSI elements** — synthetic PSI from compilers (e.g. Kotlin's
    `@JvmStatic`-generated companions) may report empty `name`. Fall through to
    `name = null, kind = "unknown"` with a warning rather than crash.
11. **Outline with `maxNodes` hit mid-class** — emit the partial tree, set
    `truncated = true`, warn `"outline truncated at $maxNodes nodes"`.

## Files to create/modify

| Path | Op | What |
|------|----|------|
| `tools/PsiToolset.kt` | Edit | Add `psi_symbol_at` + `psi_get_outline` methods (≈40 LOC + descriptions) |
| `core/PsiSymbolResolver.kt` | Create | Headless `resolveAt(psiFile, offset) → SymbolInfo?` — wraps findElementAt + findReferenceAt + walk-up. Reuses `PsiUsageSearcher.resolveTarget`'s pattern but returns a richer info record |
| `core/PsiOutlineCollector.kt` | Create | Headless `collect(psiFile, includeFields, includeInherited, maxDepth, maxNodes) → GetOutlineResponse` — wraps `StructureViewBuilder` |
| `core/PsiKindClassifier.kt` | Create (small) | Pure-Java/Kotlin classifier mapping `PsiElement` → kind string. Shared by symbol_at and outline |
| `model/PsiInfo.kt` | Edit | Append `SymbolInfo`, `SymbolAtResponse`, `LineColumn`, `OutlineNode`, `GetOutlineResponse` |
| `src/test/kotlin/.../core/PsiKindClassifierTest.kt` | Create | Unit — synthetic `PsiElement` mocks, kind mapping table |
| `src/test/kotlin/.../core/platform/PsiSymbolResolverPlatformTest.kt` | Create | Platform — Java + Kotlin fixtures |
| `src/test/kotlin/.../core/platform/PsiOutlineCollectorPlatformTest.kt` | Create | Platform — Java + Kotlin fixtures, includeFields toggle |

No new META-INF wiring: both tools live in `psi.*` and the existing
`PsiToolset` is already registered through `mcp-integration.xml`.

## Test plan

**Unit (`PsiKindClassifierTest.kt`)** — pure JVM:

- `classifies PsiClass → 'class'` / interface / enum / annotation / record.
- `classifies PsiMethod → 'method' / 'constructor'` (via `isConstructor`).
- `classifies PsiField → 'field'`; `PsiParameter → 'parameter'`;
  `PsiLocalVariable → 'variable'`.
- `classifies Kotlin KtClass / KtObjectDeclaration / KtNamedFunction /
  KtProperty / KtParameter` — through reflection-only access (Kotlin class
  presence is optional).
- `unknown element → 'unknown'`.

**Platform (`PsiSymbolResolverPlatformTest.kt`)** — extends `BasePlatformTestCase`,
fixtures under `src/test/testData/psi/`:

- `testSymbolOnDeclarationName` — caret on `class Foo` returns
  `kind="class", name="Foo", fqn="pkg.Foo", isReference=false`.
- `testSymbolOnUsage` — caret on a call site of `foo()` returns
  `kind="method", name="foo", isReference=true,
  declarationFileUrl=<same file or jar URL>`.
- `testSymbolOnLocalVariable` — `fqn=null, kind="variable",
  containingDeclarationName="bar"`.
- `testSymbolPastEOF` — `symbol=null`, warning present.
- `testSymbolOnWhitespace` — `symbol=null`, warning present.
- `testSymbolInsideInjection` — SQL-in-Kotlin-string: `kind="variable"` (or
  whatever the injected fragment says), not Kotlin.
- `testSymbolPolyvariant` — overloaded Java method call: first resolution
  returned, warning lists the count.
- `testKotlinFixture` — basic property + companion-object resolution, only
  asserted when Kotlin module is loaded in the test sandbox.

**Platform (`PsiOutlineCollectorPlatformTest.kt`)**:

- `testJavaOutlineHasClassAndMethods` — `class Foo { void bar(){} int x; }`
  produces one class node with `bar`/`x` children; check FQN, modifiers.
- `testKotlinOutlineHasTopLevelFunctions` — `fun greet()` appears as a
  top-level node.
- `testIncludeFieldsFalseSkipsFields` — toggle removes `int x`.
- `testIncludeInheritedTrueAddsSuperclassMethods` — declares `class B extends A`,
  outline of B with `includeInherited=true` contains an A-defined method.
- `testNoStructureViewBuilderReturnsEmptyWithWarning` — `.bin` fixture.
- `testMaxNodesTruncation` — synthesize a class with 600 methods, request
  `maxNodes=100`, assert `truncated=true` and 100 nodes returned.
- `testAnonymousClassExcluded` — confirm platform behaviour mirrored.

## Estimated effort

| Step | Hours |
|------|-------|
| `SymbolInfo` / `OutlineNode` models + LineColumn | 0.5 |
| `PsiKindClassifier` + unit tests | 1 |
| `PsiSymbolResolver` (symbol_at logic) | 2.5 |
| `PsiOutlineCollector` (StructureViewModel walker) | 3 |
| `PsiToolset` two methods + `@McpDescription` strings | 1 |
| Platform tests (symbol_at + outline) | 2.5 |
| Doc-text fetch + truncation | 1 |
| Doc-gen verification + manual `runIde` smoke | 0.5 |
| **Total** | **~1.5 days** |

## Open questions / risks

1. **Outline groupers** — `StructureViewModel.getGroupers()` provides language-
   specific groupings (Kotlin's "Show Companion Objects Grouped", Java's
   "Group Methods by Defining Type"). Skipping them keeps the API flat and
   predictable; surfacing them would require an extra `groupBy: String?` arg per
   language. **Decision in this plan: skip for v1**; revisit if users ask. The
   raw children path matches what the Structure tool window shows with all
   groupers disabled (which is the default in most fresh IDE installs).
2. **`docText` / qualifiedDocText** — JetBrains' `get_symbol_info` returns a
   doc snippet. We follow precedent (`includeDoc=true` by default), but the
   formatter choice matters: `DocumentationProvider.generateDoc` produces HTML;
   `PsiDocCommentOwner.docComment.text` is raw text. Plan: use raw text (cheap,
   no formatter dependency), document in `@McpDescription` that markdown is
   not rendered.
3. **Outline for non-source files** — `.json`, `.yaml`, `.xml` all have
   StructureViewBuilders (the JSON outline shows keys, YAML shows the document
   tree). We pass them through unchanged; the `kind` field will be `"unknown"`
   for non-symbol entries. Could be filtered with a `sourceOnly: Boolean` arg.
   Defer until usage data argues for it.
4. **Performance of `findReferenceAt` for languages with heavy reference
   contributors** (PHP, Vue) — typically still <100 ms but worth measuring.
   Same risk surface as `psi_get_references` with `scope="at_offset"`, which
   already ships.
5. **`isReference=true` and the declaration lives in a library jar** — the
   `declarationFileUrl` becomes a `jar://…!/path/Foo.class` URL. Agents need
   to feed this into `code.get_class_source` (which already handles jar URLs);
   document the cross-reference in the description so callers know the
   follow-up.

## References

- Existing similar code:
  - `tools/PsiToolset.kt#psi_get_references` — same position-resolution pattern
    (`resolveFile` + `resolveOffset`).
  - `core/PsiUsageSearcher.kt#resolveTarget` — the exact target-resolution
    pattern we reuse for `symbol_at`.
  - `core/PsiModifiers.kt` — modifier vocabulary already in place.
- IntelliJ source:
  - `StructureViewBuilder`:
    https://github.com/JetBrains/intellij-community/blob/master/platform/editor-ui-api/src/com/intellij/ide/structureView/StructureViewBuilder.java
  - `TreeBasedStructureViewBuilder`:
    https://github.com/JetBrains/intellij-community/blob/master/platform/editor-ui-api/src/com/intellij/ide/structureView/TreeBasedStructureViewBuilder.java
  - `PsiNameIdentifierOwner`:
    https://github.com/JetBrains/intellij-community/blob/master/platform/core-api/src/com/intellij/psi/PsiNameIdentifierOwner.java
  - Structure View docs:
    https://plugins.jetbrains.com/docs/intellij/structure-view.html
- JetBrains MCP equivalents:
  - `get_symbol_info` — partial overlap with `psi.symbol_at`. Theirs lacks the
    `isReference` flag and uses a coarser kind taxonomy (class/method/field
    only); ours adds parameter, variable, typeAlias, enumConstant, label,
    companion, import.
  - `generate_psi_tree` — takes a RAW CODE STRING, not a file; different use
    case. `psi.get_outline` is net-new.
