---
id: sdk.coroutine-dispatchers.dispatchers-vs-threads
title: Coroutine Dispatchers: Dispatchers vs. Threads
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, dispatchers, threads]
---
Part of `sdk.coroutine-dispatchers`.

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

> Source: IntelliJ Platform SDK docs — Coroutine Dispatchers: Dispatchers vs. Threads (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
