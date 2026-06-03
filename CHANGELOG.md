<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IDE Introspector Changelog

## [Unreleased]

## [2026.0.3] - 2026-06-03

### Added

- **UI interaction tools** — act on widgets, not just read them:
  - `ui.list_items` (enumerate tree/list/table/tabbedPane/comboBox items),
    `ui.select_item`, `ui.activate`, `ui.click`
- **`ide.indexing_status`** — report dumb (indexing) vs smart (ready) mode for the focused project.
- **Editor introspection** — `editor.list_tabs` (open tabs grouped by split window) and
  `editor.get_active` (focused editor's file, caret, selection).

### Changed

- `ui.find_by_xpath` and `ui.find_by_name` now match components across their full superclass
  hierarchy (`//Tree` / `//JTree` locate a `ProjectViewTree`). Components expose `classHierarchy`,
  and `ui.find_by_name` supports `searchIn=["className"]`.

### Fixed

- `ui.activate` dispatches a faithful double-click progression (clickCount 1 then 2) so
  IntelliJ's open-on-double-click navigation fires, not just tree expand/collapse.

## [2026.0.2] - 2026-05-24

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
  per-call confirmation dialog, textual safety blacklist, and audit log.
- Settings page under Settings → Tools → IDE Introspector for the Phase 2 opt-in.

[Unreleased]: https://github.com/xepozz/introspector-plugin/compare/2026.0.3...HEAD
[2026.0.3]: https://github.com/xepozz/introspector-plugin/compare/2026.0.2...2026.0.3
[2026.0.2]: https://github.com/xepozz/introspector-plugin/commits/2026.0.2
