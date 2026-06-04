---
id: sdk.tool-windows.programmatic-setup
title: Tool Windows: Programmatic Setup
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, programmatic, setup]
---
Part of `sdk.tool-windows`.

For tool windows shown only after invoking specific actions, use [ToolWindowManager.registerToolWindow(String, RegisterToolWindowTaskBuilder)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/wm/ToolWindowManager.kt).

Always use [ToolWindowManager.invokeLater()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/wm/ToolWindowManager.kt) instead of "plain" `Application.invokeLater()` when scheduling EDT tasks related to tool windows (see [Threading Model](https://plugins.jetbrains.com/docs/intellij/threading-model.html)).

> Source: IntelliJ Platform SDK docs — Tool Windows: Programmatic Setup (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
