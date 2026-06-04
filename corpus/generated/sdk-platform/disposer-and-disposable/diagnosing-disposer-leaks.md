---
id: sdk.disposer-and-disposable.diagnosing-disposer-leaks
title: Disposer and Disposable: Diagnosing `Disposer` Leaks
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, diagnosing, disposer, leaks]
---
Part of `sdk.disposer-and-disposable`.

Diagnosing `Disposer` Leaks

When the application exits, it performs a final sanity check to verify everything was disposed.
If something was registered with the `Disposer` but remains undisposed, the IntelliJ Platform reports it before shutting down.

In test, [internal](https://plugins.jetbrains.com/docs/intellij/enabling-internal.html), and debug mode (add `idea.disposer.debug=on` in `Help | Edit Custom Properties...`), registering a `Disposable` with the `Disposer` also registers a stack trace for the object's allocation path.
The `Disposer` does this by creating a `Throwable` at the time of registration.

The following snippet represents the sort of "memory leak detected" error encountered in practice:

```TEXT
java.lang.RuntimeException:
Memory leak detected: <instance> of class com.example.ClassType
See the cause for the corresponding Disposer.register() stacktrace:
    at ObjectTree.assertIsEmpty(ObjectTree.java:209)
    at Disposer.assertIsEmpty(Disposer.java:125)
    at Disposer.assertIsEmpty(Disposer.java:121)
    at ApplicationImpl.disposeSelf(ApplicationImpl.java:323)
    at ApplicationImpl.doExit(ApplicationImpl.java:780)
    …
Caused by: java.lang.Throwable
    at ObjectTree.getOrCreateNodeFor(ObjectTree.java:101)
    at ObjectTree.register(ObjectTree.java:62)
    at Disposer.register(Disposer.java:81)
    at Disposer.register(Disposer.java:75)
    …
    at ProjectManagerEx.createProject(ProjectManagerEx.java:69)
    at NewProjectWizardDynamic.doFinish(NewProjectWizardDynamic.java:235)
    at DynamicWizard$1.run(DynamicWizard.java:433)
    at CoreProgressManager$5.run(CoreProgressManager.java:237)
    at CoreProgressManager$TaskRunnable.run(CoreProgressManager.java:563)
    …
```

Tip:

The first part of the callstack is unrelated to diagnosing the memory leak.
Instead, pay attention to the second part of the call stack, after `Caused by: java.lang.Throwable`.

In this specific case, the IntelliJ Platform ([CoreProgressManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-impl/src/com/intellij/openapi/progress/impl/CoreProgressManager.java)) started a task that contained the `DynamicWizard` code.
In turn, that code allocated a `Project` that was never disposed by the time the application exited.
That is a promising place to start digging.

The above memory leak was ultimately caused by failing to pass a `Project` instance to a function responsible for registering it for disposal.
Often the fix for a memory leak is as simple as understanding the memory scope of the object being allocated - usually a UI container, project, or application - and making sure a `Disposer.register()` call is made appropriately for it.

> Source: IntelliJ Platform SDK docs — Disposer and Disposable: Diagnosing `Disposer` Leaks (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
