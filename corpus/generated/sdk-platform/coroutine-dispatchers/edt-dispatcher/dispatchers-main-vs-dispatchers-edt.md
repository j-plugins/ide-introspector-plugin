---
id: sdk.coroutine-dispatchers.edt-dispatcher.dispatchers-main-vs-dispatchers-edt
title: Coroutine Dispatchers: Dispatchers.Main` vs. `Dispatchers.EDT
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, dispatchers, main, edt]
---
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

