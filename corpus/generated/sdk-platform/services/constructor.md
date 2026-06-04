---
id: sdk.services.constructor
title: Services: Constructor
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, constructor]
---
Part of `sdk.services`.

To improve startup performance, avoid any heavy initializations in the constructor.

Project/Module-level service constructors can have a [Project](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/Project.java)/[Module](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/module/Module.java) argument.

Warning: Do not use Constructor Injection

Using constructor injection of dependency services is deprecated (and not supported in [Light Services](#light-services)) for performance reasons.

Other service dependencies must be [acquired only when needed](#retrieving-a-service) in all corresponding methods, e.g., if you need a service to get some data or execute a task, retrieve the service before calling its methods.
Do not retrieve services in constructors to store them in class fields.

Use inspection Plugin DevKit | Code | Non-default constructors for service and extension class to verify code.

### Kotlin Coroutines

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

> Source: IntelliJ Platform SDK docs — Services: Constructor (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
