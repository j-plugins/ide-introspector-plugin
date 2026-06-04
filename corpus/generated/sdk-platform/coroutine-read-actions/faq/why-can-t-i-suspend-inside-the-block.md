---
id: sdk.coroutine-read-actions.faq.why-can-t-i-suspend-inside-the-block
title: Coroutine Read Actions: Why can't I suspend inside the block?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, why, can, suspend, inside, block]
---
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

