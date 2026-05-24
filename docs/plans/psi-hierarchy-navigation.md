# `psi.type_hierarchy` + `psi.goto_implementation`

## Purpose & motivation

Two closely-related PSI navigation tools that fill obvious gaps in the existing
`psi.*` group AND in JetBrains' built-in MCP server (which has neither):

- **`psi.type_hierarchy`** — the agent equivalent of IntelliJ's Hierarchy tool
  window (Ctrl+H). For a class (resolved by FQN OR by file+offset), return its
  supertype / subtype hierarchy as a tree, with depth + node caps.
- **`psi.goto_implementation`** — Ctrl+Alt+B "Goto Implementation" for an
  interface / abstract class / method. The dedicated focused-result tool an
  agent reaches for when answering "what concretely implements this?".

Today `psi.find_usages` with `includeImplementations=true` partially covers
goto-impl, but lumps overrides into a usages list and only accepts a position
(no FQN entry point). It also can't render a multi-level type tree. These two
tools are the focused, navigation-shaped answer.

**Success criterion**: an agent can answer (1) "what is the supertype chain of
`com.intellij.openapi.editor.Editor`?" and (2) "list every concrete
implementation of `com.intellij.openapi.fileEditor.FileEditorProvider`" in one
MCP call each, without having to first open a file or call `psi.find_usages` +
hand-filter `kind=="implementation"`.

## Tool specification

### `psi.type_hierarchy`

**Signature:**

```kotlin
@McpTool(name = "psi.type_hierarchy")
@McpDescription("""…see verbatim block below…""")
suspend fun psi_type_hierarchy(
    @McpDescription("Fully-qualified class name. Mutually exclusive with file+offset; takes precedence when both are supplied.")
    target: String? = null,
    @McpDescription("VFS URL of the file containing the class declaration / reference. null → active editor tab. Used when `target` is null.")
    fileUrl: String? = null,
    @McpDescription("Document offset on a class declaration or class reference. Alternative to line+column. Used when `target` is null.")
    offset: Int? = null,
    @McpDescription("1-based line number. Alternative to `offset`.")
    line: Int? = null,
    @McpDescription("1-based column number. Alternative to `offset`.")
    column: Int? = null,
    @McpDescription("Hierarchy direction: \"up\" (supertypes only), \"down\" (subtypes only), \"both\" (default).")
    direction: String = "both",
    @McpDescription("Search scope for subtype walk: \"file\" / \"project\" (default) / \"all\" (includes library sources — slow).")
    scope: String = "project",
    @McpDescription("Max depth from the target node in either direction. Default 5.")
    maxDepth: Int = 5,
    @McpDescription("Hard cap on total nodes across supertypes + subtypes. Default 200.")
    maxNodes: Int = 200,
): TypeHierarchyResponse
```

**`@McpDescription` draft** (copy-paste verbatim into the source — trim-margin
format, the reflection bridge strips margins automatically):

```
|Returns the type hierarchy of a class — supertypes (parents) and/or subtypes
|(implementors / extenders) — as a tree rooted at the target class. Mirrors the
|IntelliJ Hierarchy tool window (Ctrl+H, "Type Hierarchy").
|
|Use this when:
|  - The agent needs to know what a class extends / implements ("up" / supertypes).
|  - The agent needs to know who extends or implements a class ("down" / subtypes).
|  - The user asks "show me the class hierarchy of X" or "what implements Y?" and
|    a multi-level tree is more useful than a flat list.
|  - You want a quick exhaustiveness check on a sealed class — every direct
|    subtype is included and the response flags `isSealed`.
|
|Do NOT use this when:
|  - You only need concrete implementations of an interface / abstract member —
|    psi.goto_implementation is more focused and returns method signatures.
|  - You want references / call sites — that is psi.find_usages.
|  - You want method-level hierarchy (overrides of one method) — also
|    psi.goto_implementation.
|
|Target resolution: pass either `target` (FQN, takes precedence) or a position
|(fileUrl + offset OR line+column) pointing at a class declaration or a class
|reference. Anonymous and local classes ARE recognised at a position but never
|surface as subtype nodes (they have no FQN).
|
|Scope (default "project"):
|  - "file"    — subtype walk restricted to one file (rarely useful).
|  - "project" — walks project sources only. Standard default.
|  - "all"     — includes library sources. For an interface like java.util.List
|                or a marker like java.lang.Object the subtype walk can saturate
|                the 10s read-action timeout. A warning is appended; prefer
|                "project" scope and narrow further only when needed.
|
|Caps: maxDepth (default 5) and maxNodes (default 200) bound the walk. When a
|cap trips the response's `truncated` flag is set; the truncated subtree's leaf
|carries `childrenTruncated=true` so the agent knows which branch was cut.
|
|Returns: { target: { fqn, psiClass, fileUrl?, declarationRange?, isInterface,
|isAbstract, isFinal, isSealed }, supertypes: HierarchyNode?, subtypes:
|HierarchyNode?, truncated, warnings[] }. Each HierarchyNode mirrors the same
|shape with a children: HierarchyNode[] array (parents for supertypes, child
|classes for subtypes). java.lang.Object is included as the supertype root when
|walking "up" but its subtype walk is always rejected (would be the world).
|
|Examples:
|  target="com.intellij.openapi.editor.Editor"         — both directions, project scope
|  target="java.util.List", direction="up"             — just the super-interfaces
|  fileUrl=null, line=42, column=14, direction="down"  — subtypes of the class under the caret in the active editor
|  target="com.acme.Sealed", direction="down"          — exhaustive sealed-subtype list
```

**Args** — every parameter, type, default, validation:

- `target: String? = null` — FQN. When non-null, position args are ignored.
- `fileUrl, offset, line, column` — same resolution rules as `psi.find_usages`.
  Required when `target == null`. (One of {offset} OR {line, column} must be set
  to pick a position; otherwise we error out.)
- `direction: "up" | "down" | "both"` — default `"both"`. Validate, throw
  `McpExpectedError` on invalid.
- `scope: "file" | "project" | "all"` — default `"project"`. Validate.
- `maxDepth: Int = 5` — bounded `1..20`.
- `maxNodes: Int = 200` — bounded `1..5_000`.

**Response model** — under `model/PsiInfo.kt` (append):

```kotlin
@Serializable
data class HierarchyClassRef(
    val fqn: String?,                   // null for anonymous / local
    val psiClass: String,               // simple-name of the PsiClass impl
    val fileUrl: String? = null,
    val declarationRange: TextRangeInfo? = null,
    val isInterface: Boolean = false,
    val isAbstract: Boolean = false,
    val isFinal: Boolean = false,
    val isSealed: Boolean = false,
    val modifiers: List<String> = emptyList(),
)

@Serializable
data class HierarchyNode(
    val node: HierarchyClassRef,
    val children: List<HierarchyNode> = emptyList(),
    val childrenTruncated: Boolean = false,
)

@Serializable
data class TypeHierarchyResponse(
    val target: HierarchyClassRef,
    val supertypes: HierarchyNode? = null,
    val subtypes: HierarchyNode? = null,
    val direction: String,
    val scope: String,
    val truncated: Boolean = false,
    val warnings: List<String> = emptyList(),
)
```

Args mirror in `model/args/PsiArgs.kt`:

```kotlin
@Serializable
data class TypeHierarchyArgs(
    val target: String? = null,
    val fileUrl: String? = null,
    val offset: Int? = null,
    val line: Int? = null,
    val column: Int? = null,
    val direction: String = "both",
    val scope: String = "project",
    val maxDepth: Int = 5,
    val maxNodes: Int = 200,
)
```

### `psi.goto_implementation`

**Signature:**

```kotlin
@McpTool(name = "psi.goto_implementation")
@McpDescription("""…see verbatim block below…""")
suspend fun psi_goto_implementation(
    @McpDescription("VFS URL of the file. null → active editor tab.")
    fileUrl: String? = null,
    @McpDescription("Document offset on a method, interface, or abstract class. Alternative to line+column.")
    offset: Int? = null,
    @McpDescription("1-based line number. Alternative to `offset`.")
    line: Int? = null,
    @McpDescription("1-based column number. Alternative to `offset`.")
    column: Int? = null,
    @McpDescription("Search scope: \"file\" / \"project\" (default) / \"all\" (includes library sources — slow).")
    scope: String = "project",
    @McpDescription("Hard cap on returned implementations. Default 200.")
    maxResults: Int = 200,
): GotoImplementationResponse
```

**`@McpDescription` draft** (verbatim):

```
|Returns every concrete implementation / override of the symbol at a given
|position — interfaces and abstract classes resolve to their concrete extenders,
|abstract / interface methods resolve to their concrete overrides. Equivalent to
|IntelliJ's Ctrl+Alt+B "Goto Implementation".
|
|Use this when:
|  - You see an interface or abstract method in code and need the concrete
|    implementations.
|  - The agent is tracing a call graph through an abstraction boundary
|    (interface, SPI, framework hook).
|  - You want a focused answer to "what overrides this method?" without the
|    noise of usages / reference sites that psi.find_usages would return.
|
|Do NOT use this when:
|  - You want call sites of a method (read/write/invoke locations) — use
|    psi.find_usages.
|  - You want a multi-level type tree (parents AND children, transitive) — use
|    psi.type_hierarchy.
|  - The caret is on a concrete final method — there are no overrides.
|
|Position: pass `offset` OR `line`+`column`. The caret may be on (a) a class /
|interface declaration or reference (returns subclasses / implementors), or
|(b) a method declaration or call site (returns overriding methods). The kind
|of resolution is reported in `target.kind` ("class" | "method").
|
|Scope (default "project"):
|  - "project" walks project sources only. Standard default — what Ctrl+Alt+B
|    uses by default.
|  - "all" includes library sources; on a JDK or platform symbol this can
|    saturate the 10s read-action timeout. A warning is appended.
|  - "file" only checks the same file (rarely useful but mirrors the other tools).
|
|Returns: { target: { name, psiClass, kind, fileUrl, declarationRange,
|isAbstract, isInterface }, implementations: [{ fileUrl, range, lineSnippet,
|declaringClassFqn, signature, isAbstract, isOverride }], total, truncated,
|warnings[] }. `implementations` are sorted by fileUrl then range.
|
|For method targets the response normalises the signature
|(`returnType name(paramType paramName, ...)`) so the agent can compare across
|generic-parameter substitutions; `signature` uses the *erasure* shown in the
|overrider's source, not the interface's signature.
|
|Examples:
|  fileUrl=null, line=10, column=18         — overrides of the method at row 10
|  fileUrl=null, line=5,  column=12         — implementations of the interface name on row 5
|  scope="all", maxResults=50               — include library overrides, capped
```

**Args** — every parameter, type, default, validation:

- Position args same as `psi.find_usages`. At least one of `offset` OR
  `(line, column)` is required.
- `scope: "file" | "project" | "all"` — default `"project"`. Validate.
- `maxResults: Int = 200` — bounded `1..5_000`.

**Response model** — under `model/PsiInfo.kt`:

```kotlin
@Serializable
data class ImplementationTarget(
    val name: String?,                  // method or class simple name
    val psiClass: String,               // simple-name of the PsiNamedElement impl
    val kind: String,                   // "class" | "method"
    val fileUrl: String? = null,
    val declarationRange: TextRangeInfo? = null,
    val isAbstract: Boolean = false,
    val isInterface: Boolean = false,
)

@Serializable
data class ImplementationInfo(
    val fileUrl: String,
    val range: TextRangeInfo,
    val lineSnippet: String,
    val declaringClassFqn: String? = null,
    val signature: String? = null,      // null when target is a class
    val isAbstract: Boolean = false,
    val isOverride: Boolean = false,    // true for methods (vs class-impl)
)

@Serializable
data class GotoImplementationResponse(
    val target: ImplementationTarget,
    val implementations: List<ImplementationInfo>,
    val scope: String,
    val total: Int,
    val truncated: Boolean = false,
    val warnings: List<String> = emptyList(),
)
```

Args mirror in `model/args/PsiArgs.kt`:

```kotlin
@Serializable
data class GotoImplementationArgs(
    val fileUrl: String? = null,
    val offset: Int? = null,
    val line: Int? = null,
    val column: Int? = null,
    val scope: String = "project",
    val maxResults: Int = 200,
)
```

## IntelliJ APIs used

**Class resolution from FQN (Java + Kotlin uniform):**

- `JavaPsiFacade.getInstance(project).findClass(fqn, GlobalSearchScope.allScope(project))`
  — returns a `PsiClass`. For Kotlin, `KtClass` exposes the same `PsiClass`
  surface via the light-class adapter (`KtLightClass`), so a single Java-PSI
  code path covers both languages. This is also how IntelliJ's own Hierarchy
  tool window does FQN lookups.
- Stability: `JavaPsiFacade` is **stable platform API** (not internal). Lives
  in `com.intellij.psi`. Required dependency: `com.intellij.modules.java` (we
  already gate Java-PSI features in `META-INF/java-introspect.xml`).

**Supertypes:**

- `PsiClass.getSupers(): Array<PsiClass>` — direct supers (extends +
  implements), platform-stable. Recursively expand to build the parent tree.
- For Kotlin sealed types the parent chain is identical via `KtLightClass`.

**Subtypes:**

- `com.intellij.psi.search.searches.ClassInheritorsSearch.search(psiClass, scope, /*checkDeep=*/false): Query<PsiClass>`
  — direct inheritors only; recurse manually so we control depth.
- Source: `platform/lang-impl/src/com/intellij/psi/search/searches/ClassInheritorsSearch.java`.
  Stable, used by every Hierarchy implementation.
- Returns Kotlin classes via their light-class adapters automatically — no
  Kotlin-plugin direct dependency needed.

**Goto Implementation:**

- For interfaces / abstract classes: `com.intellij.psi.search.searches.DefinitionsScopedSearch.search(element, scope, /*checkDeep=*/true): Query<PsiElement>`.
  This is the SAME query backing Ctrl+Alt+B platform-wide; works for Kotlin
  `KtClassOrObject` because the platform registers the appropriate
  `DefinitionsScopedSearchExecutor`.
- For methods: `com.intellij.psi.search.searches.OverridingMethodsSearch.search(psiMethod, scope, /*checkDeep=*/true): Query<PsiMethod>`.
  Returns concrete overrides (excludes the queried method itself).
- We already use `DefinitionsScopedSearch` in `PsiUsageSearcher.findUsages`
  when `includeImplementations=true` — same pattern.

**Sealed detection (Kotlin):**

- Probe by reflection on the simple name `"KtClass"` (lifted from
  `PsiUsageSearcher.isLocalVariableLike` pattern — no Kotlin-plugin link-time
  dependency). When `target.javaClass.simpleName == "KtClass"`, read its
  `modifierList` and check for `SEALED_KEYWORD`. Falls back to checking
  `PsiClass.hasModifierProperty("sealed")` for Java records-style sealed (Java
  17+).

**Modifier extraction:** reuse the existing `core/PsiModifiers.kt` helper —
already extracts modifier lists across Java + Kotlin.

**Position resolution:** reuse `PsiToolset.resolveFile` /
`PsiToolset.resolveOffset` (extract to a shared helper file if duplication
becomes painful; otherwise just call into the existing private methods via a
small dedicated `PsiPositionResolver` object). The `psi.find_usages` resolution
logic (`PsiUsageSearcher.resolveTarget` — follow reference if on usage,
otherwise nearest `PsiNamedElement`) is what we want for the position path too,
but we restrict the accepted ancestor types to `PsiClass` (for type_hierarchy
and class-mode goto_impl) or `PsiMethod` (for method-mode goto_impl).

## Threading & EDT model

No EDT. Both tools are pure PSI work and run inside a `readActionBlocking { … }`
(already used throughout `PsiToolset`). All search APIs cited
(`ClassInheritorsSearch`, `OverridingMethodsSearch`, `DefinitionsScopedSearch`,
`JavaPsiFacade.findClass`) respect the read-action context and check for PCE
on each result — cancellation propagates cleanly when the 10 s readAction cap
trips.

Wrap each search in
`DumbService.getInstance(project).computeWithAlternativeResolveEnabled<R, RuntimeException> { … }`
— same as `psi_find_usages`. Avoids dumb-mode failures when indexes are still
warming up.

No caching: hierarchies and overrides depend on project state and can change
between calls; a TTL cache would yield stale results. Cost is bounded by
`maxNodes` / `maxResults` and the 10 s read-action timeout.

## Timeout strategy

Hard 10 s cap per project rule (CLAUDE.md). The risky surface:

1. `ClassInheritorsSearch` on a hot interface in `scope="all"`
   (`java.util.List`, `java.lang.Object`, `java.lang.Runnable`) can return
   thousands of hits and saturate the read-action timeout.
2. `DefinitionsScopedSearch` on a popular SPI in `scope="all"` is similarly
   unbounded.
3. `OverridingMethodsSearch` on `Object.toString()` in `scope="all"` is
   pathological.

Mitigations baked into the spec:

- **Default scope is `"project"`** for both tools (not `"all"`). Project scope
  has a known bound (≈ project source files), is fast, and matches what
  IntelliJ's own Hierarchy tool window uses by default.
- **Hard caps on result count**: `maxNodes=200` (type_hierarchy),
  `maxResults=200` (goto_implementation). Trip `truncated=true` and return.
- **`scope="all"` warning**: when the caller picks `"all"`, append a warning
  to the response (`warnings: ["scope=all may saturate the 10s timeout; …"]`)
  even on success — so the agent learns to narrow on follow-ups.
- **`java.lang.Object` special-case** for type_hierarchy: when `direction`
  includes `"down"` and `target` resolves to `java.lang.Object`, return
  `subtypes=null` with a warning ("subtype walk of Object would be the world").
- **Per-result PCE check**: each `Query.forEach(Processor { … })` aborts as
  soon as cap trips (same pattern as `PsiUsageSearcher`). PCE thrown by the
  platform on cancellation is re-raised cleanly.

If a future caller genuinely needs > 10 s of search, the answer is **narrower
scope or paging**, not a timeout bump. (See CLAUDE.md "Timeouts".)

## Edge cases

1. **`target` FQN does not resolve** — `JavaPsiFacade.findClass` returns null.
   Throw `McpExpectedError("No class found for FQN: $target. Pass a position
   instead, or check the FQN.")`.
2. **Position resolves to neither a class nor a method** — for
   goto_implementation, throw `McpExpectedError("Caret is not on a class or
   method")`. For type_hierarchy, throw `McpExpectedError("Caret is not on a
   class declaration or reference")`.
3. **Target is `final` class** — `ClassInheritorsSearch` returns empty.
   `subtypes` is a single-node tree (root only, no children); `truncated=false`.
   No warning.
4. **Target is `java.lang.Object`** — subtype walk is rejected with a warning
   ("would enumerate everything"); supertype walk returns `supertypes=null`
   (no super) or the trivial root. The class itself is still described.
5. **Anonymous / local classes** — recognised at a position (their `PsiClass`
   is the on-the-fly anonymous class), reported with `fqn=null` and the
   declaring file in `fileUrl`. Excluded from subtype-walk results: most
   languages can't produce a sub-anonymous-class. Inherited anonymous classes
   that bubble up via `ClassInheritorsSearch` are kept (their `fqn=null` is
   informative).
6. **Kotlin objects / companion objects** — `KtObjectDeclaration` resolves to
   a `KtLightClass` with `isFinal=true` and no subtypes possible. Handle like
   any other final class: empty subtypes, no warning.
7. **Sealed class / sealed interface** — detect via Kotlin `SEALED_KEYWORD` on
   `KtClass` (simple-name probe) or Java `PsiClass.hasModifierProperty("sealed")`.
   Set `target.isSealed = true`. The subtype tree is exhaustive (sealed
   hierarchies are closed); surface a warning
   `"sealed type — direct children listed are exhaustive"` so the agent knows.
8. **Method on a generic interface** — `OverridingMethodsSearch` returns
   overrides with the actual erasure as written in the overrider's source.
   `signature` reflects the overrider's signature, not the interface's. We do
   NOT attempt generic substitution / unification (would require type-mapper
   plumbing and isn't worth the timeout budget).
9. **Method target on a `final` method** — empty overrides; not an error.
   `implementations: []`, `total: 0`.
10. **Project in dumb mode** — `computeWithAlternativeResolveEnabled` handles
    most cases; if the underlying search throws `IndexNotReadyException` (which
    DumbService usually swallows for stub-based searches), catch and append a
    warning rather than failing the whole call.
11. **Multi-resolve on a reference** — when the caret is on a reference that
    poly-resolves (overloaded method), follow the first non-null resolution,
    matching `PsiUsageSearcher.resolveTarget`'s behaviour. Append a warning
    listing how many alternatives were skipped, so the agent can re-query.
12. **Library-only target** — FQN resolves into a jar (e.g.
    `kotlin.collections.Map`). `target.fileUrl` is the `jar://…` URL.
    Subtype search in `scope="project"` only finds project subclasses, which
    is usually what the agent wants; `scope="all"` includes library hits.

## Files to create/modify

| Path | Op | What |
|------|----|------|
| `src/main/kotlin/com/github/xepozz/ide/introspector/core/PsiHierarchyResolver.kt` | Create | Headless logic for BOTH tools: FQN/position → `PsiClass`/`PsiMethod`, super/sub walk, override search, result shaping. Pure read-action callable, no `McpToolset` deps. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/PsiInfo.kt` | Edit | Append `HierarchyClassRef`, `HierarchyNode`, `TypeHierarchyResponse`, `ImplementationTarget`, `ImplementationInfo`, `GotoImplementationResponse`. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/args/PsiArgs.kt` | Edit | Append `TypeHierarchyArgs`, `GotoImplementationArgs`. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/tools/PsiToolset.kt` | Edit | Add `psi_type_hierarchy` and `psi_goto_implementation` `@McpTool` methods (thin wrappers around `PsiHierarchyResolver`). |
| `src/test/kotlin/com/github/xepozz/ide/introspector/core/PsiHierarchyResolverTest.kt` | Create | Unit tests on shape / argument validation / FQN-vs-position branching with stubbed `PsiClass`-shaped data (synthetic, no IntelliJ runtime). |
| `src/test/kotlin/com/github/xepozz/ide/introspector/core/platform/PsiHierarchyResolverPlatformTest.kt` | Create | Platform tests with Java + Kotlin fixture files. Extends `BasePlatformTestCase`. |

**No new XML wiring**: both tools live in the existing `psi.*` group on
`PsiToolset`, which is already registered in
`META-INF/mcp-integration.xml`. They share the same Java-PSI dependency story
as the rest of `psi.*` (the resolver uses `JavaPsiFacade` which requires
`com.intellij.modules.java`); since `PsiToolset` is registered unconditionally
today, fall back gracefully when the Java module is absent — `JavaPsiFacade`
will be missing and we should throw a clear `McpExpectedError` rather than
ClassNotFoundException. (Alternative: move these two methods to a separate
`PsiHierarchyToolset` under `java-introspect.xml`. Defer that split unless
runIde without Java module actually breaks the existing `psi.*` group.)

## Test plan

**Unit (`PsiHierarchyResolverTest.kt`)** — pure JVM, no IntelliJ runtime.
Covers the lang-agnostic logic that does not require platform PSI:

- `validates direction values` — accepts `up/down/both`, rejects others with
  `McpExpectedError`.
- `validates scope values` — accepts `file/project/all`, rejects others.
- `validates maxDepth / maxNodes / maxResults bounds`.
- `truncation flag set when maxNodes reached on supertype walk`.
- `truncation flag set when maxNodes reached on subtype walk`.
- `Object FQN target rejects subtype walk with warning`.
- `position args require offset OR (line+column)` — neither set → error.
- `target FQN takes precedence over position args when both supplied`.
- Signature normaliser: returns `"R name(P p)"` for a synthetic method shape.

**Platform (`PsiHierarchyResolverPlatformTest.kt`)** — extends
`BasePlatformTestCase`, loads fixture files via `testDataPath`. Mirrors the
existing `PsiUsageSearcherPlatformTest` setup.

Fixtures (under `src/test/testData/psi/hierarchy/`):

- `Animal.java` — abstract class.
- `Dog.java`, `Cat.java` — concrete extenders.
- `Puppy.java` — extends `Dog`.
- `Greeter.java` — interface with `greet()`.
- `EnglishGreeter.java`, `FrenchGreeter.java` — implementors.
- `SealedShape.kt` — `sealed class SealedShape` with `Circle` / `Square` subclasses.
- `KtAnimal.kt` — Kotlin abstract class mirroring `Animal.java`.

Test cases:

- `typeHierarchyByFqn_Animal_returnsExtenders` — FQN entry, direction=down,
  expects `Dog`, `Cat`, `Puppy` (depth ≥ 2).
- `typeHierarchyByPosition_caretOnDogClass_returnsSupertypeAnimal` — position
  entry, direction=up, expects `Animal` (and `Object` at the next level).
- `typeHierarchyBothDirections_Dog_returnsParentsAndChildren` — direction=both;
  supertypes contains `Animal` + `Object`, subtypes contains `Puppy`.
- `typeHierarchyMaxDepth1_truncatesDeepChildren` — depth=1 cuts `Puppy`; sets
  `childrenTruncated=true` on `Dog`.
- `typeHierarchyMaxNodes2_truncatesSubtypeWalk` — maxNodes=2 trips
  `truncated=true`.
- `typeHierarchySealedClass_flagsExhaustive` — `SealedShape` returns
  `isSealed=true` + warning about exhaustiveness.
- `typeHierarchyKotlinAbstract_returnsExtenders` — Kotlin `KtAnimal` works the
  same as `Animal.java` via the light-class adapter.
- `typeHierarchyObject_subtypeRejected` — FQN `java.lang.Object`, direction=both
  → supertypes null, subtypes null, warning set.
- `typeHierarchyFinalClass_returnsEmptySubtypes` — `Puppy` (no children) →
  subtypes is a single-node tree, no truncation.
- `gotoImplementationOnInterface_Greeter_returnsTwoImpls` —
  `EnglishGreeter`, `FrenchGreeter`. `target.kind=="class"`.
- `gotoImplementationOnAbstractMethod_returnsConcreteOverrides` — caret on
  `Animal.makeSound()` → `Dog.makeSound`, `Cat.makeSound`. `target.kind=="method"`.
- `gotoImplementationOnFinalMethod_returnsEmpty` — empty list, no error.
- `gotoImplementationOnConcreteClass_throwsExpectedError` — caret on `Dog`,
  not interface/abstract → expected to still return its subtypes (`Puppy`) for
  symmetry. (See open question 1 — adjust here once decided.)
- `gotoImplementationScopeAllAppendsWarning` — `scope="all"` always sets a
  warning even on small result sets.

## Estimated effort

| Step | Hours |
|------|-------|
| Args + response models in `PsiInfo.kt` / `PsiArgs.kt` | 0.5 |
| `PsiHierarchyResolver.kt` — supertype walk + subtype walk + caps | 3 |
| `PsiHierarchyResolver.kt` — goto-impl (class + method paths) + signature norm | 2 |
| `psi_type_hierarchy` + `psi_goto_implementation` toolset methods + `@McpDescription` | 1 |
| Unit tests | 1.5 |
| Java + Kotlin fixture files | 1 |
| Platform tests | 2 |
| Doc-gen verification + manual `runIde` smoke (Hierarchy parity check) | 1 |
| **Total** | **~1.5 days (12 h)** |

## Open questions / risks

1. **`psi.goto_implementation` on a class — follow both `extends` AND
   `implements`?** `DefinitionsScopedSearch` natively does both (it's the
   union of extending classes + implementing classes). **Proposed default:
   yes**, both are returned and there's no flag to split them. The
   `declaringClassFqn` lets the caller filter if needed.
2. **For `psi.type_hierarchy`, should we walk Kotlin sealed children
   automatically?** Sealed children are direct subclasses, so they fall out of
   `ClassInheritorsSearch` naturally — no special walk needed. We just flag
   `isSealed=true` and emit the warning so the agent knows the list is
   exhaustive (as opposed to "all subtypes we found in scope X"). **Proposed:
   no special walk — same `ClassInheritorsSearch` path.**
3. **Should we deduplicate implementations that are themselves overridden
   further down?** I.e. if `A.foo` is overridden by `B.foo` (abstract) which is
   overridden by `C.foo` (concrete), do we list `B` and `C` or just `C`?
   `OverridingMethodsSearch(checkDeep=true)` returns BOTH. The agent answering
   "what concretely implements `A.foo`?" usually wants just concrete ones (skip
   `B` because it's still abstract). **Proposed: filter to
   `!isAbstract` for methods by default**, with a follow-up flag
   `includeAbstract: Boolean = false` if a real caller needs both. The flag is
   not in the v1 signature — add only if a test fixture demonstrates need.
4. **Java module dependency** — `JavaPsiFacade` requires `com.intellij.modules.java`.
   `PsiToolset` is registered unconditionally today; if these methods raise
   `ClassNotFoundException` on an IDE without the Java module (PyCharm pure,
   GoLand?), we either (a) catch and degrade gracefully (return empty hierarchy
   + warning), or (b) move both methods to a new toolset registered in
   `java-introspect.xml`. **Proposed: (a) for now, revisit if CI shows
   problems**. The Kotlin light-class adapter requires the Java module anyway,
   so any IDE without it has no notion of "PsiClass hierarchy" at all.
5. **Position resolution duplication** — `PsiToolset` has `resolveFile` /
   `resolveOffset` as private members. Extracting them to a
   `core/PsiPositionResolver.kt` is the right long-term move. **Proposed for
   this plan**: copy into `PsiHierarchyResolver` for now; extract in a separate
   refactor PR when a third caller shows up.
6. **`OverridingMethodsSearch` ordering** — the platform does NOT promise a
   stable iteration order. We sort by `(fileUrl, range.startOffset)` before
   returning, so test assertions can rely on order.

## References

- Existing similar code:
  - `tools/PsiToolset.kt#psi_find_usages` — same positional-args + scope +
    truncation pattern.
  - `core/PsiUsageSearcher.kt` — `DefinitionsScopedSearch` usage to mirror;
    `resolveTarget` (follow-reference-or-named-ancestor) is the model for
    position resolution.
  - `core/PsiModifiers.kt` — reuse for `modifiers` / `isAbstract` /
    `isInterface` / `isFinal` extraction.
- IntelliJ source:
  - `ClassInheritorsSearch`:
    https://github.com/JetBrains/intellij-community/blob/master/platform/lang-impl/src/com/intellij/psi/search/searches/ClassInheritorsSearch.java
  - `OverridingMethodsSearch`:
    https://github.com/JetBrains/intellij-community/blob/master/java/java-indexing-api/src/com/intellij/psi/search/searches/OverridingMethodsSearch.java
  - `DefinitionsScopedSearch`:
    https://github.com/JetBrains/intellij-community/blob/master/platform/indexing-api/src/com/intellij/psi/search/searches/DefinitionsScopedSearch.java
  - `JavaPsiFacade`:
    https://github.com/JetBrains/intellij-community/blob/master/java/java-psi-api/src/com/intellij/psi/JavaPsiFacade.java
- JetBrains MCP equivalent: **none**. Neither type hierarchy nor goto
  implementation are exposed by JetBrains' built-in MCP server. Both are pure
  greenfield in the agent-tooling space.
