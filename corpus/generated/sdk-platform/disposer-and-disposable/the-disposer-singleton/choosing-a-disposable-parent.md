---
id: sdk.disposer-and-disposable.the-disposer-singleton.choosing-a-disposable-parent
title: Disposer and Disposable: Choosing a Disposable Parent
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, choosing, disposable, parent]
---
Part of `sdk.disposer-and-disposable.the-disposer-singleton`.

To register a child `Disposable`, a parent `Disposable` of a suitable lifetime is used to establish the parent-child relationship.
One of the parent `Disposables` provided by the IntelliJ Platform can be chosen, or it can be another `Disposable`.

Use the following guidelines to choose the correct parent:

* For resources required for a plugin's entire lifetime, use an existing or a dedicated application or project-level [service](https://plugins.jetbrains.com/docs/intellij/plugin-services.html). Example: [PythonPluginDisposable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/python/openapi/src/com/jetbrains/python/PythonPluginDisposable.java).

* For resources required while a [dialog](https://plugins.jetbrains.com/docs/intellij/dialog-wrapper.html) is displayed, use `DialogWrapper.getDisposable()`.

* For resources required while a [tool window](https://plugins.jetbrains.com/docs/intellij/tool-windows.html) tab is displayed, pass your instance implementing `Disposable` to `Content.setDisposer()`.

* For resources with a shorter lifetime, create a disposable using `Disposer.newDisposable()` and dispose it manually using `Disposable.dispose()`. Note that it's always best to specify a parent for such a disposable (for example, a project-level service), so that there is no memory leak if the `Disposable.dispose()` call is not reached because of an exception or a programming error.

Warning: Plugin disposable leaks

Even though `Application` and `Project` implement `Disposable`, they must never be used as parent disposables in plugin code.
Disposables registered using those objects as parents will not be disposed when the plugin is unloaded, leading to memory leaks.

Consider a case of a disposable resource created by a plugin and registered with a project as its parent.
The following lifetime diagram shows that the resource will outlive the plugin and live as long as the project.

```MERMAID
%%{init: {'theme': 'base', 'themeVariables': { 'primaryBorderColor': 'green', 'background': 'yellow'}}}%%
gantt
    dateFormat X
    %% do not remove trailing space in axisFormat
    axisFormat ‎
    section Lifetimes
        Project         : 0, 10
        Plugin          : 2, 5
        Plugin Resource : crit, 3, 10
```

If the resource used, for example, a plugin's project-level service (if shorter living parents are possible, prefer them), the resource would be disposed together with the plugin:

```MERMAID
gantt
    dateFormat X
    %% do not remove trailing space in axisFormat
    axisFormat ‎
    section Lifetimes
        Project         : 0, 10
        Plugin          : 2, 5
        Plugin Resource : 3, 5
```

Inspection Plugin DevKit | Code | Incorrect parentDisposable parameter will highlight such problems.

The `Disposer` API flexibility means that if the parent instance is chosen unwisely, the child may consume resources for longer than required.
Continuing to use resources when they are no longer needed can be a severe source of contention due to leaving some zombie objects behind due to each invocation.
An additional challenge is that these kinds of issues won't be reported by the regular leak checker utilities, because technically, it's not a memory leak from the test suite perspective.

For example, suppose a UI component created for a specific operation uses a project-level service as a parent disposable.
In that case, the entire component will remain in memory after the operation is complete.
This creates memory pressure and can waste CPU cycles on processing events that are no longer relevant.

> Source: IntelliJ Platform SDK docs — Disposer and Disposable: Choosing a Disposable Parent (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
