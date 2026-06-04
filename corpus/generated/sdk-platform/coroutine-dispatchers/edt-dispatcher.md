---
id: sdk.coroutine-dispatchers.edt-dispatcher
title: Coroutine Dispatchers: EDT Dispatcher
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, edt, dispatcher]
---
Part of `sdk.coroutine-dispatchers`.

The [Dispatchers.EDT](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/coroutines.kt) dispatcher is used for executing UI actions on the Swing Event Dispatch Thread.
`Dispatchers.EDT` dispatches onto EDT within the context [modality state](https://plugins.jetbrains.com/docs/intellij/threading-model.html#invoking-operations-on-edt-and-modality).

### 

`Dispatchers.Main` vs. `Dispatchers.EDT`

In Kotlin, a standard dispatcher for UI-based activities is [Dispatchers.Main](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-main.html).

2025.1+:

In the IntelliJ Platform, `Dispatchers.Main` differs from `Dispatchers.EDT`:

1. It is forbidden to initiate read or write actions in `Dispatchers.Main`.
`Dispatchers.Main` is a pure UI dispatcher, and accidental access to the IntelliJ Platform model could cause UI freezes.

2. `Dispatchers.Main` uses `any` [modality state](https://plugins.jetbrains.com/docs/intellij/threading-model.html#invoking-operations-on-edt-and-modality) if it cannot infer modality from the coroutine context.
This helps to ensure progress guarantees in libraries that use `Dispatchers.Main`.

Earlier versions:

In the IntelliJ Platform, the EDT dispatcher is also installed as `Dispatchers.Main` so both can be used, however always prefer `Dispatchers.EDT`.

Use `Dispatchers.Main` only if the coroutine is IntelliJ Platform-context agnostic (e.g., when it can be executed outside the IntelliJ Platform context).
Use `Dispatchers.EDT` when in doubt.

> Source: IntelliJ Platform SDK docs — Coroutine Dispatchers: EDT Dispatcher (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
