---
id: sdk.action-system.action-implementation.principal-implementation-overrides.anaction-getactionupdatethread
title: Action System: AnAction.getActionUpdateThread()
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, anaction, getactionupdatethread]
---
`AnAction.getActionUpdateThread()`

`AnAction.getActionUpdateThread()` returns an [ActionUpdateThread](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/ActionUpdateThread.java),
which specifies if the `update()` method is called on a [background thread (BGT) or the event-dispatching thread (EDT)](https://plugins.jetbrains.com/docs/intellij/threading-model.html).
The preferred method is to run the update on the BGT, which has the advantage of guaranteeing application-wide read access to
[PSI](https://plugins.jetbrains.com/docs/intellij/psi.html), [the virtual file system](https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html) (VFS), or [project models](https://plugins.jetbrains.com/docs/intellij/project-model.html).
Actions that run the update session on the BGT should not access the Swing component hierarchy directly.
Conversely, actions that specify to run their update on EDT must not access PSI, VFS, or project data but have access to Swing components and other UI models.

All accessible data is provided by the `DataContext` as explained in [Determining the Action Context](#determining-the-action-context).
When switching from BGT to EDT is necessary, actions can use `AnActionEvent.getUpdateSession()` to
access the [UpdateSession](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/UpdateSession.java) and
then call `UpdateSession.compute()` to run a function on EDT.

Inspection `Plugin DevKit | Code | ActionUpdateThread is missing` highlights missing implementation of
`AnAction.getActionUpdateThread()`.

