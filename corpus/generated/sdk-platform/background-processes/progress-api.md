---
id: sdk.background-processes.progress-api
title: Background Processes: Progress API
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, progress, api]
---
Warning: Use Kotlin Coroutines

Plugins targeting 2024.1+ should use [Kotlin coroutines](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html), which is a more performant solution and provides the cancellation mechanism out of the box.

See [Execution Contexts](https://plugins.jetbrains.com/docs/intellij/execution-contexts.html) for coroutine-based APIs to use in different contexts.

The Progress API allows running processes on BGT with a modal (dialog), non-modal (visible in the status bar), or invisible progress.
It also allows for process cancellation and progress tracking (as a fraction of work done or textual).

The key classes are:

* [ProgressManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/progress/ProgressManager.java) – provides methods to execute and manage background processes

* [ProgressIndicator](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/progress/ProgressIndicator.java) – an object associated with a running process. It allows cancelling the process and optionally tracking its progress. The current thread's indicator can be retrieved any time via `ProgressManager.getProgressIndicator()`. There are many `ProgressIndicator` implementations and the most commonly used are: * [EmptyProgressIndicator](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/progress/EmptyProgressIndicator.java) – invisible (ignores text/fraction-related methods), used only for cancellation tracking. Remembers its creation [modality state](https://plugins.jetbrains.com/docs/intellij/threading-model.html#invoking-operations-on-edt-and-modality). * [ProgressIndicatorBase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-impl/src/com/intellij/openapi/progress/util/ProgressIndicatorBase.java) – invisible but can be made visible by subclassing. Stores text/fraction and allows retrieving them and possibly show in the UI. Non-modal by default. * [ProgressWindow](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-impl/src/com/intellij/openapi/progress/util/ProgressWindow.java) – visible progress, either modal or background. Usually not created directly but instantiated internally inside `ProgressManager.run` methods. * [ProgressWrapper](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-impl/src/com/intellij/openapi/progress/util/ProgressWrapper.java) – wraps an existing progress indicator, usually to fork another thread with the same cancellation policy. Use [SensitiveProgressWrapper](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-impl/src/com/intellij/concurrency/SensitiveProgressWrapper.java) to allow that separate thread's indicator to be canceled independently of the main thread.

* [Task](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/progress/Task.java) – encapsulates an operation to perform. See `Task`'s inner subclasses for backgroundable, modal and other base task classes.

### Starting (background-processes/progress-api/starting.md)
### Cancellation (background-processes/progress-api/cancellation.md)
#### Requesting Cancellation (background-processes/progress-api/cancellation/requesting-cancellation.md)
#### Handling Cancellation (background-processes/progress-api/cancellation/handling-cancellation.md)
### Tracking Progress (background-processes/progress-api/tracking-progress.md)
