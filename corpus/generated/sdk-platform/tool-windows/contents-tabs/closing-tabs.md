---
id: sdk.tool-windows.contents-tabs.closing-tabs
title: Tool Windows: Closing Tabs
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, closing, tabs]
---
A plugin can control whether the user is allowed to close tabs either globally or on a per-content basis.
The former is done by passing the `canCloseContents` parameter to the `registerToolWindow()` function, or by specifying `canCloseContents="true"` in `plugin.xml`.
The default value is `false`; calling `setClosable(true)` on `ContentManager` content will be ignored unless `canCloseContents` is explicitly set.

If closing tabs is enabled in general, a plugin can disable closing of specific tabs by calling [Content.setCloseable(false)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/ui/content/Content.java).

