# Tool Windows

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

## Declarative Setup (sdk.tool-windows.declarative-setup)
## Programmatic Setup

For tool windows shown only after invoking specific actions, use [ToolWindowManager.registerToolWindow(String, RegisterToolWindowTaskBuilder)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/wm/ToolWindowManager.kt).

Always use [ToolWindowManager.invokeLater()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/wm/ToolWindowManager.kt) instead of "plain" `Application.invokeLater()` when scheduling EDT tasks related to tool windows (see [Threading Model](https://plugins.jetbrains.com/docs/intellij/threading-model.html)).

## Contents (Tabs) (sdk.tool-windows.contents-tabs)
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
