---
id: sdk.tool-windows.contents-tabs
title: Tool Windows: Contents (Tabs)
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, contents, tabs]
---
Displaying the contents of many tool windows requires access to [indexes](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html).
Because of that, tool windows are disabled by default while building indexes unless the `ToolWindowFactory` is marked as [dumb aware](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html#DumbAwareAPI).

As mentioned previously, tool windows can contain multiple contents (tabs).
To manage the contents of a tool window, call [ToolWindow.getContentManager()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/wm/ToolWindow.java).
To add a [Content](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/ui/content/Content.java) (tab), first create it by calling `ContentManager.getFactory().createContent()`,
and then to add it to the tool window using `ContentManager.addContent()`.
Set the preferred focus component via `Content.setPreferredFocusableComponent()`.
Use `Content.setDisposer()` to register an associated `Disposable` (see [Disposer and Disposable](https://plugins.jetbrains.com/docs/intellij/disposers.html)).

See [SimpleToolWindowPanel](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/ui/SimpleToolWindowPanel.java) as a convenient base class,
supporting [Toolbars](https://plugins.jetbrains.com/docs/intellij/action-system.html#buildingToolbarPopupMenu) and a vertical or horizontal layout.

### Closing Tabs (tool-windows/contents-tabs/closing-tabs.md)
