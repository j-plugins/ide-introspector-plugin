---
id: sdk.action-system.registering-actions.registering-actions-from-code
title: Action System: Registering Actions from Code
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, registering, actions, from, code]
---
Part of `sdk.action-system.registering-actions`.

Two steps are required to register an action from code:

* First, an instance of the class derived from `AnAction` must be passed to the `registerAction()` method of [ActionManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/ActionManager.java), to associate the action with an ID.

* Second, the action needs to be added to one or more groups. To get an instance of an action group by ID, it is necessary to call `ActionManager.getAction()` and cast the returned value to [DefaultActionGroup](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/actionSystem/DefaultActionGroup.java).

> Source: IntelliJ Platform SDK docs — Action System: Registering Actions from Code (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
