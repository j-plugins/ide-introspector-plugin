---
id: sdk.action-system.action-implementation.overriding-the-anaction-update-method.determining-the-action-context
title: Action System: Determining the Action Context
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, determining, action, context]
---
The `AnActionEvent` object passed to `update()` carries information about the current context for the action.
Context information is available from the methods of `AnActionEvent`, providing information such as the Presentation and whether the action is triggered by a Toolbar.
Additional context information is available using the method `AnActionEvent.getData()`.
Keys defined, for example, in [CommonDataKeys](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/CommonDataKeys.java) are passed to the `getData()` method to retrieve objects such as `Project`, `Editor`, `PsiFile`, and other information.
Accessing this information is relatively light-weight and is suited for `AnAction.update()`.

