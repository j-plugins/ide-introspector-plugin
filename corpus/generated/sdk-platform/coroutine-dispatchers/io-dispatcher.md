---
id: sdk.coroutine-dispatchers.io-dispatcher
title: Coroutine Dispatchers: IO Dispatcher
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, dispatcher]
---
Part of `sdk.coroutine-dispatchers`.

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

> Source: IntelliJ Platform SDK docs — Coroutine Dispatchers: IO Dispatcher (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
