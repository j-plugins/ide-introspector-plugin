# Review: code.list_classes_in_module + code.list_classes_in_package

Branch: `claude/project-features-analysis-odEwP` @ `4a94097` (review HEAD).
Reviewer scope: `core/ClassCatalog.kt`, the appended types in
`model/ClassSourceInfo.kt`, the new `model/args/CodeArgs.kt`, the two new
`@McpTool` methods on `tools/CodeSourceToolset.kt`, the conditional load
in `META-INF/java-introspect.xml`, plus the tests the plan called for.

## Verdict
Needs changes before merge. Shape is right — `ModuleFileIndex.iterateContent`
+ extension pre-filter for the module variant, `JavaPsiFacade.findPackage` +
BFS for the package variant, `readActionBlocking`, 10 s deadline via injectable
`Clock` seam, `KtLightClassForFacade` detected reflectively, `clampLimit`
validated before the read action, `ModuleLookup.NOT_FOUND` → `McpExpectedError`.
But (1) zero tests despite a detailed test plan with a clock seam built
specifically for them, (2) the package variant ignores `includeLibraries=false`
when recursing — `pkg.subPackages` (no-scope) BFSes into JDK packages, then
each scoped `getClasses` returns empty, burning the 10 s deadline on
`java.**`, (3) `iterateContent` walks ALL content (resources, excluded-but-
not-excluded extras) without an `isInSourceContent` filter so a stray
`.java`/`.kt` under a resource folder leaks into results. Fix 2+3 and add
even minimal tests and this is mergeable.

## Summary
- Files added/edited match the plan list except the two test files
  (`ClassCatalogFilterTest.kt`, `ClassCatalogPlatformTest.kt`) which are
  not present in `src/test/kotlin/.../core/`. The `Clock` seam exists
  (`ClassCatalog.kt:299-304`) and `listInModuleForTest` is wired
  (`ClassCatalog.kt:306-329`) — clearly drafted for tests that were never
  written.
- Doc generation worked — `docs/MCP_TOOLS.md:32-33,702,745` lists both tools.
- No new META-INF wiring (correct — both ship via the existing
  `java-introspect.xml` registration of `CodeSourceToolset`).
- `META-INF/java-introspect.xml` correctly gates on `com.intellij.modules.java`
  (file header comment is explicit) — verified.
- `KtLightClassForFacade` reflective probe (`ClassCatalog.kt:46-48`) keeps
  the tool working in pure-Java IDEs (Kotlin plugin absent).
- `clampLimit` is called OUTSIDE the read action wrappers
  (`CodeSourceToolset.kt:417,480`) so `IllegalArgumentException` surfaces
  cleanly to the MCP caller — correct.
- `ModuleLookup.NOT_FOUND` distinction (`ClassCatalog.kt:295`) lets the
  toolset distinguish a missing module from an empty result — matches
  plan §Edge cases #1.
- Hard 10 s cap respected — `WALL_CLOCK_MS=10_000L` (`ClassCatalog.kt:39`),
  `readActionBlocking` adds its own 10 s outer cap.
- `@McpDescription` strings on both tools follow the 5-section convention
  verbatim from the plan; per-param descriptions present.
- `isInGeneratedSources(VirtualFile)` — **verified** against
  `ideaIU-2025.2.6.2/lib/app-client.jar` decompile: the method exists on
  `ProjectFileIndex` in 252, is NOT deprecated, NOT renamed. The fixer
  note's worry about `isInGeneratedSourceContent` being moved/renamed in
  252 is a false alarm — the code calls the correct, current name
  (`ClassCatalog.kt:95`). `isInTestSourceContent` inherits from
  `FileIndex` — also verified present.

## Findings (numbered, severity-tagged)

1. **[HIGH] `ClassCatalog.kt:193-200` — `pkg.subPackages` ignores
   `includeLibraries=false`; recursive package walks BFS into the entire
   JDK/library subpackage tree.** `PsiPackage.getSubPackages()` (Kotlin
   `subPackages` property) is the no-arg overload — confirmed by
   PsiPackage class decompile and JetBrains' own docs: it returns
   subpackages "under all source roots" but PsiElementFinders fold in
   library packages too. The scoped overload (`getSubPackages(scope)`)
   exists and takes the same `GlobalSearchScope` we already build at
   `:156-159`. The current recursive `java.util` call without
   `includeLibraries` will BFS into `java.util.concurrent`,
   `java.util.concurrent.atomic`, `java.util.function`, … hundreds of
   JDK subpackages, calling `getClasses(projectScope)` on each (which
   correctly returns empty), burning wall-clock time on package
   resolution alone. Fix: call `pkg.getSubPackages(scope)` instead of
   `pkg.subPackages` so the project-scope filter prunes the tree
   before BFS. One-line change.

2. **[HIGH] `ClassCatalog.kt:92-95` — `iterateContent` visits all
   content roots including resource roots and non-source content; the
   filter chain has no `isInSourceContent` check.** Per `FileIndex`
   javadoc, `iterateContent` "processes all files and directories under
   content roots skipping excluded and ignored." Resource folders are
   NOT excluded — a `.java` or `.kt` file under `src/main/resources`
   (e.g. test data, KSP fixture inputs, code-snippet docs) or under a
   build dir that's a content root but not source-marked (e.g. some
   bazel/buck layouts) gets picked up. The extension pre-filter at
   `:93` is not enough. Add:
   ```kotlin
   if (!fileIndex.isInSourceContent(vf)) return@ContentIterator true
   ```
   right before the test/generated checks. This also makes the
   `packageName ?: ""` extraction at `:99` correct — files outside
   source roots have no package association and pollute results with
   `pkg=""`.

3. **[HIGH] Zero tests for `ClassCatalog`** — the plan asked for
   `src/test/kotlin/.../core/ClassCatalogFilterTest.kt` (unit) AND
   `src/test/kotlin/.../core/platform/ClassCatalogPlatformTest.kt`
   (platform, multi-module Java+Kotlin fixture). Neither file exists;
   `find src/test -name "*ClassCatalog*"` returns nothing. The
   `internal listInModuleForTest` (`ClassCatalog.kt:306-329`) and the
   `Clock` seam (`:299-304`) are visibly drafted for these tests, then
   left unused. None of the plan's listed coverage points are
   exercised — kind filter accepts/rejects, `packagePrefix` null/exact/
   non-matching, `limit` invalid throws, oversize → `truncated=true`,
   `kotlinFileFacade` excluded-when-absent, `includeTests` toggle,
   `includeGenerated=false` hides KSP output, `moduleName="nope"` →
   `McpExpectedError`, `recursive` vs non-recursive, `packageFqn=""`,
   tiny injected deadline → `timedOut=true` with partial results. The
   `Clock` interface is literally there to make the last test
   non-flaky — and it's never called. Finding 1 would have failed at
   the first `recursive=true` + `packageFqn="java.util"` test.

4. **[MEDIUM] `ClassCatalog.kt:108,117` — `truncated` is computed off
   `total > collected.size` but `total` only counts entries that
   passed `toEntry()` (kind+FQN filter).** When `limit=10` and the
   raw match-set is 12 but two are anonymous/no-FQN (`toEntry`
   returns null without bumping `total`), `total=10`, `collected=10`,
   `truncated=false` — even though iteration could have surfaced more
   classes had they had FQNs. Conversely, when `kinds=["class"]` and
   the directory has 20 interfaces + 5 classes, `total=5`,
   `collected=5`, `truncated=false` — correct. The bug is only in the
   first scenario but it's confusing: `total` is "kept entries before
   the limit hit" not "matched-and-eligible entries". Either rename
   `total` to `matched` in the response doc, or count rejected entries
   into a separate `skipped` field. The model comment claims
   "unbounded count BEFORE limit was applied" — currently false when
   filters drop entries after they're matched.

5. **[MEDIUM] `ClassCatalog.kt:101-111` — `psiFile.classes` inside a
   loop can return synthetic light classes for Kotlin file facades
   AND for top-level Kotlin classes from the same file.** For a
   `Foo.kt` containing `class Foo` + `fun bar()`, `psiFile.classes`
   yields TWO entries: `KtLightClassForSourceDeclaration` (`Foo`) and
   `KtLightClassForFacade` (`FooKt`). Plan §Edge cases #4 acknowledges
   this. The current code includes both, which is correct, but the
   `byteLength` reported for both is the same `containing.textLength`
   — so an agent budgeting `code.get_source` per row will double-count
   the file. Add a comment noting `byteLength` is per-file-not-per-
   class so the agent knows; or dedupe on `containing.virtualFile.url`
   before computing `total`.

6. **[MEDIUM] `ClassCatalog.kt:135-154` — package variant has no
   equivalent of `includeTests`/`includeGenerated`.** Plan §Edge
   cases #7 documents this as "explicit asymmetry" — fine, but the
   asymmetry isn't surfaced in the `@McpDescription`. An agent asking
   `code.list_classes_in_package packageFqn="com.acme.billing"` gets
   test-scope classes mixed with production ones with no warning.
   Either (a) thread `includeTests` through the package variant by
   filtering on `GlobalSearchScope.notScope(GlobalSearchScopes.
   projectTestScope(project))`, or (b) call out in the
   `@McpDescription` "Includes test sources — use
   `code.list_classes_in_module` if you need a test/production split."
   Currently the agent has to discover the asymmetry by reading the
   results.

7. **[MEDIUM] `ClassCatalog.kt:97-112` — `runCatching {...}` outer
   block swallows every PSI exception silently.** Mirrors
   `PluginInventory`'s pattern but with no logging and no warning
   thread-through. If `PsiManager.findFile(vf)` blows up because of an
   index-not-ready hiccup, the file is silently skipped and the agent
   sees a smaller `total`. Same pattern in the BFS at `:179,194`. At
   minimum, add a `logger<ClassCatalog>().debug(...)` call so a future
   debugger can correlate "missing entries" with a `runCatching`
   swallow. Better: thread a `warnings: MutableList<String>` through
   and surface "Skipped N files due to index errors" on the response —
   the response model already has a `note` field that's an obvious
   home for this.

8. **[LOW] `ClassCatalog.kt:88-91` — deadline check fires `false`
   return value from `ContentIterator`, which per `FileIndex`
   contract stops iteration.** Correct. But the `false` is silent —
   `timedOut=true` is set before returning, so the response IS
   marked, but the wall-clock check happens BEFORE the
   `vf.isDirectory` short-circuit. A pathological tree (10k empty
   directories, 100 source files) burns one `clock.now()` call per
   directory descent. Not a correctness bug; in practice 10k
   `System.currentTimeMillis()` calls is microseconds.

9. **[LOW] `ClassCatalog.kt:278-288` — `packageOf()` derives package
   from FQN string-slicing instead of `cls.qualifiedName.substringBeforeLast('.')`
   or `(cls.containingFile as PsiClassOwner).packageName`.** The
   fallback for "FQN doesn't end with .simpleName" is over-defensive
   for top-level-classes-only callers; the inner-class smell comment
   admits the path never fires today. Two micro-issues: (a) the
   string slice does one substring allocation per class — call sites
   are bounded by `limit≤5000` so it's fine; (b) the `idx <= 0` guard
   on `:287` should be `idx < 0` (FQN like `.X` would map to `""` not
   throw, but `idx == 0` means FQN starts with `.simpleName` which
   isn't valid Java/Kotlin). Cosmetic.

10. **[LOW] `model/ClassSourceInfo.kt:123` + `model/args/CodeArgs.kt:25`
    — plan calls the field `package`, code uses `pkg`.** Plan §107-119:
    > `ClassEntry(fqn, simpleName, package, kind, …) — package is "" for default`.
    Implementation renamed to `pkg` (Kotlin reserved word, so the rename
    is necessary). MCP JSON output will be `"pkg": "..."` — different
    from the documented `"package": "..."`. Two options: (a) use
    `@SerialName("package") val pkg: String` so JSON matches the plan;
    (b) update the plan + `@McpDescription` to say `pkg`. The current
    state (no `@SerialName`, plan unchanged) is a silent contract drift.
    The `code.list_classes_in_module` description at
    `CodeSourceToolset.kt:389` was updated to `pkg`, so option (b) was
    partially applied — finish by updating the plan or apply
    `@SerialName` for consistency with the original spec.

11. **[LOW] `ClassCatalog.kt:127-145` — package variant's
    `JavaPsiFacade.findPackage` returns null for unresolved packages
    AND silently returns `total=0`.** Plan §Edge cases #2 says this
    is intentional ("not an error — matches `arch.list_services`
    convention"). Fine, but the `note` field could carry "Package not
    found: <fqn>" so the agent can distinguish "empty package" from
    "typo'd FQN" without making a second `code.find_class` call.
    Currently both return `total=0, note=null` (or `note=dumb`).

12. **[LOW] `CodeSourceToolset.kt:417,480` — `ClassCatalog.clampLimit(limit)`
    is called purely for the side effect of throwing.** Awkward — the
    return value is discarded. Either inline `require(limit in 1..MAX_LIMIT)`
    at the toolset boundary, or rename to `validateLimit()`. The
    current `clampLimit` name suggests it clamps (silently caps); it
    actually validates-or-throws.

13. **[NIT] `ClassCatalog.kt:147-154` — `note = noteIfDumb(project)`
    fires when the project is indexing.** Good. But the note string
    "Project is indexing; results may be incomplete" is duplicated at
    `:124` and `:210` and `:153`. Pull into a `const val
    DUMB_NOTE` to avoid drift.

14. **[NIT] `model/args/CodeArgs.kt` — args classes are declared but
    never used.** `CodeSourceToolset` takes individual parameters
    (per the MCP framework's reflection bridge convention); the args
    classes mirror those parameters but aren't deserialized from
    anywhere. Either delete the args file (parameters are already
    self-documenting via `@McpDescription` on `suspend fun`) or call
    out in the file header that this is documentation-shape only.

## Plan-vs-implementation gaps

| Plan asked for | Implementation | Action |
|---|---|---|
| Unit tests (`ClassCatalogFilterTest.kt`) | Missing | Finding 3 |
| Platform tests (`ClassCatalogPlatformTest.kt`) | Missing | Finding 3 |
| `includeLibraries=false` honoured on recursive package walk | `subPackages` no-scope; widens to JDK | Finding 1 |
| `iterateContent` + source-content guard | Extension-only filter; no `isInSourceContent` | Finding 2 |
| Field name `package` in `ClassEntry` | Renamed to `pkg` without `@SerialName` | Finding 10 |
| Module-not-found → `McpExpectedError` | Yes (`CodeSourceToolset.kt:428-433`) | OK |
| `Clock` seam for deadline testing | Yes (`ClassCatalog.kt:299-304`) | OK — but unused (Finding 3) |
| `KtLightClassForFacade` behind `Class.forName` guard | Yes (`ClassCatalog.kt:46-48`) | OK |
| `readActionBlocking` wrapping | Yes (`CodeSourceToolset.kt:418,481`) | OK |
| Per-file `runCatching` | Yes (`ClassCatalog.kt:97,179,194`) | OK, but silent (Finding 7) |
| `includeLibraries=false` default | Yes (`CodeArgs.kt:42`) | OK |
| `includeTests=false` default | Yes (`CodeArgs.kt:23`) | OK |
| `DumbService` note | Yes | OK |
| Java module conditional load | Yes (`java-introspect.xml`) | OK |
| Anonymous + inner excluded | Yes (via `psiFile.classes` top-level only) | OK |
| 5-section `@McpDescription` | Yes, verbatim from plan | OK |

## Research notes (URLs)

- `ProjectFileIndex` decompile from
  `ideaIU-2025.2.6.2/lib/app-client.jar` (via `javap -p`) confirms
  `isInGeneratedSources(VirtualFile)` is present, abstract, NOT
  deprecated in 252. `FileIndex` (super-interface) provides
  `isInTestSourceContent` + `isInSourceContent`. The fixer note's
  worry about a rename to `isInGeneratedSourceContent` does not
  apply — current code is correct.
- 252-tagged source on GitHub:
  https://github.com/JetBrains/intellij-community/blob/252/platform/projectModel-api/src/com/intellij/openapi/roots/ProjectFileIndex.java
- `FileIndex.iterateContent` javadoc: "Processes all files and
  directories under content roots skipping excluded and ignored files
  and directories." Confirms Finding 2 — resources are visited.
- `PsiPackage.getSubPackages()` (no-arg) returns subpackages across
  all sources INCLUDING library subpackages; the scoped overload
  `getSubPackages(GlobalSearchScope)` is the way to restrict — confirms
  Finding 1. Unofficial docs:
  https://dploeger.github.io/intellij-api-doc/com/intellij/psi/PsiPackage.html
- `PsiClassOwner.classes` returns top-level classes uniformly for
  `.java` and `.kt` (via Kotlin light classes); for a Kotlin file with
  both a class and a top-level `fun`, it returns the source class AND
  the `KtLightClassForFacade` — confirms Finding 5.

## Test coverage assessment

Unit tests for `ClassCatalog`: **0**. Platform tests: **0**. Fixtures
(`src/test/testData/code/catalog/…`): **none present**. The plan's
test plan (lines 207-226 of `docs/plans/code-class-catalog.md`) is
the most detailed in the docs/plans tree for this PR series and was
not executed at all.

Minimum recommended before merge:
- Unit (`ClassCatalogFilterTest.kt`): `matchesPackagePrefix` truth
  table (`com.acme` matches `com.acme.X` + `com.acme.sub.X`, NOT
  `com.acmex`); `clampLimit` rejects 0, -1, 5001 with
  `IllegalArgumentException`; `packageOf` strips simple name; kind
  vocabulary acceptance.
- Platform (`ClassCatalogPlatformTest.kt`, `BasePlatformTestCase` with
  a 2-module Java+Kotlin fixture):
  - module variant: top-level-only (no inner/anonymous); `includeTests`
    toggle; `includeGenerated=false` hides KSP/generated; `packagePrefix`
    honoured; `kinds=["annotation"]` filter; `moduleName="nope"` →
    `McpExpectedError`. **Add a fixture with a `.kt` file under
    `src/main/resources/` to catch Finding 2.**
  - package variant: non-recursive vs recursive; `includeLibraries=false`
    + `recursive=true` + `packageFqn="java"` finishes < 10 s (would
    catch Finding 1); `packageFqn=""` works; missing package returns
    empty (not error).
  - shared: Kotlin top-level `fun foo()` → `kind="kotlinFileFacade"`;
    `limit` triggers `truncated=true`; injected tiny `Clock` →
    `timedOut=true` with partial results (the `Clock` seam exists
    precisely for this — use it).

Mirror `PsiUsageSearcherPlatformTest.kt` for fixture-loading shape.

## Cross-cutting suggestions

- The `Clock` seam (`ClassCatalog.kt:299-304`) is a nice piece of
  engineering for testability but completely wasted while there are
  zero tests. Either land the tests or delete the seam — dead
  abstractions accumulate.
- `internal fun listInModuleForTest` (`ClassCatalog.kt:306-329`) is
  marked `@Suppress("unused")` because no test calls it. Same story —
  drop it or use it.
- `CodeArgs.kt` is technically dead code (Finding 14); decide if
  `code.*` will eventually take args-class parameters (like
  `psi_find_usages`) or stay flat. If flat, delete the file.
- `BFS queue` setup in the package variant (`:168-201`) is correct
  but `seen` is `HashSet<String>` keyed by qualified name. Anonymous/
  unnamed root packages could collide (rare); switch to identity
  hashing keyed by `PsiPackage` if Finding 1 fix doesn't already
  bound the tree.

## References

- `src/main/kotlin/com/github/xepozz/ide/introspector/core/ClassCatalog.kt:46-48,87-115,135-212,242-261,265-275,278-288,290-329`
  — Findings 1, 2, 4, 5, 7, 8, 9, 12, 13.
- `src/main/kotlin/com/github/xepozz/ide/introspector/model/ClassSourceInfo.kt:101-149`
  — `DeclarationRange`, `ClassEntry`, `ListClassesResponse`; Finding 10.
- `src/main/kotlin/com/github/xepozz/ide/introspector/model/args/CodeArgs.kt`
  — Finding 14 (file present but unused).
- `src/main/kotlin/com/github/xepozz/ide/introspector/tools/CodeSourceToolset.kt:372-491`
  — two new `@McpTool` methods; Findings 6, 12.
- `src/main/resources/META-INF/java-introspect.xml`
  — conditional load on `com.intellij.modules.java`; verified.
- `docs/plans/code-class-catalog.md:107-119,168-189,196-226`
  — contract this PR claims to implement (`ClassEntry` shape, edge
  cases, test plan).
- `docs/MCP_TOOLS.md:32-33,702,745` — auto-generated entries confirm
  doc-gen ran.
- 252 `ProjectFileIndex` source (verified via decompile):
  https://github.com/JetBrains/intellij-community/blob/252/platform/projectModel-api/src/com/intellij/openapi/roots/ProjectFileIndex.java
- `PsiPackage` unofficial docs (no-arg vs scoped subPackages):
  https://dploeger.github.io/intellij-api-doc/com/intellij/psi/PsiPackage.html
