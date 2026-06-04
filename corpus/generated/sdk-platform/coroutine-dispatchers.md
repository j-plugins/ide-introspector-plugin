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

## Subtopics

- Default Dispatcher — `sdk.coroutine-dispatchers.default-dispatcher`
- IO Dispatcher — `sdk.coroutine-dispatchers.io-dispatcher`
- EDT Dispatcher — `sdk.coroutine-dispatchers.edt-dispatcher`
- Dispatchers vs. Threads — `sdk.coroutine-dispatchers.dispatchers-vs-threads`

> Source: IntelliJ Platform SDK docs — Coroutine Dispatchers (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
