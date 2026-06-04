---
id: sdk.custom-settings-groups
title: Custom Settings Groups
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, custom, settings, groups]
---
As described in [Extension Points for Settings](https://plugins.jetbrains.com/docs/intellij/settings-guide.html#extension-points-for-settings), custom Settings can be declared as children of existing parent groups such as `Tools`.
These parent groups are the existing categories of Settings in the IntelliJ Platform-based IDE.

However, suppose the custom Settings are rich enough to require multiple levels?
For example, a custom Setting implementation has multiple sub-Settings implementations.
Extension Point declarations can create this kind of multilayer Settings hierarchy.

Tip:

See [Inspecting Settings](https://plugins.jetbrains.com/docs/intellij/internal-ui-inspector.html#inspecting-settings) on how to gather information in the IDE instance for Settings dialog.

## Extension Points for Parent-Child Settings Relationships (custom-settings-groups/extension-points-for-parent-child-settings-relationships.md)
### Parent-Child Settings Using Separate EPs (custom-settings-groups/extension-points-for-parent-child-settings-relationships/parent-child-settings-using-separate-eps.md)
### Parent-Child Settings Using Nested EPs (custom-settings-groups/extension-points-for-parent-child-settings-relationships/parent-child-settings-using-nested-eps.md)
### Attributes for Parent-Child Settings EPs (custom-settings-groups/extension-points-for-parent-child-settings-relationships/attributes-for-parent-child-settings-eps.md)
## Implementations for Parent-Child Settings (custom-settings-groups/implementations-for-parent-child-settings.md)
### Configurable Marker Interfaces (custom-settings-groups/implementations-for-parent-child-settings/configurable-marker-interfaces.md)

> Source: IntelliJ Platform SDK docs — Custom Settings Groups (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
