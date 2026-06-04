---
id: sdk.coroutine-scopes.intellij-platform-scopes
title: Coroutine Scopes: IntelliJ Platform Scopes
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, intellij, platform, scopes]
---
Part of `sdk.coroutine-scopes`.

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

### Main Scopes

* Root - the root scope spans all the coroutines. This is the standard root scope launched with [runBlocking](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/run-blocking.html) coroutine builder.

* Application - a scope associated with the [Application](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java) container (component manager) lifetime. It is canceled on application shutdown. This triggers cancellation of the Application×Plugin scope and, subsequently, its children, including the Project×Plugin scope.

* Project - a scope associated with a [Project](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/Project.java) container (component manager) lifetime. It is canceled when a project is being closed. This triggers the cancellation of the Project×Plugin scope and, subsequently, its children.

* Plugin - a scope associated with a plugin lifetime. It is canceled on unloading of the corresponding plugin. This triggers cancellation of the Application×Plugin scope and, subsequently, its children, including the Project×Plugin scope.

### Intersection Scopes

* Application×Plugin - a scope which is an intersection of the Application and Plugin scopes. It is canceled when the application is shutdown or the corresponding plugin is unloaded. This triggers the cancellation of its children and the Project×Plugin scope and, subsequently, its children.

* Project×Plugin - a scope which is an intersection of the Project and Plugin scopes. It is canceled when a project is being closed or the corresponding plugin is unloaded.

Intersection scopes enable creating coroutines whose lifetime is limited by application/project and plugin lifetimes, e.g.,
application/project [services](https://plugins.jetbrains.com/docs/intellij/plugin-services.html) provided by a plugin.

### Service Scopes

The Application Service and Project Service scopes are bound to an application and project [service](https://plugins.jetbrains.com/docs/intellij/plugin-services.html#types) lifetimes accordingly.
They are children of the [Intersection Scopes](#intersection-scopes), which means that they are canceled when the application/project is closed or a plugin is unloaded.

The service scope is provided to services via constructor injection.
The following constructor signatures are supported:

* `MyService(CoroutineScope)` for application and project services

* `MyProjectService(Project, CoroutineScope)` for project services

Each service instance receives its own scope instance.
The injected scopes' contexts contain [Dispatchers.Default](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-default.html) and [CoroutineName(serviceClass)](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-name/).

See [Launching Coroutine From Service Scope](https://plugins.jetbrains.com/docs/intellij/launching-coroutines.html#launching-coroutine-from-service-scope) for full samples.

> Source: IntelliJ Platform SDK docs — Coroutine Scopes: IntelliJ Platform Scopes (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
