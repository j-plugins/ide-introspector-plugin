<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IDE Introspector Changelog

## [Unreleased]
### Added
- **Phase 1 — Tier 1 MCP tools** registered via `com.intellij.mcpServer.mcpToolset` extension point:
  - `ui.get_tree`, `ui.find_by_name`, `ui.find_by_coordinates`, `ui.find_by_xpath`, `ui.get_properties`
  - `screenshot.capture`, `screenshot.crop`
  - `arch.list_extension_points`, `arch.list_extensions_for_ep`, `arch.list_plugins`,
    `arch.get_plugin_details`, `arch.find_extenders_of`
- **Phase 1 — Platform Explorer tool window** (right anchor) with three view modes
  (By Plugin / By Extension Point / By Plugin Dependencies), SpeedSearch, live filter,
  HTML details panel, and copy-id context menu.
- **Phase 2 demo — `exec.execute_kotlin_in_ide`** (opt-in) backed by `kotlin-scripting-jsr223`,
  per-call confirmation dialog, textual safety blacklist (`Runtime.exec`, `ProcessBuilder`,
  `setAccessible(true)`, `System.exit`, `Class.forName("sun.*")`), AST safety checker,
  audit log (category `ide-introspector-audit`), and hard 10 s execution timeout cap.
- Settings page under Settings → Tools → IDE Introspector for the Phase 2 opt-in.
- **`psi.*` tools (4)** — PSI introspection and navigation:
  - `psi.list_open_files` — open editor tabs with active-file marker and per-tab caret offset.
  - `psi.get_structure` — full PSI tree of an open file across every view-provider language,
    with optional injected-language fragments (SQL-in-string, JS-in-HTML, …) and per-node
    stable ids reusable in `psi.get_references`.
  - `psi.get_references` — `PsiReferenceService` resolution (`file` / `at_offset` scope)
    with `PsiPolyVariantReference` expansion.
  - `psi.find_usages` — Find Usages by position with `file` / `project` / `all` scope and
    optional implementation/override folding; local-variable scope is auto-narrowed.
- **`code.*` tools (4)** — class source resolution (loaded only when
  `com.intellij.modules.java` is present; absent in PyCharm CE / RubyMine / GoLand):
  - `code.find_class` — cheap probe returning availability state (`SOURCE` /
    `ATTACHED_SOURCE` / `DECOMPILED` / `STUBS_ONLY` / `NOT_FOUND`) plus metadata.
  - `code.get_source` — class text picking real source > attached source > Fernflower
    decompiled > stubs, truncated to a UTF-8 byte budget.
  - `code.list_members` — methods, constructors, fields and inner classes with signatures
    and modifiers, optionally including inherited members.
  - `code.attach_sources` — best-effort wrapper around the IDE's source-download actions
    for Maven / Gradle library jars.
- Platform Explorer details panel: clickable FQN navigation (plugin id, EP name and class
  links) with structured sections — Breadcrumb, Chips, MembersSection, JavaMembersPreview.
- Internal TTL cache (`core/internal/TtlCache.kt`, `ExtensionMetadata.kt`) for repeated
  EP / extension lookups across `arch.*` tools.
- KSP doc-processor (`doc-processor/`) that regenerates `docs/MCP_TOOLS.md` from
  `@McpTool` / `@McpDescription` annotations on every `./gradlew compileKotlin`.
- Conditional plugin loading via three optional `<depends>` files
  (`mcp-integration.xml`, `java-introspect.xml`, `kotlin-exec.xml`): without the MCP
  Server plugin the Platform Explorer tool window still works; without the Java module
  `code.*` is skipped; without the Kotlin plugin `exec.*` is skipped.

### Changed
- Tier 1 surface grew from 13 tools across four groups (`ui`, `screenshot`, `arch`, `exec`)
  to **21 tools across six groups** (adds `psi`, `code`).
