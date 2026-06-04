---
id: sdk.coroutine-dispatchers
title: Coroutine Dispatchers
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, coroutine, dispatchers]
---
Tip: Kotlin Coroutines×IntelliJ Platform

This section focuses on explaining coroutines in the specific context of the [IntelliJ Platform](https://plugins.jetbrains.com/docs/intellij/intellij-platform.html).
If you are not experienced with Kotlin Coroutines, it is highly recommended to get familiar with
[Learning Resources](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html#learning-resources) first.

Coroutines are always executed in a [context](https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html) represented by [CoroutineContext](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines/-coroutine-context/).
One of the most important parts of the context is a dispatcher, which determines what thread or thread pool the corresponding coroutine is executed on.

In the IntelliJ Platform, coroutines are executed on three main dispatchers:

* [Default Dispatcher](#default-dispatcher)

* [IO Dispatcher](#io-dispatcher)

* [EDT Dispatcher](#edt-dispatcher)

## Default Dispatcher

The [Dispatchers.Default](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-default.html) dispatcher is used for performing CPU-bound tasks.

It ensures that the number of tasks running in parallel does not exceed the number of CPU cores.
A hundred threads performing CPU-bound work on a machine with 10 CPU cores can result in threads competing for CPU time and excessive thread switching.
This makes the IDE effectively slower, hence the limitation.
Using the default dispatcher (or its [limitedParallelism()](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-dispatcher/limited-parallelism.html) slice) enables a consistent CPU load.

## IO Dispatcher

The [Dispatchers.IO](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-i-o.html) dispatcher is used for performing IO operations like reading/writing to files, network, executing external processes, etc.

It must be used at the very deep moment in the trace right before the actual IO operation happens and exited as soon as the operation is finished.
Example:

Wrong:

```KOTLIN
suspend fun readDataFromFile(): Data {
  return withContext(Dispatchers.IO) {
    val fileName = computeFileName()
    val bytes = readFile(fileName)
    Data(parseBytes(bytes))
  }
}
```

Correct:

```KOTLIN
suspend fun readDataFromFile(): Data {
  val fileName = computeFileName()
  val bytes = withContext(Dispatchers.IO) {
    readFile(fileName)
  }
  return Data(parseBytes(bytes))
}
```

## EDT Dispatcher

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

## Dispatchers vs. Threads

The dispatcher concept is a higher level of abstraction over threads.
While the code is always executed on threads, do not think about dispatchers as specific thread instances.

A single coroutine is not bound to the same thread during the whole execution time.
It may happen that a coroutine starts on thread A, is suspended, and finished on thread B, even if the whole is executed with the same dispatcher context.

Consider the following code snippet:

```KOTLIN
suspend fun doSomething() {
  val fetchedData = suspendingTask()
  withContext(Dispatchers.EDT) {
    updateUI(fetchedData)
  }
}

suspend fun suspendingTask(): Data {
  // fetch data from the internet
}
```

The following diagram presents one of the potential execution scenarios:

```MERMAID
gantt
    dateFormat X
    %% do not remove trailing space in axisFormat
    axisFormat ‎
    section Thread 1
        suspendingTask() : 2, 3
    section Thread 2
        suspendingTask() : 0, 1
    section EDT
        updateUI() : 3, 4
```

The code is executed as follows:

1. `suspendingTask` is started and partially executed on Thread 2.

2. `suspendingTask` is suspended when it waits for data fetched from the internet.

3. After receiving data, `suspendingTask` is resumed, but now it is executed on Thread 1.

4. Execution explicitly switches to the EDT dispatcher and `updateUI` is executed on EDT.

Warning:

This behavior can result in unexpected consequences for code that relies on thread-specific data and assumes it will execute consistently on the same thread.

Note: Something missing?

If a topic is not covered in the above sections,
let us know via the Feedback widget displayed on the right,
or [other channels](https://plugins.jetbrains.com/docs/intellij/getting-help.html#problems-with-the-guide).

Be specific about the topics and reasons for adding them and leave your email in case we need
more details. Thanks for your feedback!

> Source: IntelliJ Platform SDK docs — Coroutine Dispatchers (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
