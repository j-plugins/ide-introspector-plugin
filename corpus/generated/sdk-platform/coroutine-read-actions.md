---
id: sdk.coroutine-read-actions
title: Coroutine Read Actions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, coroutine, read, actions]
---
Tip: Kotlin Coroutines×IntelliJ Platform

This section focuses on explaining coroutines in the specific context of the [IntelliJ Platform](https://plugins.jetbrains.com/docs/intellij/intellij-platform.html).
If you are not experienced with Kotlin Coroutines, it is highly recommended to get familiar with
[Learning Resources](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html#learning-resources) first.

The concept of read/write locks and running blocking and non-blocking read actions is explained in
the Threading section:

* [Read-Write Lock](https://plugins.jetbrains.com/docs/intellij/threading-model.html#read-write-lock)

* [Non-Blocking Read Actions](https://plugins.jetbrains.com/docs/intellij/threading-model.html#non-blocking-read-actions)

This section explains running read actions (RA) in coroutines specifically.

## Coroutine Read Actions API

Running RA from coroutines is executed with `*ReadAction*` functions from
[coroutines.kt](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/coroutines.kt)
(see their KDocs for the details).
Functions can be divided into two groups, which differ in reacting to an incoming write action (WA):

| Write Allowing Read Action (WARA) |Write Blocking Read Action (WBRA) |
------------------------------------------------------------------------
| `readAction` |`readActionBlocking` |
| `smartReadAction` |`smartReadActionBlocking` |
| `constrainedReadAction` |`constrainedReadActionBlocking` |

WARA is canceled when a parent coroutine is canceled or a WA arrives.

WBRA is canceled only when a parent coroutine is canceled.
It blocks WA until finishing its lambda.

Warning: Naming Convention

It is important to note that in the coroutines context, default functions
(without the `Blocking` suffix) behavior prioritizes WA.

In contrast, in the non-coroutine context,
[Application.runReadAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java)
and similar methods (without any prefix/suffix) perform RA blocking WA, whereas RA allowing WA are invoked via
the [NonBlockingReadAction API](https://plugins.jetbrains.com/docs/intellij/threading-model.html#non-blocking-read-actions).

Be careful when migrating the code running read actions to coroutines.

### Write Allowing Read Action vs. NonBlockingReadAction

WARA API is simpler than [NonBlockingReadAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/NonBlockingReadAction.java) (NBRA).
WARA doesn't need the following API methods:

* `submit(Executor backgroundThreadExecutor)` because this is a responsibility of the coroutine dispatcher

* `executeSynchronously()` because effectively they're executed in the current coroutine dispatcher already

* `expireWhen(BooleanSupplier expireCondition)`, `expireWith(Disposable parentDisposable)`, and `wrapProgress(ProgressIndicator progressIndicator)` because they're canceled when the calling coroutine is canceled

* `finishOnUiThread()` because this is handled by switching to the [EDT dispatcher](https://plugins.jetbrains.com/docs/intellij/coroutine-dispatchers.html#edt-dispatcher). Note that the UI data must be pure (for example, strings/icons/element pointers), which inherently can't be invalidated during the transfer from a background thread to EDT. In the case of using NBRA's `finishOnUiThread` to start a write action, the coroutine equivalent is `readAndWriteAction`: ```KOTLIN readAndWriteAction { val computedData = computeDataInReadAction() writeAction { applyData(computedData) } } ``` It provides the same guarantees as `finishOnUIThread` (no WA between `computeDataInReadAction` and `applyData`), but it is not bound to EDT.

* `coalesceBy(Object ... equality)` because this should be handled by [Flow.collectLatest()](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/collect-latest.html) and/or [Flow.distinctUntilChanged()](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/distinct-until-changed.html). Usually, NBRAs are run as a reaction to user actions, and there might be multiple NBRAs running, even if their results are unused. Instead of cancelling the read action, in the coroutine world the coroutines are canceled: ```KOTLIN eventFlow.collectLatest { event -> // the next emitted event will cancel the current coroutine // and run it again with the next event readAction { readData() } } eventFlow.distinctUntilChanged().collectLatest { event -> // the next emitted event will cancel the current coroutine // and run it again with the next event if the next event // wasn't equal to the previous one readAction { readData() } } ```

#### Read Action Cancellability

Suspending read actions use coroutines as the underlying framework.

WARA (invoked with [mentioned *ReadAction functions](#coroutine-read-actions-api))
may make several attempts to execute its lambda.
The block needs to know whether the current attempt was canceled.
`*ReadAction` functions create a child
[Job](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-job/)
for each attempt, and this job becomes canceled when a write action arrives.
`*ReadAction` restarts the block if it was canceled by a write action, or throws
`CancellationException` if the calling coroutine was canceled, causing the cancellation
of the child `Job`.

To check whether the current action was canceled, clients must call [ProgressManager.checkCanceled()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/progress/ProgressManager.java), which was adjusted to work in coroutines.
Clients mustn't throw [ProcessCanceledException](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/base/multiplatform/src/com/intellij/openapi/progress/ProcessCanceledException.kt) manually.

## FAQ

### Why can't I suspend inside the block?

Read actions must be short.
Technically, it is possible to allow suspension during the read action, but it is complex to implement,
and it still might be surprising:

```KOTLIN
readAction {
  withContext(IO) {
    // this will be canceled and restarted on each write action
    loadTenGigabytesOfIndexes()
  }
}
```

Also, it is impossible to solve this with a continuation interceptor like:

```KOTLIN
object ReadAction : ContinuationInterceptor, CoroutineContext.Key<RA> {
  override val key: CoroutineContext.Key<*> get() = this
  override fun <T> interceptContinuation(
    continuation: Continuation<T>
  ): Continuation<T> {
    return Continuation(continuation.context) { result ->
      ApplicationManager.getApplication().runReadAction {
        continuation.resumeWith(result)
      }
    }
  }
}
```

It is impossible to give it suspending semantics: the interceptor will block its thread waiting for
the read lock.
The interceptors shouldn't be used for that.

As of Kotlin 1.8.x, it is not possible to combine interceptors and dispatchers.
Only one of them can exist in the context:

```KOTLIN
withContext(ReadAction) {
  foo()
  withContext(Dispatchers.Default) { // replaces ReadAction in the context
    bar() // this will be called outside read action
  }
}
```

Even if that wasn't the case, the following code will work unexpectedly:

```KOTLIN
withContext(ReadAction) {
  val foo = foo()
  yield() // or another function which will suspend

  // At this point 'foo' crossed the boundary between two read actions =>
  // 'foo' might be invalidated if there was a write action in between.
  bar(foo)
}
```

Note: Something missing?

If a topic is not covered in the above sections,
let us know via the Feedback widget displayed on the right,
or [other channels](https://plugins.jetbrains.com/docs/intellij/getting-help.html#problems-with-the-guide).

Be specific about the topics and reasons for adding them and leave your email in case we need
more details. Thanks for your feedback!

> Source: IntelliJ Platform SDK docs — Coroutine Read Actions (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
