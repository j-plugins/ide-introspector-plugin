---
id: sdk.coroutine-dumps.coroutine-dump-format.coroutineclass-jobstate
title: Coroutine Dumps: CoroutineClass{JobState}
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, coroutineclass, jobstate]
---
`CoroutineClass{JobState}`

A coroutine's `toString()`:

* `CoroutineClass` - a coroutine class. Notable values: * `StandaloneCoroutine` and `LazyStandaloneCoroutine` are created by [launch](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/launch.html). * `DeferredCoroutine` and `LazyDeferredCoroutine` are created by [async](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/async.html). * `BlockingCoroutine` is created by [runBlockingCancellable()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/progress/coroutines.kt). * `ProducerCoroutine` is created by [produce](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/produce.html). * `ChildScope` is created by [childScope](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/coroutines/src/coroutineScope.kt).

* `JobState` - a coroutine `Job`'s state. Possible states and transition can be found in the [Job's KDoc](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-job/).

