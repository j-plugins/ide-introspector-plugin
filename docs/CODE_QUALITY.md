# Code Quality Criteria — IDE Introspector

A rule set derived from analysis of this codebase and current best practices
(kotlinlang.org coding conventions, IntelliJ SDK 2024–2026, Effective Kotlin,
detekt rule sets — potential-bugs / coroutines / style / exceptions / performance,
idioms from Philipp Hauer / sdkotlin, Kotest/MockK/JUnit 5).

Every rule is a "bad → good" pair with the replacement technique, so it can be
applied on review without interpretation.

The rules in [CLAUDE.md](../CLAUDE.md) (Hard rules: 10 s timeout, MCP API target,
`compileOnly` for kotlinx-serialization, exec as a last resort) remain
authoritative and are not duplicated below.

---

## 1. Kotlin — general code

### 1.1 Null safety and immutability
- **`!!` is forbidden** in production code. Use `?.`, `?:`,
  `requireNotNull(x) { "meaningful message" }`. Acceptable only when the invariant
  is documented and unreachable in practice.
- **`val` by default**, `var` only when mutation is required. Return `List`/`Map`,
  not `MutableList`/`MutableMap`, from public APIs.
- `as?` instead of `as`. Convert platform types (`String!` from Java) to `T?`
  immediately at the boundary.
- `data class.copy(...)` for "updates" — do not expose mutating setters on domain
  models.

### 1.2 Scope functions — by purpose, not for prettiness

| Goal | Function |
|---|---|
| null-safe transform | `?.let { … }` |
| object configuration (DSL-like) | `apply { … }` |
| init + result | `run { … }` |
| side effect in a chain (logging) | `also { … }` |
| group of calls on a ready object | `with(obj) { … }` |

Do not nest scope functions; do not use `apply` for a single line.

### 1.3 Types
- `data class` — value carriers (DTOs) only. Do not add behavior, do not expose
  `var`.
- `sealed interface` for closed hierarchies (results, ADTs). `when` without `else`
  lets the compiler flag the missing branch.
- `@JvmInline value class` for type-safety over a single primitive
  (`ComponentId(String)`, `EpName(String)`).
- `enum class` — only for constants without per-instance data.

### 1.4 Coroutines (structured concurrency)
- **`GlobalScope`, `runBlocking` on the EDT are forbidden.** In the IDE use a
  service scope (constructor injection — see §2.3).
- `withContext(Dispatchers.IO/Default/EDT)` for switching, not `launch().join()`.
- `withTimeoutOrNull` for recoverable cases, `withTimeout` for an error. All
  timeouts ≤ 10 s (see CLAUDE.md).
- A `suspend fun` reads as an action: `fetchUser()`, not `getUserSuspending()`.
- Insert `yield()` in CPU loops to allow cancellation.

### 1.5 Visibility and packages
- **`private` by default**, then `internal`, and only what is needed — `public`.
  Especially important for a plugin: `internal` limits the API surface.
- One public top-level class per file; group sealed children in one file with the
  parent.
- Packages by feature (`core/`, `tools/`, `model/`), not by layer.

### 1.6 Naming
- Classes — `UpperCamelCase`, functions/fields — `lowerCamelCase`, `const val` —
  `UPPER_SNAKE_CASE`.
- Backing properties: `_value` (private) + `value` (public). Booleans: `is…`,
  `has…`, `should…`.
- Do not write comments explaining *what* the code does — names should do that.
  A comment is only for *why* (a hidden invariant, a workaround).

### 1.7 Extension functions — justified, not abused
- **Justified:** add an intent-revealing method to a type you do not control
  (`String.toSlug()`); DSLs; replacing `Utils` classes.
- **Anti-pattern:** extensions on `Any?`, `Any`; extensions that should be a class
  member; extensions that depend on private state.
- Group them in a file named after the receiver (`StringExtensions.kt`).

---

## 2. IntelliJ Platform Plugin

### 2.1 Threading (rules from IntelliJ SDK 2024.1+)
- **EDT — Swing mutations only.** Any other work on the EDT is a violation. Use
  `onEdtBlocking { }` (`util/EdtHelpers.kt`).
- **Reading PSI/indexes — inside a `ReadAction` or `readAction { }`** (suspending,
  since 2024.1). Any PSI operation (even `getName()`) requires a read action.
- **A `PsiElement` must not be held between read actions** — PSI may be reparsed
  in between. Use `SmartPointerManager.createSmartPsiElementPointer(psi)` and check
  `.isValid()` after resolving.
- **`ModalityState.any()` only for pure UI operations** on the EDT while a modal
  dialog is open (our exec-confirmation case). PSI/VFS/project-model changes under
  `any()` are undefined behavior.
- **Do not run heavy operations on the EDT** — `SlowOperations.assertSlowOperationsAreAllowed()`
  will catch it. Move to a background thread instead of suppressing the assert.

### 2.2 PSI and dumb mode
- Mark an action `DumbAware` **only if it does not touch indexes**. Extend
  `DumbAwareAction`, do not override `isDumbAware()`.

### 2.3 Services
- **`@Service(Service.Level.PROJECT|APP)`** + a `final` class (Kotlin default). No
  plugin.xml entry for light services.
- **Never cache a service in a field** of another class —
  `project.getService(Foo::class.java)` on every call (the registry is
  thread-safe).
- **Constructor injection scope:** `class Foo(private val cs: CoroutineScope)` for
  an app service, `(project: Project, cs: CoroutineScope)` for a project service.
  The IDE cancels the scope on unload.
- **No heavy work in the constructor** — it blocks the first call. Use a
  `suspend init()` launched via `cs.launch`.
- **Never use `Application`/`Project` as a parent `Disposable`** — it leaks on
  plugin unload.

### 2.4 Extension points
- **`ep.point.size()`** for counting (adapter count, no instantiation). **Never
  `ep.extensionList.size`** — see CLAUDE.md, it breaks other plugins.
- Declare an EP as
  `private val EP_NAME = ExtensionPointName.create<T>("…")`.
- Do not cache extension instances — dynamic plugins will not survive it.

### 2.5 plugin.xml
- `<depends optional="true" config-file="myPluginId-kotlin.xml">org.jetbrains.kotlin</depends>`
  — our pattern with `kotlin-exec.xml` and `mcp-integration.xml`.
- `<idea-version since-build="252" until-build="252.*"/>` — without
  `pluginUntilBuild=false`.

### 2.6 Logging and errors
- `private val LOG = Logger.getInstance(MyClass::class.java)` (or `thisLogger()`).
  **`println`, `System.out` are forbidden.**
- `LOG.warn(t)` / `LOG.error(t)` — surfaces in the IDE Internal Error reporter. Use
  `PluginException` to attribute the error to our plugin.
- `LOG.debug` — wrap in `if (LOG.isDebugEnabled)` for expensive messages.

---

## 3. MCP tool descriptions

Already defined in CLAUDE.md, restated here as a rule:

1. **What** (one line, present tense, action + scope).
2. **Use this when** — concrete intents.
3. **Do NOT use this when** — pointers to alternative tools.
4. **Returns** — JSON shape, key fields.
5. **Examples** — runnable invocations for non-trivial tools.

Technical requirements:
- Kotlin trim-margin (`""" |line… """`) — the framework calls `trimMargin` via
  reflection.
- `@McpDescription` on **every** parameter (no exceptions).
- The return type is a `@Serializable data class` in `model/`.

---

## 4. Tests

### 4.1 Stack
- **JUnit 5 (Jupiter) baseline** for everything new. Existing JUnit 4 code —
  migrate opportunistically, not in a massive refactor.
- **MockK > Mockito** — native support for `suspend`, extension functions,
  `mockkObject`.
- **Kotest** — optional for pure-Kotlin modules (`core/`, `util/`). **Do not mix
  with `BasePlatformTestCase`** — lifecycle conflict.

### 4.2 Choosing a test base

| What you test | Base |
|---|---|
| Pure logic (XPathMatcher, ImageBudget, TtlCache) | plain JUnit/Kotest |
| PSI / fixture / completion | `BasePlatformTestCase` (fast, shared project) |
| Multi-module, real SDK | `HeavyPlatformTestCase` (only when necessary) |
| Disposable without `Project` | `UsefulTestCase` |

Currently ~60% of tests are platform tests; try to extract logic into pure classes
so it can be tested without the IDE.

### 4.3 Naming
- **Backticks**, describing behavior:
  `` `walker stops at maxDepth and reports truncation`() ``.
- Structure: either `method_state_expectedBehavior` or
  `` `given X, when Y, then Z` `` — pick one and stick with it.
- `testFoo1` / `testCase2` is an anti-pattern.

### 4.4 Test structure
- **Arrange-Act-Assert**, three blocks separated by a blank line. One Act per test —
  if there are several, split it.
- Heavy setup goes in `@BeforeEach` or factory functions, not in every test.
- No logic in a test (`if`, `for` to decide what to assert) — parameterize it
  (`@ParameterizedTest` / Kotest `withData`).

### 4.5 Coroutines
- `runTest { … }` + `StandardTestDispatcher` for determinism. `advanceTimeBy` /
  `advanceUntilIdle`.
- **Inject dispatchers** — no hardcoded `Dispatchers.IO` in production.
- Do not mix `runTest` with `onEdtBlocking` — the EDT is real-threaded, virtual
  time does not work.

### 4.6 Test data
- `src/test/testData/<feature>/before.kt` + `after.kt`, `<caret>`/`<selection>`
  markers, `myFixture.configureByFile(...)` / `checkResultByFile(...)`.
- Do not assert pixel layout in UI tests — assert on the component-tree structure.

### 4.7 Anti-patterns
- **Mock everything** — mock only boundaries (FS, network, IDE services, time).
  Your own data classes and pure functions — no.
- **Testing privates via reflection** — extract into an `internal` class with a
  public API.
- **Hidden dependencies** — no `System.getenv`, real clock, real network. Inject
  them.
- **Assertion roulette** — bare `assertTrue` without messages. Use AssertJ or
  Kotest fluent assertions, or `assertSoftly`.
- **`Thread.sleep` for synchronization** — replace with
  `CountDownLatch.await(timeout)`, `advanceUntilIdle()`, `waitForCondition`.
- **Brittle full-JSON equality** for EDT-collected trees — assert on stable
  structural fields.

### 4.8 Coverage
- 70-80% on core logic is reasonable. Higher is chasing the percentage.
- Exclude from Kover (already done): `tools/` (the McpToolset registry), `model/`
  (data classes), `toolwindow/`, KSP-generated code.
- Optionally — PIT mutation testing on core, only on changed files in CI.

---

## 5. Idioms — modern instead of outdated/verbose

Each item: the Java-style/verbose technique on the left, the idiomatic replacement
on the right. Source: Effective Kotlin, detekt `style`, Philipp Hauer's idioms.

### 5.1 Expression body instead of block-return

```kotlin
fun mapToDto(entity: Entity): Dto { return Dto(entity.code, entity.date) }

fun mapToDto(entity: Entity) = Dto(entity.code, entity.date)
```

`if`/`when`/`try` are expressions — assign their result, do not mutate a `var`:

```kotlin
val locale: Locale
when (area) { "germany" -> locale = Locale.GERMAN; else -> locale = Locale.ENGLISH }

val locale = when (area) { "germany" -> Locale.GERMAN; else -> Locale.ENGLISH }
```

### 5.2 Default + named arguments instead of overloads and builders

```kotlin
fun find(name: String) = find(name, true)
fun find(name: String, recursive: Boolean) { … }
SearchConfig().setRoot("p").setTerm("t").setRecursive(true)

fun find(name: String, recursive: Boolean = true) { … }
SearchConfig(root = "p", term = "t", recursive = true)
```

Named arguments are mandatory for boolean/numeric literals at the call site
(`walk(maxDepth = 12, includeProperties = false)`, not `walk(12, false)`).

### 5.3 `when` instead of an `if-else` chain, `if` instead of a binary `when`

```kotlin
when (x) { null -> true; else -> false }     // detekt UseIfInsteadOfWhen
if (x == null) true else false
```

An argument-less `when` replaces an `else if` ladder. On `sealed`/`enum` — `when`
without `else` (see §6.4).

### 5.4 `require` / `check` / `error` instead of a manual `throw`

The semantics are fixed — do not mix up the exception type:

| Technique | When | Throws |
|---|---|---|
| `require(cond) { msg }` / `requireNotNull` | **argument** validation | `IllegalArgumentException` |
| `check(cond) { msg }` / `checkNotNull` | object **state** validation | `IllegalStateException` |
| `error(msg)` | unreachable branch / invariant | `IllegalStateException` |

```kotlin
if (timeoutMs > 10_000) throw IllegalArgumentException("timeout too large")
fun current() { if (project == null) throw IllegalStateException("no project"); … }

require(timeoutMs <= 10_000) { "timeout must be <= 10_000ms, got $timeoutMs" }
fun current() { checkNotNull(project) { "no project" }; … }
```

### 5.5 Smart-cast via `as?` + `?:` instead of an `is` check plus a cast

```kotlin
if (service !is ExecToolset) throw IllegalStateException(); service.run()

val toolset = service as? ExecToolset ?: error("not an ExecToolset")
toolset.run()
```

### 5.6 Do not wrap a direct call in a scope function

```kotlin
component.let { print(it) }                   // detekt UnnecessaryLet
print(component)

config.apply { version = "1.2" }              // detekt UnnecessaryApply (single field)
config.version = "1.2"
```

In a multiline lambda, give the parameter a name — not a nested `it`:

```kotlin
node.let { println(it); collect(it) }
node.let { current -> println(current); collect(current) }
```

### 5.7 Collection operators and string templates instead of manual loops/concatenation

```kotlin
val ids = mutableListOf<String>()
for (c in components) { if (c.isVisible) ids.add(c.id) }
val msg = "node " + id + " at depth " + depth

val ids = components.filter { it.isVisible }.map { it.id }
val msg = "node $id at depth $depth"
```

Build accumulations with `buildList { }` / `buildString { }`, not a `var acc` plus
mutation.

### 5.8 Extension/top-level instead of an `XxxUtils` object

```kotlin
object StringUtils { fun toSlug(s: String): String = … }
StringUtils.toSlug(name)

fun String.toSlug(): String = …
name.toSlug()
```

(The boundaries from §1.7 apply — no extensions on `Any`/`Any?`.)

---

## 6. Correctness — bug-prone patterns

Source: detekt `potential-bugs`. These are hard blockers on review.

### 6.1 `map[key]!!` → safe access

```kotlin
val toolset = registry["exec"]!!             // NPE if the key is absent
val toolset = registry.getValue("exec")       // NoSuchElementException naming the key
val toolset = registry["exec"] ?: defaultToolset
```

`!!` is forbidden everywhere (see §1.1); `map[k]!!` is its most common source.

### 6.2 Casting nullable → non-null and unsafe casts

```kotlin
val name = bar as String                      // bar: Any? → NPE on null
val name = checkNotNull(bar) as String        // explicit contract
val name = bar as? String                      // or safely: returns null
```

### 6.3 Structural `==` instead of referential `===`

`===`/`!==` are for identity checks only (the same object). For values (`String`,
data classes) use `==`:

```kotlin
if (id === otherId) …                          // detekt AvoidReferentialEquality
if (id == otherId) …
```

### 6.4 `equals`/`hashCode` and "do not throw from them"

- Override `equals` → override `hashCode` (and vice versa). The parameter is
  `Any?`, not a concrete type (otherwise it is not an override).
- **Never throw** from `equals` / `hashCode` / `toString` — they are called by
  logging, collections, and the debugger (detekt `ExceptionRaisedInUnexpectedLocation`).
- For value carriers use a `data class`, not hand-written implementations (§1.3).

### 6.5 Exhaustive `when` without `else`

On `sealed`/`enum` write `when` **without** `else` — the compiler forces you to
cover a new branch when the hierarchy grows. A redundant `else` in an exhaustive
`when` is a smell (detekt `ElseCaseInsteadOfExhaustiveWhen`); it silently swallows
the new variant.

### 6.6 A null-check on a `var` is unsound

After the check, another thread/reentrant call may have changed the `var`
property. Capture it into a local `val`:

```kotlin
if (cachedScope != null) cachedScope.launch { … }     // smart-cast is impossible

val scope = cachedScope ?: return
scope.launch { … }
```

### 6.7 Unreachable code / catch

Code after `return`/`throw`/`break`/`continue`, and a `catch` of a broader type
above a narrower one, are dead branches. Delete them, do not comment them out.

---

## 7. Exceptions and error handling

Source: detekt `exceptions`. Complements §2.6 (logging).

- **Do not catch generic** `Exception`/`Throwable`/`RuntimeException` — catch a
  concrete type (`IOException`, `JsonDecodingException`). A broad catch hides bugs.
- **Do not throw generic** — `throw IllegalArgumentException("maxDepth must be > 0")`,
  not `throw Exception()`. Always with a meaningful message.
- **Do not swallow an exception** — preserve the cause:

  ```kotlin
  catch (e: IOException) { throw ToolError(e.message) }   // stack trace lost
  catch (e: IOException) { throw ToolError(e) }            // cause preserved
  catch (e: IOException) { }                               // empty catch — forbidden
  ```

- **`return`/`throw` from `finally`** swallow the original exception — forbidden. A
  `finally` block holds only idempotent cleanup that does not throw.
- **`printStackTrace()` / `System.out`** are forbidden — use `LOG` only (§2.6).
- **`TODO()` / `NotImplementedError()`** must not reach a merge.
- **Expected errors are not exceptions.** For business results with a predictable
  failure, use a `sealed interface` result or `kotlin.Result` so the compiler
  forces the branch to be handled (§1.3).
- **In coroutines, do not swallow `CancellationException`** — `runCatching` catches
  it too; if you use `try/catch`, rethrowing `CancellationException` is mandatory,
  otherwise structured concurrency breaks.

---

## 8. Performance — cheap wins

Source: detekt `performance`. Especially important on the EDT (§2.1, cheap
defaults).

- **`Sequence` for long lazy chains** on large collections — no intermediate lists;
  for short chains / small collections, eager operators are faster (no wrapper
  overhead):

  ```kotlin
  nodes.map { transform(it) }.filter { it.visible }.map { it.id }      // 2 temp lists
  nodes.asSequence().map { transform(it) }.filter { it.visible }.map { it.id }.toList()
  ```

- **Primitive arrays** — `IntArray`/`LongArray` instead of `Array<Int>` (no boxing)
  on hot paths.
- **`for` instead of `forEach` over a range** — `for (i in 1..n)`, not
  `(1..n).forEach`.
- **Do not spread an existing array** (`f(*existingArray)` copies it); a spread is
  justified only for an `arrayOf(...)` built right there.
- **No needless temporary instances** (`Integer(1).toString()` → `1.toString()`).
- **`by lazy` for expensive initialization** of a property instead of work in the
  constructor (echoes §2.3 "no heavy work in a service constructor").

---

## Where to apply

These rules are the criteria for:
- Code review (self-check before commit).
- Decisions in ambiguous cases ("should I mock this service?", "where does this
  function go?").
- Context when working with agents (`/review`, `/security-review`).
