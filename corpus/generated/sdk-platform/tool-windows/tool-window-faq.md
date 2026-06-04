---
id: sdk.tool-windows.tool-window-faq
title: Tool Windows: Tool Window FAQ
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, tool, window, faq]
---
Part of `sdk.tool-windows`.

### Accessing Tool Window

Use [ToolWindowManager.getToolWindow()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/wm/ToolWindowManager.kt) specifying the `id` used for [registration](#declarative-setup).

### Tool Window Notification

Showing a balloon notification for a tool window can be done:

* [registering notification](https://plugins.jetbrains.com/docs/intellij/notification-balloons.html#tool-window-notifications) for a specific tool window

* by calling [ToolWindowManager.notifyByBalloon()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/wm/ToolWindowManager.kt)

### Events

Project-level topic [ToolWindowManagerListener](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/wm/ex/ToolWindowManagerListener.java) allows listening to tool window registration/show events (see [Listeners](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html)).

> Source: IntelliJ Platform SDK docs — Tool Windows: Tool Window FAQ (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
