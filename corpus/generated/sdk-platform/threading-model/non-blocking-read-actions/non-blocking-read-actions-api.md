---
id: sdk.threading-model.non-blocking-read-actions.non-blocking-read-actions-api
title: Threading Model: Non-Blocking Read Actions API
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, non, blocking, read, actions, api]
---
Warning:

Plugins targeting versions 2024.1+ should use Write Allowing Read Actions available in the [Kotlin Coroutines Read Actions API](https://plugins.jetbrains.com/docs/intellij/coroutine-read-actions.html#coroutine-read-actions-api).

To run a non-blocking read action, use one of the available APIs:

* [ReadAction.nonBlocking()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/ReadAction.java) which returns [NonBlockingReadAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/NonBlockingReadAction.java) (NBRA). NBRA handles restarting the action out-of-the-box.

* [ReadAction.computeCancellable()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/ReadAction.java) which computes the result immediately in the current thread or throws an exception if there is a running or requested write action.

In both cases, when a read action is started and a write action occurs in the meantime, the read action is marked as canceled.
Read actions must [check for cancellation](https://plugins.jetbrains.com/docs/intellij/background-processes.html#handling-cancellation) often enough to trigger actual cancellation.
Although the cancellation mechanism may differ under the hood ([Progress API](https://plugins.jetbrains.com/docs/intellij/background-processes.html#progress-api) or [Kotlin Coroutines](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html)), the cancellation handling rules are the same in both cases.

Always check at the start of each read action if the [objects are still valid](#objects-validity) and if the whole operation still makes sense.
With `ReadAction.nonBlocking()`, use `expireWith()` or `expireWhen()` for that.

Note:

If NBRA needs to access a [file-based index](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html) (for example, it is doing any project-wide PSI analysis, resolves references, or performs other tasks depending on indexes), use `ReadAction.nonBlocking(…).inSmartMode()`.

