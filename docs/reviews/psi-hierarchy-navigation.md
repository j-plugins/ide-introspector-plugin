# Review: psi.type_hierarchy + psi.goto_implementation

Branch: `claude/project-features-analysis-odEwP` @ `3225444` (review HEAD)
Reviewer scope: `core/PsiHierarchyResolver.kt`, the appended types in
`model/PsiInfo.kt`, the two args classes in `model/args/PsiArgs.kt`, the two
new `@McpTool` methods on `tools/PsiToolset.kt`, plus the tests the plan
called for.

## Verdict
Needs changes before merge. The shape is right — `ClassInheritorsSearch` /
`OverridingMethodsSearch` / `DefinitionsScopedSearch` chosen per the plan,
`readActionBlocking` + `computeWithAlternativeResolveEnabled` mirror
`psi_find_usages`, `Object` is special-cased, sealed + final flags are
surfaced. But `scope="file"` is silently demoted to project-scope in BOTH
tools (validation accepts it; the resolver discards it), the entire test
suite the plan demanded is missing (zero unit + zero platform tests), and
the new tools duplicate `containingFile.viewProvider.document` /
`FileDocumentManager` boilerplate instead of using `PositionResolver` /
`PsiStructureWalker.textRangeInfoOf` helpers that already exist. Fix the
scope handling + add the tests and this is mergeable.

## Summary
- Files added/edited match the plan list except for the two test files
  (`PsiHierarchyResolverTest.kt`, `PsiHierarchyResolverPlatformTest.kt`)
  which are not present in `src/test/kotlin/.../core/`.
- `psi_type_hierarchy` and `psi_goto_implementation` are both thin wrappers
  on `PsiToolset` per the plan; no new META-INF wiring (correct — both
  ship under the existing `mcp-integration.xml` registration of
  `PsiToolset`).
- Doc generation worked — `docs/MCP_TOOLS.md:62,65,1497,1650` lists both
  new tools.
- `java.lang.Object` is special-cased in both directions
  (`PsiHierarchyResolver.kt:93,221,242`), warnings appended, subtype walk
  refused — matches the plan exactly.
- Sealed detection covers Java-17 `hasModifierProperty("sealed")` AND a
  Kotlin simple-name probe (`PsiHierarchyResolver.kt:475-492`), with
  `isSealed` set on `HierarchyClassRef` and a warning appended on the
  target.
- Hard 10 s cap inherited from `readActionBlocking` (Hard rules ok). No new
  `withTimeoutOrNull` / latch added.
- `JavaPsiFacade` CNFE is caught and degraded to `McpExpectedError` per the
  plan's "split to `java-introspect.xml` only if CI breaks" decision —
  `PsiHierarchyResolver.kt:301-308`.
- `@McpDescription` strings on both tools follow the 5-section convention
  verbatim from the plan; per-param descriptions present.

## Findings (numbered, severity-tagged)

1. **[HIGH] `PsiHierarchyResolver.kt:494-509` — `scope="file"` is silently
   coerced to `GlobalSearchScope.projectScope`; the `searchScopeForFile`
   helper that would respect it is never called.**
   ```kotlin
   private fun globalScope(project: Project, kind: String): GlobalSearchScope = when (kind) {
       "file" -> GlobalSearchScope.projectScope(project)   // <-- silently widens
       "all"  -> GlobalSearchScope.allScope(project)
       else   -> GlobalSearchScope.projectScope(project)
   }
   ```
   Both `gotoImplementation` (`:210`) and `typeHierarchy` (`:74`) call
   `globalScope(project, scopeKind)`. The `searchScopeForFile(file, scope,
   project)` overload directly below honors `"file"` via
   `GlobalSearchScope.fileScope(file)` but is dead code — no caller invokes
   it. Meanwhile both toolset methods validate
   `scope ∈ {file, project, all}` (`PsiToolset.kt:577,736`), so an agent
   asking for `scope="file"` gets `scope="file"` echoed in the response
   while the search ran across the entire project. This is a silent contract
   violation, not just an inefficient default. Fix: thread the `PsiFile`
   into `gotoImplementation` and (when `target` is null) `typeHierarchy`,
   then call `searchScopeForFile` instead of `globalScope`. The
   `ClassInheritorsSearch` / `OverridingMethodsSearch` / `DefinitionsScopedSearch`
   queries all accept `SearchScope` (the supertype of both Global- and
   LocalSearchScope), so no cast hop is needed.

2. **[HIGH] Zero tests for `PsiHierarchyResolver`** — the plan asked for
   `src/test/kotlin/.../core/PsiHierarchyResolverTest.kt` (unit) AND
   `src/test/kotlin/.../core/platform/PsiHierarchyResolverPlatformTest.kt`
   (platform, Java + Kotlin fixtures under
   `src/test/testData/psi/hierarchy/`). Neither file exists; `find
   src/test -name "*Hierarchy*"` returns nothing. None of the listed
   coverage points (`byFqn`, `byPosition`, `bothDirections`, `maxDepth=1
   childrenTruncated`, `maxNodes=2 truncated`, `Object` rejection, sealed
   exhaustive flag, Kotlin via `KtLightClass`, `final` class empty
   subtypes, interface impl, abstract method override, final method empty,
   `scope="all"` warning) is exercised. Finding 1 would have failed at
   the first `scope="file"` test. The PR's commit message
   (`2f35ef3 feat: salvage psi-hierarchy-navigation … (untested)`) is
   honest about this, but "untested" plus a positional contract is the
   exact recipe Finding 1 grew out of.

3. **[MEDIUM] `PsiHierarchyResolver.kt:225-235` — `OverridingMethodsSearch
   (checkDeep=true)` returns intermediate abstract overrides in the same
   flat list as concrete ones; nothing filters duplicates or sorts
   distinguishable levels.** Plan open question 3 explicitly proposed
   "filter `!isAbstract` for methods by default; add `includeAbstract`
   flag only if a caller demonstrates need (not in v1)." Implementation
   skipped the filter — all `OverridingMethodsSearch` hits land in
   `out`, tagged `isAbstract`/`isOverride` but otherwise indistinguishable
   from concrete leaves. An interface method with a four-level chain of
   abstract intermediates and one concrete leaf returns five
   `ImplementationInfo` entries; the agent has to filter `!isAbstract`
   itself. Either (a) implement the proposed default filter
   (`if (info.isAbstract) return@Processor true`) or (b) update the
   `@McpDescription` to call out that intermediate abstracts are
   returned and that `isAbstract` should be checked. Silently disagreeing
   with the plan is the bug.

4. **[MEDIUM] `PsiHierarchyResolver.kt:353-369,371-409,411-447` — three
   nearly-identical `describe*` helpers each redo the
   `containingFile.virtualFile` / `viewProvider.document` /
   `FileDocumentManager.getInstance().getDocument(vf)` /
   `nameIdentifier?.textRange ?: textRange ?: TextRange(0,0)` /
   `PsiStructureWalker.textRangeInfoOf(...)` dance.** ~50 lines duplicated
   three ways. Extract a `describeDeclSite(element: PsiNamedElement):
   Triple<String?, TextRangeInfo, Document?>` (or similar) helper in
   `PsiStructureWalker` or a new shared file. The `target` resolver
   parameter is independent of class vs. method shape, so the description
   helper can be polymorphic. This is the same kind of churn the
   `PsiModifiers` extraction already cleaned up for modifier vocabularies.

5. **[MEDIUM] `PsiHierarchyResolver.kt:475-492` — Kotlin sealed detection
   greps the source text for `\bsealed\b` in the first 80 chars.** Works
   for trivial declarations but mis-fires on annotations, KDoc, or
   modifier-list comments containing the word "sealed":
   `@Deprecated("sealed in 2024") class Foo` ⇒ false positive. Plan
   referenced `PsiUsageSearcher.isLocalVariableLike`'s simple-name probe,
   but that probe matches class names — it doesn't grep source. Replace
   with a reflective `hasModifier(KtTokens.SEALED_KEYWORD)` invocation:
   ```kotlin
   val modifierList = nav::class.java.getMethod("getModifierList").invoke(nav)
   val ktTokensCls = Class.forName("org.jetbrains.kotlin.lexer.KtTokens", true, nav::class.java.classLoader)
   val sealedKw = ktTokensCls.getField("SEALED_KEYWORD").get(null)
   modifierList?.let { it::class.java.getMethod("hasModifier", sealedKw::class.java).invoke(it, sealedKw) as Boolean } ?: false
   ```
   ...or just probe `nav.firstChild?.text == "sealed"` (the modifier list
   is the first child of `KtClass`). Either avoids the regex on comments.

6. **[MEDIUM] `PsiHierarchyResolver.kt:309` — `JavaPsiFacade.findClass(fqn,
   GlobalSearchScope.allScope(project))` ignores the user's requested
   `scope`.** A caller asking `scope="project"` for FQN
   `com.acme.Foo` expects the resolver to refuse a class that only exists
   in a library jar — instead it resolves via `allScope`, then runs the
   subtype walk with `projectScope`. The user gets a target node pointing
   into a jar plus an empty `subtypes` tree and no warning. Two fixes
   either work: (a) lookup via the requested `scope`, or (b) keep
   `allScope` for resolution but emit a warning when
   `targetFile is in library and scope != "all"`. The second is friendlier
   to the most common ask ("what extends Editor in MY project?"); pair
   with a `targetInProjectSources: Boolean` field on `HierarchyClassRef`
   if the agent needs to know.

7. **[MEDIUM] `PsiHierarchyResolver.kt:172-176,236-239,266-269` —
   `catch (_: Throwable)` swallows every non-PCE error silently.** Mirrors
   `PsiUsageSearcher.kt:111-115`, which at least logs the comment
   "Index hiccup — keep what we have". The new resolver has the same
   comment but the swallowed error never reaches the response — the
   agent sees an empty (or short) list and assumes "no subtypes" rather
   than "search blew up". Append a warning when the catch fires:
   ```kotlin
   } catch (t: Throwable) {
       warnings += "Subtype search aborted: ${t.javaClass.simpleName}"
   }
   ```
   `warnings` already threads through to `TypeHierarchyResponse` /
   `GotoImplementationResponse` — wire it through to the `walkSubtypes`
   / search blocks (currently it's only mutated at the top level).

8. **[LOW] `PsiHierarchyResolver.kt:528-544` — `NodeBudget` "consume() at
   the start, then check before recursion" is off by one in the
   childrenTruncated flag.** When `maxNodes=2` and the tree is
   `Root → A → B`, `consume()` lands on `Root` (root counts per
   `:84`), then `A` (consumed at `:130`), then `B` exhausts. `walk` for
   `A` enters with `budget.exhausted=false`, finds parent `B`, consumes
   (`remaining=0, truncated=true`), then recurses into `build(B,...)`
   which produces `HierarchyNode(B, [], false)` — the `B` node looks
   complete even though there might be more parents above it. The
   `childrenTruncated` on `A` says "true if `cutByBudget` after the
   loop" but the loop ran to completion on B. Tighten by checking
   `budget.exhausted` *before* calling `build(parent, depth+1)`, or by
   passing the budget state forward into B's recursion so it propagates
   its own cut. The cap still bounds total work — the bug is just the
   `childrenTruncated` flag's accuracy on the boundary node.

9. **[LOW] `PsiHierarchyResolver.kt:148` — `if (cls === root) rootRef
   else describeClass(cls)` optimisation only fires for the literal
   root.** The walker recurses through hundreds of classes per direct
   inheritor; `describeClass` runs the full reflection +
   `nameIdentifier.textRange` + `FileDocumentManager` lookup per node
   every time. Acceptable at `maxNodes=200`, but at the upper bound of
   `maxNodes=5000` (validated as the cap on `PsiToolset.kt:580`) this
   walks 5000 nameIdentifier / `FileDocumentManager` round trips inside
   a single read action. Optionally cache `describeClass` keyed by
   `qualifiedName` for the duration of one resolver call.

10. **[LOW] `PsiHierarchyResolver.kt:113-114` — `seen` is keyed by
    `qualifiedName ?: name ?: "(anonymous)"`.** Two anonymous super-
    interfaces in the same walk both hash to `"(anonymous)"` and the
    second one is silently dropped. Anonymous classes can't have an
    explicit `extends`, so practically this only bites if `cls.supers`
    surfaces a synthetic anonymous wrapper (rare). Use an `IdentityHashMap`
    keyed by the `PsiClass` instance to make it watertight.

11. **[LOW] `PsiToolset.kt:511-571,683-720` — both `@McpDescription`
    strings are excellent for content but the "Returns" sections describe
    `HierarchyNode?` / `ImplementationInfo[]` shapes without flagging
    the response-level `warnings[]` enum.** Agents who programmatically
    parse `warnings[]` benefit from "expected warning strings include:
    `scope="all" includes library sources...`, `Target is sealed —
    direct subtypes are exhaustive.`, `Subtype walk for java.lang.Object
    is rejected ...`". Same pattern as `psi.find_usages` should but
    doesn't either — track separately.

12. **[LOW] `PsiHierarchyResolver.kt:201-289` — `gotoImplementation`
    duplicates the `psi.find_usages` setup (target resolve →
    DefinitionsScopedSearch + Processor + cap)**. `PsiUsageSearcher`
    already calls `DefinitionsScopedSearch` (`PsiUsageSearcher.kt:121`);
    refactor opportunity: extract a `DefinitionsHarvester` that both
    `psi.find_usages` (when `includeImplementations=true`) and
    `psi.goto_implementation` consume. Out of scope for this PR but
    flag for the next cleanup pass.

13. **[NIT] `PsiToolset.kt:587-596` — the `if (target.isNullOrBlank())`
    branch builds `resolvedPsiFile`/`resolvedOffset` from positional args
    via `resolveFile` + `resolveOffset`.** Works, but if the agent passes
    both `target="…"` and a `fileUrl`, the FQN wins (per the
    `@McpDescription`) and `fileUrl` is silently ignored. Match
    `psi_get_references`'s pattern — warn or reject when mutually-
    exclusive params are co-supplied — or document explicitly in the
    `@McpDescription` (currently says "Takes precedence" which is close
    but the agent might not realise `fileUrl` is fully ignored).

14. **[NIT] `PsiToolset.kt:557` — `@McpDescription("VFS URL of the file
    holding the target. null → active editor tab. Only used when `target`
    is null.")`** — when `target` is non-null AND there's no active tab,
    `resolveFile` would throw, but we shortcut around it. Good. Same
    text elsewhere claims "active editor tab" without the conditional
    — fine here, just confirming.

## Plan-vs-implementation gaps

| Plan asked for | Implementation | Action |
|---|---|---|
| `scope="file"` supported via `LocalSearchScope`/`fileScope` | Silently demoted to `projectScope` for both tools | Finding 1 — HIGH |
| Unit tests (`PsiHierarchyResolverTest.kt`) | Missing | Finding 2 — HIGH |
| Platform tests (`PsiHierarchyResolverPlatformTest.kt`) + Java/Kotlin fixtures | Missing | Finding 2 — HIGH |
| `!isAbstract` filter for method overrides by default | Not applied | Finding 3 |
| Sealed detection via `KtClass.modifierList.hasModifier(SEALED_KEYWORD)` | Regex grep on first 80 chars | Finding 5 |
| `scope` honored on FQN lookup | `allScope` hardcoded | Finding 6 |
| Position resolution reuses `PositionResolver` | Yes (`PsiToolset.kt:812`) | OK |
| `readActionBlocking` + `computeWithAlternativeResolveEnabled` | Yes (`PsiToolset.kt:586,597,742,745`) | OK |
| `Object` subtype walk rejected with warning | Yes (`PsiHierarchyResolver.kt:93-95`) | OK |
| `final` class shortcut → empty subtypes | Yes (`PsiHierarchyResolver.kt:156-158`) | OK, but only for non-interface |
| 5-section `@McpDescription` | Yes, verbatim from the plan | OK |
| Sort impls by `(fileUrl, range.startOffset)` | Yes (`PsiHierarchyResolver.kt:278-280`) | OK |
| `scope="all"` warning | Yes (both tools) | OK |
| `McpExpectedError` on unresolved target | Yes (`PsiToolset.kt:609-611,754-756`) | OK |

## Research notes (URLs)

- `JavaClassInheritorsSearcher` source confirms `ClassInheritorsSearch` is a
  cached/per-PsiClass index lookup that respects the current read action
  and yields anonymous classes too (we filter via
  `sub.qualifiedName == null` — correct):
  https://github.com/JetBrains/intellij-community/blob/master/java/java-indexing-impl/src/com/intellij/psi/impl/search/JavaClassInheritorsSearcher.java
- `DumbService` docs confirm `computeWithAlternativeResolveEnabled` is the
  intended way to suppress `IndexNotReadyException` during PSI walks; our
  toolset uses it (`PsiToolset.kt:597,745`) — same pattern as
  `psi_find_usages`:
  https://dploeger.github.io/intellij-api-doc/com/intellij/openapi/project/DumbService.html
- JetBrains community Q&A on `ClassInheritorsSearch` direct-vs-deep
  semantics — confirms `checkDeep=false` returns direct extenders only,
  which is what our manual depth walk needs:
  https://intellij-support.jetbrains.com/hc/en-us/community/posts/207009975-Finding-direct-subclasses
- IntelliJ "Source code hierarchy" help page documents the tool window we're
  mirroring — confirms project scope is the UI default:
  https://www.jetbrains.com/help/idea/viewing-structure-and-hierarchy-of-the-source-code.html
- `OverridingMethodsSearch` behaviour at `checkDeep=true` (intermediate
  abstracts) — no direct doc; verified empirically in upstream tests:
  https://github.com/JetBrains/intellij-community/blob/master/java/java-indexing-impl/src/com/intellij/psi/impl/search/MethodSuperSearcher.java

## Test coverage assessment

Unit tests for `PsiHierarchyResolver`: **0**. Platform tests: **0**.
Fixtures (`src/test/testData/psi/hierarchy/Animal.java`, `SealedShape.kt`,
…): **none present**. The plan's test plan (lines 248-267 of
`docs/plans/psi-hierarchy-navigation.md`) is the most detailed in the
docs/plans tree and was not executed at all.

Minimum recommended before merge:
- Unit test: arg validation — `direction`, `scope`, `maxDepth`/`maxNodes`
  bounds, FQN-or-position contract, signature normaliser format.
- Platform test: byFqn on `Animal`, byPosition on `Dog`, both-directions,
  `maxDepth=1` truncation, `maxNodes=2` truncation, sealed exhaustive,
  Kotlin via KtLightClass, `Object` subtype rejection, final class empty
  subtypes, `scope="file"` (would catch Finding 1), `scope="all"`
  warning, interface impl via `goto_implementation`, abstract method
  override, final method empty.

Mirror `PsiUsageSearcherPlatformTest.kt` for shape — same `BasePlatformTestCase`
fixture-loading idiom that `psi_find_usages` already uses.

## Cross-cutting suggestions

- `PsiHierarchyResolver` is `object` — fine for stateless utilities, but
  `NodeBudget` is a nested non-`data` class shared between supertype +
  subtype walks. The current sharing means a supertype walk can exhaust
  the budget before the subtype walk gets a single node when
  `direction="both"`. That's the documented behaviour ("hard cap on
  total"), but the response's `subtypes` will then be `null` rather than
  a `HierarchyNode` with `childrenTruncated=true` on the root — confusing.
  Consider splitting the budget: half for supertypes, half for subtypes,
  with leftover spillover.
- The `searchScopeForFile` dead-code helper (Finding 1) suggests the
  author drafted the file-scope path and forgot to wire it. Either delete
  it or make it the real implementation.
- `PsiHierarchyResolver.resolveClassTarget` is `fun` (not `private`) —
  intentional? It's tagged on the only callsite that lives in the same
  file. Same for `searchScopeForFile`, `describeClass`, `describeImplTarget`,
  `methodSignature`. Visibility cleanup pass when the test files land
  would reduce the surface.
- `walkSupertypes`'s `seen` collection prevents cycles but
  `walkSubtypes` has no `seen` — `ClassInheritorsSearch` doesn't return
  cycles (you can't extend yourself), so it's safe; worth a one-line
  comment explaining the asymmetry.
- Once Finding 1 is fixed, the per-tool `searchScopeForFile` call needs
  the `PsiFile` to reach the bottom of `gotoImplementation`. The
  `target` parameter is `PsiElement` (could be method or class); use
  `target.containingFile` rather than threading another arg.

## References

- `src/main/kotlin/com/github/xepozz/ide/introspector/core/PsiHierarchyResolver.kt:74,93,156,221,242,309,475-492,494-509,528-544`
  — Findings 1, 3, 5, 6, 7, 8.
- `src/main/kotlin/com/github/xepozz/ide/introspector/tools/PsiToolset.kt:511-614,683-759,812`
  — toolset wrappers, position resolver reuse (good), warning enumeration (Finding 11).
- `src/main/kotlin/com/github/xepozz/ide/introspector/model/PsiInfo.kt:309-409`
  — six new `@Serializable` types, all default-correctly-constructed.
- `src/main/kotlin/com/github/xepozz/ide/introspector/model/args/PsiArgs.kt:42-63`
  — `TypeHierarchyArgs`, `GotoImplementationArgs`.
- `src/main/kotlin/com/github/xepozz/ide/introspector/util/PositionResolver.kt`
  — reused via `PsiToolset.resolveOffset` (correct — no duplication).
- `src/main/kotlin/com/github/xepozz/ide/introspector/core/PsiUsageSearcher.kt:101-135,180-188`
  — pattern reference; the new resolver follows it but doesn't extract
  the shared `DefinitionsScopedSearch` harvest (Finding 12).
- `src/main/kotlin/com/github/xepozz/ide/introspector/util/EdtHelpers.kt:52-68`
  — `readActionBlocking` enforces the 10 s cap (Hard rules).
- `docs/plans/psi-hierarchy-navigation.md:174,196,202-213,239-241,248-267`
  — contract this PR claims to implement (validation, position resolver,
  threading + timeout, files-to-create test files, test plan).
- `docs/MCP_TOOLS.md:62,65,1497,1650` — auto-generated entries confirm
  doc-gen ran.
