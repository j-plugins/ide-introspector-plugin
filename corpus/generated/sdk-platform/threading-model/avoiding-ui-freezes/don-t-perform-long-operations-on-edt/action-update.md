---
id: sdk.threading-model.avoiding-ui-freezes.don-t-perform-long-operations-on-edt.action-update
title: Threading Model: Action Update
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, action, update]
---
For implementations of [AnAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnAction.java), plugin authors should specifically
review the documentation of `AnAction.getActionUpdateThread()` in the [Action System](https://plugins.jetbrains.com/docs/intellij/action-system.html) section as it describes how threading works for actions.

