---
id: sdk.coroutine-dispatchers.default-dispatcher
title: Coroutine Dispatchers: Default Dispatcher
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, default, dispatcher]
---
The [Dispatchers.Default](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-default.html) dispatcher is used for performing CPU-bound tasks.

It ensures that the number of tasks running in parallel does not exceed the number of CPU cores.
A hundred threads performing CPU-bound work on a machine with 10 CPU cores can result in threads competing for CPU time and excessive thread switching.
This makes the IDE effectively slower, hence the limitation.
Using the default dispatcher (or its [limitedParallelism()](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-dispatcher/limited-parallelism.html) slice) enables a consistent CPU load.

