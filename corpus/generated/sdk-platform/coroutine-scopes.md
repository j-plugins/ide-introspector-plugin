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

## IntelliJ Platform Scopes

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

## Using a Correct Scope

### Use Service Scopes

If a plugin requires running some code in a coroutine, the approach recommended in most cases is to create a separate [service](https://plugins.jetbrains.com/docs/intellij/plugin-services.html) that will receive its [own scope](#service-scopes) via constructor and launch the coroutine in this scope.
This approach guarantees the usage of the correct scope, preventing leaks and canceling wrong scopes and killing all their (e.g., application's or project's) coroutines accidentally.

Note:

Note that since 2024.2, `AnAction.actionPerformed()` logic can be executed in the [current thread coroutine scope](https://plugins.jetbrains.com/docs/intellij/launching-coroutines.html#using-currentthreadcoroutinescope).

See the [Launching Coroutines](https://plugins.jetbrains.com/docs/intellij/launching-coroutines.html) section for details.

Warning:

The following sections describe the potential problems that would occur if the wrong coroutine scopes were used.
This allows better understanding of the platform scopes and why the [service approach](#use-service-scopes) mentioned above must be used.

### Do Not Use Application/Project Scope

Application and Project scopes are exposed with `Application.getCoroutineScope()` and `Project.getCoroutineScope()`.
Never use these methods, as they are deprecated and will be removed in the future.

Using these scopes could easily lead to project or plugin class leaks.

1. Project leak:

```KOTLIN
application.coroutineScope.launch {
  project.getService(PsiDirectoryFactory::class.java)
}
```

Closing the project cancels its scope.
The application scope remains active, and the project is leaked.

```MERMAID
gantt
    dateFormat X
    %% do not remove trailing space in axisFormat
    axisFormat ‎
    section Lifetimes
        Application Scope : 0, 10
        Project Scope     : done, 2, 8
        Project leak      : crit, 4, 10
```

2. Plugin leak:

```KOTLIN
project.coroutineScope.launch {
  project.getService(MyPluginService::class.java)
}
```

Unloading of the plugin cancels its scope.
The project scope remains active, and the plugin classes are leaked.

```MERMAID
gantt
    dateFormat X
    %% do not remove trailing space in axisFormat
    axisFormat ‎
    section Lifetimes
        Project Scope        : 0, 10
        Plugin Scope         : done, 2, 8
        MyPluginService leak : crit, 4, 10
```

### Do Not Use Intersection Scopes

There is no API for retrieving Application×Plugin and Project×Plugin [intersection scopes](#intersection-scopes),
but let's assume there is a method exposing the Project×Plugin scope:

```KOTLIN
/**
 * Returns the correct intersection scope for the project and plugin
 * by a given plugin class.
 */
fun Project.getCoroutineScope(pluginClass: Class<*>): CoroutineScope
```

Using this scope could lead to a plugin leak:

```KOTLIN
project.getCoroutineScope(PluginBService::class.java).launch {
  project.getService(PluginAService::class.java)
}
```

Unloading of Plugin A cancels its scope.
The Project × Plugin B scope remains active, and the Plugin A classes are leaked.

```MERMAID
gantt
    dateFormat X
    %% do not remove trailing space in axisFormat
    axisFormat ‎
    section Lifetimes
        Application Scope                               : done, 0, 10
        Project Scope                                   : done, 2, 9
        Plugin A Scope                                  : active, done, 1, 6
        Project × Plugin A Scope                        : active, done, 2, 6
        Plugin B Scope                                  : 4, 10
        Project × Plugin B Scope                        : 4, 9
        PluginAService leak                             : crit, 5, 9
        Correct PluginAService lifetime should end here : active, milestone, 6, 6
```

Note: Something missing?

If a topic is not covered in the above sections,
let us know via the Feedback widget displayed on the right,
or [other channels](https://plugins.jetbrains.com/docs/intellij/getting-help.html#problems-with-the-guide).

Be specific about the topics and reasons for adding them and leave your email in case we need
more details. Thanks for your feedback!

> Source: IntelliJ Platform SDK docs — Coroutine Scopes (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
