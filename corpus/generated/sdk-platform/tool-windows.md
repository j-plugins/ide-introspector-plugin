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

## Subtopics

- Declarative Setup — `sdk.tool-windows.declarative-setup`
- Programmatic Setup — `sdk.tool-windows.programmatic-setup`
- Contents (Tabs) — `sdk.tool-windows.contents-tabs`
- Tool Window FAQ — `sdk.tool-windows.tool-window-faq`
- Sample Plugin — `sdk.tool-windows.sample-plugin`
- Testing — `sdk.tool-windows.testing`

> Source: IntelliJ Platform SDK docs — Tool Windows (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
