---
id: sdk.action-system.executing-actions-programmatically
title: Action System: Executing Actions Programmatically
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, executing, actions, programmatically]
---
Sometimes, it is required to execute actions programmatically, for example, executing an action implementing logic needed in another place, and the implementation is out of our control.
Executing actions can be achieved with [ActionUtils.invokeAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/actionSystem/ex/ActionUtil.kt).

Warning:

Executing actions programmatically should be avoided whenever possible.
If an action executed programmatically is under your control, extract its logic to a [service](https://plugins.jetbrains.com/docs/intellij/plugin-services.html) or utility class and call it directly, without the action execution context.

