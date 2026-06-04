---
id: sdk.coroutine-scopes.using-a-correct-scope
title: Coroutine Scopes: Using a Correct Scope
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, using, correct, scope]
---
Part of `sdk.coroutine-scopes`.

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

> Source: IntelliJ Platform SDK docs — Coroutine Scopes: Using a Correct Scope (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
