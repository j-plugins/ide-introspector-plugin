---
id: sdk.coroutine-dispatchers.edt-dispatcher
title: Coroutine Dispatchers: EDT Dispatcher
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, edt, dispatcher]
---
The [Dispatchers.EDT](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/coroutines.kt) dispatcher is used for executing UI actions on the Swing Event Dispatch Thread.
`Dispatchers.EDT` dispatches onto EDT within the context [modality state](https://plugins.jetbrains.com/docs/intellij/threading-model.html#invoking-operations-on-edt-and-modality).

### Dispatchers.Main` vs. `Dispatchers.EDT (coroutine-dispatchers/edt-dispatcher/dispatchers-main-vs-dispatchers-edt.md)
