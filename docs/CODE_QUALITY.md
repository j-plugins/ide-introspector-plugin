# Критерии качества кода — IDE Introspector

Свод правил на основе анализа кодовой базы и актуальных best practices
(kotlinlang.org coding conventions, IntelliJ SDK 2024–2026, Effective Kotlin,
detekt rule sets — potential-bugs / coroutines / style / exceptions / performance,
идиомы Philipp Hauer / sdkotlin, Kotest/MockK/JUnit 5).

Каждое правило — пара «плохо → хорошо» с приёмом-заменой, чтобы применять на ревью
без интерпретации.

Правила в [CLAUDE.md](../CLAUDE.md) (Hard rules: таймаут 10 s, MCP API target,
`compileOnly` для kotlinx-serialization, exec — последнее средство) остаются
авторитетными и не дублируются ниже.

---

## 1. Kotlin — общий код

### 1.1 Null safety и иммутабельность
- **`!!` запрещён** в production-коде. Используй `?.`, `?:`,
  `requireNotNull(x) { "осмысленное сообщение" }`. Допустимо только если инвариант
  задокументирован и недостижим на практике.
- **`val` по умолчанию**, `var` — только при необходимости мутации. Возвращай
  `List`/`Map`, не `MutableList`/`MutableMap`, из публичных API.
- `as?` вместо `as`. Платформенные типы (`String!` из Java) приводи к `T?` сразу
  на границе.
- `data class.copy(...)` для "обновления" — не выставляй мутирующие сеттеры на
  доменных моделях.

### 1.2 Scope-функции — по назначению, не для красоты

| Цель | Функция |
|---|---|
| null-safe трансформ | `?.let { … }` |
| настройка объекта (DSL-like) | `apply { … }` |
| init + результат | `run { … }` |
| побочный эффект в цепочке (лог) | `also { … }` |
| группа вызовов на готовом объекте | `with(obj) { … }` |

Не вкладывай scope-функции друг в друга; не используй `apply` ради одной строки.

### 1.3 Типы
- `data class` — только value carriers (DTO). Не добавляй поведение, не выставляй
  `var`.
- `sealed interface` для закрытых иерархий (results, ADT). `when` без `else` —
  компилятор сам подскажет недостающую ветку.
- `@JvmInline value class` для type-safety над одиночным примитивом
  (`ComponentId(String)`, `EpName(String)`).
- `enum class` — только для констант без per-instance данных.

### 1.4 Coroutines (structured concurrency)
- **`GlobalScope`, `runBlocking` на EDT — запрещены.** В IDE используй scope
  сервиса (constructor injection — см. §2.3).
- `withContext(Dispatchers.IO/Default/EDT)` для переключения, не `launch().join()`.
- `withTimeoutOrNull` для recoverable, `withTimeout` для ошибки. Все таймауты
  ≤ 10 с (см. CLAUDE.md).
- `suspend fun` читается как действие: `fetchUser()`, не `getUserSuspending()`.
- В CPU-циклах вставляй `yield()` для cancellation.

### 1.5 Видимость и пакеты
- **По умолчанию `private`**, затем `internal`, и только нужное — `public`.
  Особенно важно для плагина: `internal` ограничивает API-поверхность.
- Один публичный top-level класс на файл; sealed-дети группируй в один файл с
  родителем.
- Пакеты — по фиче (`core/`, `tools/`, `model/`), не по слою.

### 1.6 Именование
- Классы — `UpperCamelCase`, функции/поля — `lowerCamelCase`, `const val` —
  `UPPER_SNAKE_CASE`.
- Backing properties: `_value` (private) + `value` (public). Boolean: `is…`,
  `has…`, `should…`.
- Не пиши комментарии, объясняющие *что* делает код — имена должны это сделать.
  Комментарий — только для *почему* (скрытый инвариант, workaround).

### 1.7 Extension functions — оправдано, не злоупотреблять
- **Оправдано:** добавить intent-revealing метод к типу, который ты не
  контролируешь (`String.toSlug()`); DSL; замена `Utils`-классов.
- **Антипаттерн:** расширения `Any?`, `Any`; расширения, которые должны быть
  членом класса; расширения с зависимостью от приватного состояния.
- Группируй в файле, названном по receiver-у (`StringExtensions.kt`).

---

## 2. IntelliJ Platform Plugin

### 2.1 Threading (правила из IntelliJ SDK 2024.1+)
- **EDT — только Swing-мутации.** Любая другая работа на EDT — нарушение.
  Используй `onEdtBlocking { }` (`util/EdtHelpers.kt`).
- **Чтение PSI/индексов — в `ReadAction` или `readAction { }`** (suspending,
  since 2024.1). Любые PSI-операции (даже `getName()`) требуют RA.
- **`PsiElement` нельзя удерживать между ReadAction-ами** — между ними PSI может
  быть переразобран. Используй
  `SmartPointerManager.createSmartPsiElementPointer(psi)` и проверяй `.isValid()`
  после ресолва.
- **`ModalityState.any()` только для чисто UI-операций** на EDT при открытом
  модальном диалоге (наш case с exec-confirmation). Изменения PSI/VFS/project
  model под `any()` — UB.
- **Не пиши тяжёлые операции на EDT** — `SlowOperations.assertSlowOperationsAreAllowed()`
  поймает. Уйди в BG, а не подавляй ассерт.

### 2.2 PSI и dumb mode
- Помечай action `DumbAware` **только если он не трогает индексы**. Расширяй
  `DumbAwareAction`, не переопределяй `isDumbAware()`.

### 2.3 Сервисы
- **`@Service(Service.Level.PROJECT|APP)`** + `final` класс (Kotlin default).
  Без записи в plugin.xml для light services.
- **Никогда не кешируй сервис в поле** другого класса —
  `project.getService(Foo::class.java)` на каждый вызов (registry — thread-safe).
- **Constructor injection scope:** `class Foo(private val cs: CoroutineScope)`
  для app-сервиса, `(project: Project, cs: CoroutineScope)` для project-сервиса.
  IDE сам кэнсельнёт scope при unload.
- **Никакой тяжёлой работы в конструкторе** — он блокирует первый вызов. Делай
  `suspend init()` и запускай через `cs.launch`.
- **Никогда не используй `Application`/`Project` как parent `Disposable`** —
  утечка при unload плагина.

### 2.4 Extension points
- **`ep.point.size()`** для подсчёта (adapter count, без инстанциирования).
  **Никогда `ep.extensionList.size`** — см. CLAUDE.md, ломает другие плагины.
- Объявляй EP как
  `private val EP_NAME = ExtensionPointName.create<T>("…")`.
- Не кешируй extension instances — динамические плагины этого не переживут.

### 2.5 plugin.xml
- `<depends optional="true" config-file="myPluginId-kotlin.xml">org.jetbrains.kotlin</depends>`
  — наш паттерн с `kotlin-exec.xml` и `mcp-integration.xml`.
- `<idea-version since-build="252" until-build="252.*"/>` — без
  `pluginUntilBuild=false`.

### 2.6 Логирование и ошибки
- `private val LOG = Logger.getInstance(MyClass::class.java)` (или
  `thisLogger()`). **Запрещены `println`, `System.out`.**
- `LOG.warn(t)` / `LOG.error(t)` — попадает в IDE Internal Error reporter.
  Используй `PluginException` для атрибуции к нашему плагину.
- `LOG.debug` — оборачивай `if (LOG.isDebugEnabled)` для дорогих сообщений.

---

## 3. MCP tool descriptions

Уже задано в CLAUDE.md, явно фиксирую как правило:

1. **What** (одна строка, present tense, action + scope).
2. **Use this when** — конкретные интенты.
3. **Do NOT use this when** — указатели на альтернативные tools.
4. **Returns** — форма JSON, ключевые поля.
5. **Examples** — runnable invocations для нетривиальных tools.

Технические требования:
- Kotlin trim-margin (`""" |line… """`) — framework вызывает `trimMargin` через
  рефлексию.
- `@McpDescription` на **каждом** параметре (без исключений).
- Возвращаемый тип — `@Serializable data class` в `model/`.

---

## 4. Тесты

### 4.1 Стек
- **JUnit 5 (Jupiter) baseline** для всего нового. Текущий код на JUnit 4 —
  миграция оппортунистически, не massive refactor.
- **MockK > Mockito** — нативная поддержка `suspend`, extension functions,
  `mockkObject`.
- **Kotest** — опционально для чисто-Kotlin модулей (`core/`, `util/`). **Не
  смешивать со `BasePlatformTestCase`** — конфликт lifecycle.

### 4.2 Выбор test base

| Что тестируешь | База |
|---|---|
| Pure logic (XPathMatcher, ImageBudget, TtlCache) | plain JUnit/Kotest |
| PSI / fixture / completion | `BasePlatformTestCase` (быстрая, shared project) |
| Multi-module, real SDK | `HeavyPlatformTestCase` (только при необходимости) |
| Disposable без `Project` | `UsefulTestCase` |

Сейчас ~60% тестов — platform; пытайся выносить логику в pure-классы, чтобы
тестировать их без IDE.

### 4.3 Naming
- **Backticks**, описание поведения:
  `` `walker stops at maxDepth and reports truncation`() ``.
- Структура: либо `method_state_expectedBehavior`, либо
  `` `given X, when Y, then Z` `` — выбери одну и держись.
- `testFoo1` / `testCase2` — антипаттерн.

### 4.4 Структура теста
- **Arrange-Act-Assert**, три блока через пустую строку. Один Act per test —
  если их несколько, разбей.
- Тяжёлый setup — в `@BeforeEach` или factory-функции, не в каждом тесте.
- Никакой логики в тесте (`if`, `for` для решения, что ассертить) —
  параметризуй (`@ParameterizedTest` / Kotest `withData`).

### 4.5 Coroutines
- `runTest { … }` + `StandardTestDispatcher` для детерминизма. `advanceTimeBy`
  / `advanceUntilIdle`.
- **Инжектируй диспетчеры** — никаких хардкод `Dispatchers.IO` в production.
- Не миксуй `runTest` с `onEdtBlocking` — EDT real-threaded, virtual time не
  работает.

### 4.6 Testdata
- `src/test/testData/<feature>/before.kt` + `after.kt`, `<caret>`/`<selection>`
  маркеры, `myFixture.configureByFile(...)` / `checkResultByFile(...)`.
- Не assert pixel-layout в UI-тестах — assert на структуру дерева компонентов.

### 4.7 Антипаттерны
- **Mock everything** — мокаем только границы (FS, network, IDE-сервисы, время).
  Свои data classes и pure functions — нет.
- **Тестирование private через reflection** — извлеки в `internal` класс с
  публичным API.
- **Hidden dependencies** — никаких `System.getenv`, real clock, real network.
  Инжектируй.
- **Assertion roulette** — голые `assertTrue` без сообщений. Используй AssertJ
  или Kotest fluent assertions, либо `assertSoftly`.
- **`Thread.sleep` для синхронизации** — замени на
  `CountDownLatch.await(timeout)`, `advanceUntilIdle()`, `waitForCondition`.
- **Brittle full-JSON-equality** для EDT-collected trees — assert по стабильным
  структурным полям.

### 4.8 Coverage
- 70-80% на core logic — здраво. Выше — погоня за процентом.
- Исключи из Kover (уже сделано): `tools/` (McpToolset реестр), `model/`
  (data classes), `toolwindow/`, KSP-генерируемое.
- При желании — PIT mutation testing на core, только на changed files в CI.

---

## 5. Идиомы — современное вместо устаревшего/многословного

Каждый пункт — Java-style/многословный приём слева, идиоматичная замена справа.
Источник: Effective Kotlin, detekt `style`, идиомы Philipp Hauer.

### 5.1 Expression body вместо block-return

```kotlin
fun mapToDto(entity: Entity): Dto { return Dto(entity.code, entity.date) }

fun mapToDto(entity: Entity) = Dto(entity.code, entity.date)
```

`if`/`when`/`try` — выражения, присваивай их результат, не мутируй `var`:

```kotlin
val locale: Locale
when (area) { "germany" -> locale = Locale.GERMAN; else -> locale = Locale.ENGLISH }

val locale = when (area) { "germany" -> Locale.GERMAN; else -> Locale.ENGLISH }
```

### 5.2 Default + named аргументы вместо overload-ов и builder-ов

```kotlin
fun find(name: String) = find(name, true)
fun find(name: String, recursive: Boolean) { … }
SearchConfig().setRoot("p").setTerm("t").setRecursive(true)

fun find(name: String, recursive: Boolean = true) { … }
SearchConfig(root = "p", term = "t", recursive = true)
```

Именованные аргументы обязательны для boolean/числовых литералов в вызове
(`walk(maxDepth = 12, includeProperties = false)` — не `walk(12, false)`).

### 5.3 `when` вместо цепочки `if-else`, `if` вместо бинарного `when`

```kotlin
when (x) { null -> true; else -> false }     // detekt UseIfInsteadOfWhen
if (x == null) true else false
```

`when` без аргумента заменяет лестницу `else if`. На `sealed`/`enum` — `when` без
`else` (см. §6.4).

### 5.4 `require` / `check` / `error` вместо ручного `throw`

Семантика фиксирована — не путать тип исключения:

| Приём | Когда | Бросает |
|---|---|---|
| `require(cond) { msg }` / `requireNotNull` | проверка **аргумента** | `IllegalArgumentException` |
| `check(cond) { msg }` / `checkNotNull` | проверка **состояния** объекта | `IllegalStateException` |
| `error(msg)` | недостижимая ветка / инвариант | `IllegalStateException` |

```kotlin
if (timeoutMs > 10_000) throw IllegalArgumentException("timeout too large")
fun current() { if (project == null) throw IllegalStateException("no project"); … }

require(timeoutMs <= 10_000) { "timeout must be <= 10_000ms, got $timeoutMs" }
fun current() { checkNotNull(project) { "no project" }; … }
```

### 5.5 Smart-cast через `as?` + `?:` вместо `is`-проверки и каста

```kotlin
if (service !is ExecToolset) throw IllegalStateException(); service.run()

val toolset = service as? ExecToolset ?: error("not an ExecToolset")
toolset.run()
```

### 5.6 Не оборачивай в scope-функцию то, что вызывается напрямую

```kotlin
component.let { print(it) }                   // detekt UnnecessaryLet
print(component)

config.apply { version = "1.2" }              // detekt UnnecessaryApply (одно поле)
config.version = "1.2"
```

В многострочной лямбде давай параметру имя — не вложенный `it`:

```kotlin
node.let { println(it); collect(it) }
node.let { current -> println(current); collect(current) }
```

### 5.7 Операторы коллекций и string-template вместо ручных циклов/конкатенации

```kotlin
val ids = mutableListOf<String>()
for (c in components) { if (c.isVisible) ids.add(c.id) }
val msg = "node " + id + " at depth " + depth

val ids = components.filter { it.isVisible }.map { it.id }
val msg = "node $id at depth $depth"
```

Аккумуляцию строй через `buildList { }` / `buildString { }`, не `var acc` + мутация.

### 5.8 Extension/top-level вместо `XxxUtils`-объекта

```kotlin
object StringUtils { fun toSlug(s: String): String = … }
StringUtils.toSlug(name)

fun String.toSlug(): String = …
name.toSlug()
```

(Границы из §1.7 действуют — без расширений `Any`/`Any?`.)

---

## 6. Корректность — паттерны-источники багов

Источник: detekt `potential-bugs`. Это hard-блокеры на ревью.

### 6.1 `map[key]!!` → безопасный доступ

```kotlin
val toolset = registry["exec"]!!             // NPE если ключа нет
val toolset = registry.getValue("exec")       // NoSuchElementException с именем ключа
val toolset = registry["exec"] ?: defaultToolset
```

`!!` запрещён повсеместно (см. §1.1); `map[k]!!` — самый частый его источник.

### 6.2 Каст nullable → non-null и небезопасный каст

```kotlin
val name = bar as String                      // bar: Any? → NPE при null
val name = checkNotNull(bar) as String        // явный контракт
val name = bar as? String                      // или безопасно: вернёт null
```

### 6.3 Структурное `==` вместо ссылочного `===`

`===`/`!==` — только для проверки идентичности (тот же объект). Для значений
(`String`, data class) используй `==`:

```kotlin
if (id === otherId) …                          // detekt AvoidReferentialEquality
if (id == otherId) …
```

### 6.4 `equals`/`hashCode` и «не бросай из них»

- Переопределил `equals` → переопредели `hashCode` (и наоборот). Параметр —
  `Any?`, не конкретный тип (иначе это не override).
- **Никогда не бросай** из `equals` / `hashCode` / `toString` — их зовёт логирование,
  коллекции, отладчик (detekt `ExceptionRaisedInUnexpectedLocation`).
- Для value-носителей — `data class`, не ручные реализации (§1.3).

### 6.5 Исчерпывающий `when` без `else`

На `sealed`/`enum` пиши `when` **без** `else` — компилятор заставит покрыть новую
ветку при расширении иерархии. Лишний `else` в исчерпывающем `when` — смелл
(detekt `ElseCaseInsteadOfExhaustiveWhen`), он молча проглотит новый вариант.

### 6.6 Null-check на `var` несостоятелен

После проверки `var`-свойство мог изменить другой поток/реентрант. Захвати в
локальный `val`:

```kotlin
if (cachedScope != null) cachedScope.launch { … }     // smart-cast невозможен

val scope = cachedScope ?: return
scope.launch { … }
```

### 6.7 Unreachable code / catch

Код после `return`/`throw`/`break`/`continue` и `catch` более широкого типа выше
узкого — мёртвые ветки. Удаляй, не комментируй.

---

## 7. Исключения и обработка ошибок

Источник: detekt `exceptions`. Дополняет §2.6 (логирование).

- **Не лови обобщённое** `Exception`/`Throwable`/`RuntimeException` — лови
  конкретный тип (`IOException`, `JsonDecodingException`). Широкий catch прячет баги.
- **Не бросай обобщённое** — `throw IllegalArgumentException("maxDepth must be > 0")`,
  не `throw Exception()`. Всегда с осмысленным сообщением.
- **Не глотай исключение** — сохраняй причину:

  ```kotlin
  catch (e: IOException) { throw ToolError(e.message) }   // стек потерян
  catch (e: IOException) { throw ToolError(e) }            // cause сохранён
  catch (e: IOException) { }                               // пустой catch — запрещён
  ```

- **`return`/`throw` из `finally`** проглатывают исходное исключение — запрещены.
  В `finally` только идемпотентный cleanup, который сам не бросает.
- **`printStackTrace()` / `System.out`** запрещены — только `LOG` (§2.6).
- **`TODO()` / `NotImplementedError()`** не должны попадать в merge.
- **Ожидаемые ошибки — не исключения.** Для бизнес-результатов с предсказуемым
  провалом — `sealed interface`-результат или `kotlin.Result`, чтобы компилятор
  заставил обработать ветку (§1.3).
- **В корутинах не глотай `CancellationException`** — `runCatching` ловит и его;
  если используешь `try/catch`, проброс `CancellationException` обязателен, иначе
  ломается structured concurrency.

---

## 8. Производительность — дешёвые выигрыши

Источник: detekt `performance`. Особенно важно на EDT (§2.1, дешёвые дефолты).

- **`Sequence` для длинных ленивых цепочек** на больших коллекциях — нет
  промежуточных списков; для коротких цепочек / малых коллекций eager-операторы
  быстрее (нет накладных на обёртку):

  ```kotlin
  nodes.map { transform(it) }.filter { it.visible }.map { it.id }      // 2 временных списка
  nodes.asSequence().map { transform(it) }.filter { it.visible }.map { it.id }.toList()
  ```

- **Примитивные массивы** — `IntArray`/`LongArray` вместо `Array<Int>` (нет
  боксинга) на горячих путях.
- **`for` вместо `forEach` по диапазону** — `for (i in 1..n)`, не `(1..n).forEach`.
- **Не разворачивай готовый массив через spread** (`f(*existingArray)` копирует);
  spread оправдан только для тут же построенного `arrayOf(...)`.
- **Без лишних temporary-инстансов** (`Integer(1).toString()` → `1.toString()`).
- **`by lazy` для дорогой инициализации** свойства вместо работы в конструкторе
  (перекликается с §2.3 «никакой тяжёлой работы в конструкторе сервиса»).

---

## Куда применять

Эти правила — критерий для:
- Code review (само-проверка перед commit).
- Принятия решений в спорных моментах ("надо ли мокать сервис?", "куда положить
  функцию?").
- Контекста при работе с агентами (`/review`, `/security-review`).
