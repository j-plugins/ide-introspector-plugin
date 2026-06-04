---
id: sdk.action-system.action-implementation.overriding-the-anaction-actionperformed-method
title: Action System: Overriding the `AnAction.actionPerformed()` Method
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, overriding, anaction, actionperformed, method]
---
Part of `sdk.action-system.action-implementation`.

Overriding the `AnAction.actionPerformed()` Method

When the user selects an enabled action, be it from a menu or toolbar, the action's `AnAction.actionPerformed()` method is called.
This method contains the code executed to perform the action, and it is here that the real work gets done.

Warning: Reusable Logic

Reusable logic must not be exposed in the `AnAction` implementation via `static` methods (Java) or `companion object` (Kotlin).

Instead, introduce dedicated methods in utility classes or [Services](https://plugins.jetbrains.com/docs/intellij/plugin-services.html).

By using the `AnActionEvent` methods and `CommonDataKeys`, objects such as the `Project`, `Editor`, `PsiFile`, and other information is available.
For example, the `actionPerformed()` method can modify, remove, or add PSI elements to a file open in the editor.

The code that executes in the `AnAction.actionPerformed()` method should execute efficiently, but it does not have to meet the same stringent requirements as the `update()` method.

An example of inspecting PSI elements is demonstrated in the `action_basics` SDK code sample in [PopupDialogAction.actionPerformed()](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/action_basics/src/main/java/org/intellij/sdk/action/PopupDialogAction.java).

> Source: IntelliJ Platform SDK docs — Action System: Overriding the `AnAction.actionPerformed()` Method (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
