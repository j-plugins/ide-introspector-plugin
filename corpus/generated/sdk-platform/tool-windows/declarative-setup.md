---
id: sdk.tool-windows.declarative-setup
title: Tool Windows: Declarative Setup
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, declarative, setup]
---
The tool window is registered in `[plugin.xml](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html)` using the [com.intellij.toolWindow](https://jb.gg/ipe?extensions=com.intellij.toolWindow) extension point
.
The extension point attributes specify all the data which is necessary to display the tool window button:

* The `id` attribute (required) of the tool window which corresponds to the text displayed on the tool window button. To provide a localized text, specify matching `toolwindow.stripe.[id]` message key (escape spaces with `_`) in the [resource bundle](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__resource-bundle).

* The `icon` to display on the tool window button (see [Tool Window](https://plugins.jetbrains.com/docs/intellij/tool-window.html) in UI Guidelines and [Working with Icons](https://plugins.jetbrains.com/docs/intellij/icons.html))

* The `anchor`, meaning the side of the screen on which the tool window is displayed ("left" (default), "right" or "bottom")

* The `secondary` attribute, specifying whether the tool window is displayed in the primary or the secondary group

* The `factoryClass` attribute (required), a class implementing [ToolWindowFactory](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/wm/ToolWindowFactory.kt).

When the user clicks on the tool window button, the `createToolWindowContent()` method of the factory class is called and initializes the UI of the tool window.
This procedure ensures that unused tool windows don't cause any overhead in startup time or memory usage: if a user does not interact with the tool window, no plugin code will be loaded or executed.

### Conditional Display (tool-windows/declarative-setup/conditional-display.md)
