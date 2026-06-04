---
id: sdk.coroutine-scopes.using-a-correct-scope.do-not-use-application-project-scope
title: Coroutine Scopes: Do Not Use Application/Project Scope
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, not, use, application, project, scope]
---
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

