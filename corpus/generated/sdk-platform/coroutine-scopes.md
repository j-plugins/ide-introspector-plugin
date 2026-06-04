---
id: sdk.coroutine-scopes
title: Coroutine Scopes
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, coroutine, scopes]
---
Tip: Kotlin Coroutines×IntelliJ Platform

This section focuses on explaining coroutines in the specific context of the [IntelliJ Platform](https://plugins.jetbrains.com/docs/intellij/intellij-platform.html).
If you are not experienced with Kotlin Coroutines, it is highly recommended to get familiar with
[Learning Resources](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html#learning-resources) first.

Kotlin's coroutines follow the principle of structured concurrency.
It means that each coroutine is run in a specific [CoroutineScope](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/), which delimits the lifetime of the coroutine.
This ensures that they are not lost and do not leak.
An outer scope does not complete until all its child coroutines are completed.
Cancellation of the outer scope also cancels its child coroutines.
Structured concurrency ensures that any errors in the code are properly reported and never lost.

## IntelliJ Platform Scopes (coroutine-scopes/intellij-platform-scopes.md)
### Main Scopes (coroutine-scopes/intellij-platform-scopes/main-scopes.md)
### Intersection Scopes (coroutine-scopes/intellij-platform-scopes/intersection-scopes.md)
### Service Scopes (coroutine-scopes/intellij-platform-scopes/service-scopes.md)
## Using a Correct Scope (coroutine-scopes/using-a-correct-scope.md)
### Use Service Scopes (coroutine-scopes/using-a-correct-scope/use-service-scopes.md)
### Do Not Use Application/Project Scope (coroutine-scopes/using-a-correct-scope/do-not-use-application-project-scope.md)
### Do Not Use Intersection Scopes (coroutine-scopes/using-a-correct-scope/do-not-use-intersection-scopes.md)

> Source: IntelliJ Platform SDK docs — Coroutine Scopes (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
