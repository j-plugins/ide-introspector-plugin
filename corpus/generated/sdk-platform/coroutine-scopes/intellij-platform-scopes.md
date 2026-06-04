---
id: sdk.coroutine-scopes.intellij-platform-scopes
title: Coroutine Scopes: IntelliJ Platform Scopes
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, intellij, platform, scopes]
---
IntelliJ Platform provides special coroutine scopes that help ensure proper structured concurrency of coroutines run from the platform or plugin code.
After cancellation, the platform awaits the completion of each scope.
Using correct parent scopes guarantees that child coroutines will be properly canceled when no longer needed, preventing resource leaks.

The following diagram presents the scopes and their parent-child relationships:

![IntelliJ Platform Coroutine Scopes](images/intellij_platform_coroutine_scopes.svg)
All scopes presented on the diagram are [supervisor scopes](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/supervisor-scope.html) — they ignore the failures of their children.

Each coroutine scope can have only one actual parent, pointed with solid arrow lines.
Dashed arrow lines point to fictional parents, which follow the actual coroutine parent-child semantics:

* a parent scope cancels children on its own cancellation

* a parent scope awaits children before considering itself complete

* a failed child cancels its parent (which effectively is not happening because presented scopes are supervisors)

The Application×Plugin and Project×Plugin are [intersection scopes](#intersection-scopes) with two semantic parents (actual and fictional).

### Main Scopes (coroutine-scopes/intellij-platform-scopes/main-scopes.md)
### Intersection Scopes (coroutine-scopes/intellij-platform-scopes/intersection-scopes.md)
### Service Scopes (coroutine-scopes/intellij-platform-scopes/service-scopes.md)
