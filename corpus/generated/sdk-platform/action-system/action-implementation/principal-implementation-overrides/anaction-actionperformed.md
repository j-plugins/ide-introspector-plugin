---
id: sdk.action-system.action-implementation.principal-implementation-overrides.anaction-actionperformed
title: Action System: AnAction.actionPerformed()
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, anaction, actionperformed]
---
`AnAction.actionPerformed()`

An action's method `AnAction.actionPerformed()` is called by the IntelliJ Platform if available and selected by the user.
This method does the heavy lifting for the action: it contains the code executed when the action gets invoked.
The `actionPerformed()` method also receives `AnActionEvent` as a parameter, which is used to access any context data like projects, files, selection, and similar.
See [Overriding the AnAction.actionPerformed() Method](#overriding-the-anactionactionperformed-method) for more information.

