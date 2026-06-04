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

## Coroutine Read Actions API (coroutine-read-actions/coroutine-read-actions-api.md)
### Write Allowing Read Action vs. NonBlockingReadAction (coroutine-read-actions/coroutine-read-actions-api/write-allowing-read-action-vs-nonblockingreadaction.md)
#### Read Action Cancellability (coroutine-read-actions/coroutine-read-actions-api/write-allowing-read-action-vs-nonblockingreadaction/read-action-cancellability.md)
## FAQ (coroutine-read-actions/faq.md)
### Why can't I suspend inside the block? (coroutine-read-actions/faq/why-can-t-i-suspend-inside-the-block.md)

> Source: IntelliJ Platform SDK docs — Coroutine Read Actions (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
