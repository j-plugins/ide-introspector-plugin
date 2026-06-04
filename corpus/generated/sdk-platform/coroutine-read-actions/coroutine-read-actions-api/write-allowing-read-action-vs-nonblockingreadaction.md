---
id: sdk.coroutine-read-actions.coroutine-read-actions-api.write-allowing-read-action-vs-nonblockingreadaction
title: Coroutine Read Actions: Write Allowing Read Action vs. NonBlockingReadAction
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, write, allowing, read, action, nonblockingreadaction]
---
WARA API is simpler than [NonBlockingReadAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/NonBlockingReadAction.java) (NBRA).
WARA doesn't need the following API methods:

* `submit(Executor backgroundThreadExecutor)` because this is a responsibility of the coroutine dispatcher

* `executeSynchronously()` because effectively they're executed in the current coroutine dispatcher already

* `expireWhen(BooleanSupplier expireCondition)`, `expireWith(Disposable parentDisposable)`, and `wrapProgress(ProgressIndicator progressIndicator)` because they're canceled when the calling coroutine is canceled

* `finishOnUiThread()` because this is handled by switching to the [EDT dispatcher](https://plugins.jetbrains.com/docs/intellij/coroutine-dispatchers.html#edt-dispatcher). Note that the UI data must be pure (for example, strings/icons/element pointers), which inherently can't be invalidated during the transfer from a background thread to EDT. In the case of using NBRA's `finishOnUiThread` to start a write action, the coroutine equivalent is `readAndWriteAction`: ```KOTLIN readAndWriteAction { val computedData = computeDataInReadAction() writeAction { applyData(computedData) } } ``` It provides the same guarantees as `finishOnUIThread` (no WA between `computeDataInReadAction` and `applyData`), but it is not bound to EDT.

* `coalesceBy(Object ... equality)` because this should be handled by [Flow.collectLatest()](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/collect-latest.html) and/or [Flow.distinctUntilChanged()](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/distinct-until-changed.html). Usually, NBRAs are run as a reaction to user actions, and there might be multiple NBRAs running, even if their results are unused. Instead of cancelling the read action, in the coroutine world the coroutines are canceled: ```KOTLIN eventFlow.collectLatest { event -> // the next emitted event will cancel the current coroutine // and run it again with the next event readAction { readData() } } eventFlow.distinctUntilChanged().collectLatest { event -> // the next emitted event will cancel the current coroutine // and run it again with the next event if the next event // wasn't equal to the previous one readAction { readData() } } ```

#### Read Action Cancellability (coroutine-read-actions/coroutine-read-actions-api/write-allowing-read-action-vs-nonblockingreadaction/read-action-cancellability.md)
