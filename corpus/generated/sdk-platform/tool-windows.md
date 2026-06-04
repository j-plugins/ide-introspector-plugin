---
id: sdk.tool-windows
title: Tool Windows
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, tool, windows]
---
<tldr>
Product Help: [Tool windows](https://www.jetbrains.com/help/idea/tool-windows.html)

UI Guidelines: [Tool Window](https://plugins.jetbrains.com/docs/intellij/tool-window.html)
</tldr>

Tool windows are child windows of the IDE used to display information.
These windows generally have their own toolbars (referred to as tool window bars) along the outer edges of the main window.
These contain one or more tool window buttons, which activate panels displayed on the left, bottom, and right sides of the main IDE window.

Each side contains two tool window groups, the primary and the secondary one, and only one tool window from each group can be active at a time.

Each tool window can show multiple tabs (or "contents", as they are called in the API).
For example, the Run tool window displays a tab for each active run configuration, and the Version Control related tool windows display a fixed set of tabs depending on the version control system used in the project.

There are two main scenarios for the use of tool windows in a plugin.
Using [declarative setup](#declarative-setup), a tool window button is always visible, and the user can activate it and interact with the plugin functionality at any time.
Alternatively, using [programmatic setup](#programmatic-setup), the tool window is created to show the results of a specific operation and can then be closed after the operation is completed.

## Declarative Setup

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

### Conditional Display

If the tool window of a plugin should not be displayed for all projects, the plugin can provide a corresponding condition.

2023.3+:

Implement suspending `ToolWindowFactory.isApplicableAsync(Project)` in Kotlin.

Earlier versions:

Implement `ToolWindowFactory.isApplicable(Project)`.

Tip:

The condition is evaluated only once when the project is loaded.

To show and hide a tool window dynamically while the user is working with the project, use [programmatic setup](#programmatic-setup) for tool window registration.

## Programmatic Setup

For tool windows shown only after invoking specific actions, use [ToolWindowManager.registerToolWindow(String, RegisterToolWindowTaskBuilder)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/wm/ToolWindowManager.kt).

Always use [ToolWindowManager.invokeLater()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/wm/ToolWindowManager.kt) instead of "plain" `Application.invokeLater()` when scheduling EDT tasks related to tool windows (see [Threading Model](https://plugins.jetbrains.com/docs/intellij/threading-model.html)).

## Contents (Tabs)

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

### Closing Tabs

A plugin can control whether the user is allowed to close tabs either globally or on a per-content basis.
The former is done by passing the `canCloseContents` parameter to the `registerToolWindow()` function, or by specifying `canCloseContents="true"` in `plugin.xml`.
The default value is `false`; calling `setClosable(true)` on `ContentManager` content will be ignored unless `canCloseContents` is explicitly set.

If closing tabs is enabled in general, a plugin can disable closing of specific tabs by calling [Content.setCloseable(false)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/ui/content/Content.java).

## Tool Window FAQ

### Accessing Tool Window

Use [ToolWindowManager.getToolWindow()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/wm/ToolWindowManager.kt) specifying the `id` used for [registration](#declarative-setup).

### Tool Window Notification

Showing a balloon notification for a tool window can be done:

* [registering notification](https://plugins.jetbrains.com/docs/intellij/notification-balloons.html#tool-window-notifications) for a specific tool window

* by calling [ToolWindowManager.notifyByBalloon()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/wm/ToolWindowManager.kt)

### Events

Project-level topic [ToolWindowManagerListener](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/wm/ex/ToolWindowManagerListener.java) allows listening to tool window registration/show events (see [Listeners](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html)).

## Sample Plugin

To clarify how to develop plugins that create tool windows, consider the toolWindow sample plugin available in the [code samples](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/tool_window).

See [Code Samples](https://plugins.jetbrains.com/docs/intellij/code-samples.html) on how to set up and run the plugin.

This plugin creates the Sample Calendar tool window that displays the system date, time and time zone.
When opened, this tool window is similar to the following screen:

![Sample Calendar](images/sample_calendar.png)

## Testing

One of the testing approaches for tool windows is implementing [UI integration tests](https://plugins.jetbrains.com/docs/intellij/integration-tests-ui.html).

To get a tool window in UI tests, use [IdeaFrameUI.toolWindow()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/remote-driver/test-sdk/src/com/intellij/driver/sdk/ui/components/common/IdeaFrameUiExt.kt).

> Source: IntelliJ Platform SDK docs — Tool Windows (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
