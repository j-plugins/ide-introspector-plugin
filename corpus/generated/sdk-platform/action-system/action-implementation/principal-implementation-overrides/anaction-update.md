---
id: sdk.action-system.action-implementation.principal-implementation-overrides.anaction-update
title: Action System: AnAction.update()
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, anaction, update]
---
`AnAction.update()`

An action's method `AnAction.update()` is called by the IntelliJ Platform framework to update an action state.
The state (enabled, visible) of an action determines whether the action is available in the UI.
An object of the [AnActionEvent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnActionEvent.java) type is passed to this method and contains information about the current context for the action.

Actions are made available by changing the state in the [Presentation](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/Presentation.java) object associated with the event context.
As explained in [Overriding the AnAction.update() Method](#overriding-the-anactionupdate-method), it is vital `update()` methods execute quickly and return execution to platform.

