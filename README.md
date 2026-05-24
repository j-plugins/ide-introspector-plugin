# IDE Introspector

![Build](https://github.com/xepozz/introspector-plugin/workflows/Build/badge.svg)

Exposes the running IntelliJ-based IDE to MCP clients (Claude, Cursor, Codex, ...) as a
set of tools for UI introspection, plugin-architecture exploration, and on-demand
Kotlin code execution. Ships with a **Platform Explorer** tool window for browsing the
live extension-point / plugin graph without an MCP client attached.

## Architecture

Two tiers:

| Tier | Coverage | Latency | Safety |
|------|----------|---------|--------|
| Tier 1: pre-built MCP tools | ~80% of routine introspection | 1–50 ms | whitelist of read-only operations |
| Tier 2: `exec.execute_kotlin_in_ide` | the remaining 20% | 1–5 s (compile) | opt-in + per-call confirmation + textual blacklist |

### Tier 1 — pre-built MCP tools

Registered via the `com.intellij.mcpServer.mcpToolset` extension point. All become visible
to MCP clients once the bundled MCP Server plugin is enabled. There are 21 tools across
six groups:

- `ui.*` (5) — live Swing component tree, by-name / by-coordinates / by-XPath lookup, per-id property bag.
- `screenshot.*` (2) — PNG capture (component / active frame / all frames / virtual desktop) and rectangular crop.
- `arch.*` (5) — extension-point and plugin inventory of the running IDE.
- `psi.*` (4) — open editor tabs, full PSI tree (with injections), reference resolution, Find Usages.
- `code.*` (4) — FQN resolution, class source (real / attached / decompiled / stubs), member listing, source download. Only loaded in IDEs that ship the Java module.
- `exec.*` (1, opt-in) — `execute_kotlin_in_ide` escape hatch. Only loaded when the Kotlin plugin is present.

**Conditional loading.** Optional dependencies on `com.intellij.mcpServer`,
`com.intellij.modules.java` and `org.jetbrains.kotlin` mean the plugin degrades
gracefully:

| Host IDE                    | `ui.*` / `screenshot.*` / `arch.*` / `psi.*` | `code.*` | `exec.*` |
|-----------------------------|----------------------------------------------|----------|----------|
| IDEA Community / Ultimate, Android Studio | ✅ | ✅ | ✅ |
| PyCharm CE / RubyMine / GoLand / WebStorm | ✅ | — | ✅ (if Kotlin plugin installed) |
| Any IDE without MCP Server plugin         | — (Platform Explorer tool window still works) | — | — |

**Full tool reference with every parameter and example:** [`docs/MCP_TOOLS.md`](docs/MCP_TOOLS.md)
— generated from the source-level `@McpDescription` annotations at build time
(`./gradlew generateToolsDoc`), so there's only one place to edit.

Component ids returned by `ui.*` tools are stable for the duration of the IDE session and
can be passed back into `ui.get_properties` / `screenshot.capture`.

### Tier 1 — Platform Explorer tool window

Right-side tool window with three view modes:

1. **By Plugin** — each plugin with declared EPs and registered extensions.
2. **By Extension Point** — each EP with the list of registered implementations.
3. **By Plugin Dependencies** — declared `<depends>` graph.

Features: SpeedSearch, live filter input (200 ms debounce), HTML details panel,
right-click "Copy plugin id / EP name / class name", refresh button.

### Tier 2 — `exec.execute_kotlin_in_ide` (Phase 2 demo)

Compiles and executes arbitrary Kotlin in the IDE JVM. **Disabled by default**;
toggle in Settings → Tools → IDE Introspector.

User code is wrapped with three implicit helpers (`read`, `write`, `onEdt`) and runs
inside an auto-disposed `Disposable` scope so subscriptions are cleaned up between calls.

**Security**:

1. Off by default (`enabled = false` in `ExecSettings`).
2. Per-call confirmation dialog inside the IDE (with session-only bypass).
3. Textual blacklist: `Runtime.exec`, `ProcessBuilder`, `setAccessible(true)`,
   `System.exit`, `Class.forName("sun.*")`.
4. Audit log to `idea.log` (category `ide-introspector-audit`).
5. Hard execution timeout (default 10 s, capped at `maxTimeoutMs = 10 000`). The 10 s
   cap is a project-wide hard rule — see [`CLAUDE.md`](CLAUDE.md) "Timeouts".

**Demo implementation note**: the spec calls for forking LivePlugin's compiler bootstrap.
This demo takes a smaller shortcut and uses `kotlin-scripting-jsr223` (which internally
wraps `kotlin-compiler-embeddable`). This keeps the diff small and gives us a fresh
classloader per call out of the box, but bundles ~50 MB of compiler classes into the
plugin zip. A future iteration should either:

- Switch to the LivePlugin fork once measured cold-start latency justifies the operational
  cost of pinning Kotlin compiler versions, or
- Reuse a single compiler daemon across calls to cut compilation overhead.

## Manual verification

After running `./gradlew runIde`:

1. **Tool window**: open the *Platform Explorer* tool window on the right. Switch
   between view modes; type in the filter to narrow the tree.
2. **MCP tools (with MCP Inspector or Claude Desktop)**:
   - `arch.list_extension_points` — should return ≥ 1000 EPs on a vanilla IDEA.
   - `ui.get_tree` with default args — should return ≥ 50 nodes.
   - `ui.find_by_xpath` with `//div[@class='ActionButton' and @text='Run']` — should find
     the Run button on the main toolbar.
   - `arch.find_extenders_of` with target `com.intellij.toolWindow` — should list every
     `ToolWindowFactory` implementation.
   - `psi.list_open_files` — should return the focused tab plus every other open editor.
   - `psi.get_structure` on an open `.kt` file — should return one `psiFiles[]` root plus
     any string-injected fragments under `injections[]`.
   - `code.find_class` with `fqn="java.util.HashMap"` — should return state
     `ATTACHED_SOURCE` (with JBR sources) or `DECOMPILED` (Fernflower fallback).
3. **Kotlin execution**: enable in Settings, then call `exec.execute_kotlin_in_ide` with
   code like `1 + 1` or `project?.name`. A confirmation dialog should pop up; on accept,
   the result should round-trip as JSON.

## Project layout

```
src/main/kotlin/com/github/xepozz/ide/introspector/
├── core/                — ComponentRegistry, ComponentTreeWalker, ComponentSerializer,
│   │                     XPathMatcher, ScreenshotCapture, PluginInventory,
│   │                     ExtensionPointInspector, ClassSourceResolver,
│   │                     PsiStructureWalker, PsiReferenceCollector, PsiUsageSearcher,
│   │                     PsiModifiers
│   └── internal/        — TtlCache, ExtensionMetadata (cached EP/extension lookup)
├── model/               — Serializable response types (ComponentInfo, PluginInfo,
│   │                     ExtensionInfo, ExtensionPointInfo, ClassSourceInfo, PsiInfo, …)
│   └── args/            — @Serializable args (UiArgs, ArchArgs, PsiArgs, ScreenshotArgs, ExecArgs)
├── tools/               — one McpToolset class per group:
│                          UiInspectorToolset / ScreenshotToolset / ArchitectureToolset /
│                          PsiToolset / CodeSourceToolset / ExecToolset
├── toolwindow/          — Platform Explorer (panel, tree model, cell renderer, nodes,
│   │                     view modes, details panel)
│   └── details/         — Breadcrumb, Chips, FqnLink, MembersSection,
│                          JavaMembersPreview, DetailForm, DetailViews
├── exec/                — Settings, Configurable, ConfirmationManager, AstSafetyChecker,
│                          AuditLogger, KotlinExecutor, ResultSerializer, CodeWrapper
└── util/                — EdtHelpers, ImageEncoding, Utf8Truncation
```

The KSP module under `doc-processor/` regenerates `docs/MCP_TOOLS.md` from
`@McpTool` / `@McpDescription` annotations on every `./gradlew compileKotlin`.

## Build

```bash
./gradlew buildPlugin
# produces build/distributions/ide-introspector-<version>.zip
```
