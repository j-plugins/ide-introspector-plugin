---
id: sdk.services.constructor.kotlin-coroutines
title: Services: Kotlin Coroutines
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, kotlin, coroutines]
---
When using [Kotlin Coroutines](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html), a distinct service [scope](https://plugins.jetbrains.com/docs/intellij/coroutine-scopes.html) can be injected as parameter.

The Application Service and Project Service scopes are bound to an application and project [service](#types) lifetimes accordingly.
They are children of the [Intersection Scopes](https://plugins.jetbrains.com/docs/intellij/coroutine-scopes.html#intersection-scopes), which means that they are canceled when the application/project is closed or a plugin is unloaded.

The service scope is provided to services via constructor injection.
The following constructor signatures are supported:

* `MyService(CoroutineScope)` for application and project services

* `MyProjectService(Project, CoroutineScope)` for project services

Each service instance receives its own scope instance.
The injected scopes' contexts contain [Dispatchers.Default](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-default.html) and [CoroutineName(serviceClass)](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-name/).

See [Launching Coroutine From Service Scope](https://plugins.jetbrains.com/docs/intellij/launching-coroutines.html#launching-coroutine-from-service-scope) for full samples.

