# Progress API

Warning: Use Kotlin Coroutines

Plugins targeting 2024.1+ should use [Kotlin coroutines](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html), which is a more performant solution and provides the cancellation mechanism out of the box.

See [Execution Contexts](https://plugins.jetbrains.com/docs/intellij/execution-contexts.html) for coroutine-based APIs to use in different contexts.

The Progress API allows running processes on BGT with a modal (dialog), non-modal (visible in the status bar), or invisible progress.
It also allows for process cancellation and progress tracking (as a fraction of work done or textual).

The key classes are:

* [ProgressManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/progress/ProgressManager.java) – provides methods to execute and manage background processes

* [ProgressIndicator](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/progress/ProgressIndicator.java) – an object associated with a running process. It allows cancelling the process and optionally tracking its progress. The current thread's indicator can be retrieved any time via `ProgressManager.getProgressIndicator()`. There are many `ProgressIndicator` implementations and the most commonly used are: * [EmptyProgressIndicator](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/progress/EmptyProgressIndicator.java) – invisible (ignores text/fraction-related methods), used only for cancellation tracking. Remembers its creation [modality state](https://plugins.jetbrains.com/docs/intellij/threading-model.html#invoking-operations-on-edt-and-modality). * [ProgressIndicatorBase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-impl/src/com/intellij/openapi/progress/util/ProgressIndicatorBase.java) – invisible but can be made visible by subclassing. Stores text/fraction and allows retrieving them and possibly show in the UI. Non-modal by default. * [ProgressWindow](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-impl/src/com/intellij/openapi/progress/util/ProgressWindow.java) – visible progress, either modal or background. Usually not created directly but instantiated internally inside `ProgressManager.run` methods. * [ProgressWrapper](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-impl/src/com/intellij/openapi/progress/util/ProgressWrapper.java) – wraps an existing progress indicator, usually to fork another thread with the same cancellation policy. Use [SensitiveProgressWrapper](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-impl/src/com/intellij/concurrency/SensitiveProgressWrapper.java) to allow that separate thread's indicator to be canceled independently of the main thread.

* [Task](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/progress/Task.java) – encapsulates an operation to perform. See `Task`'s inner subclasses for backgroundable, modal and other base task classes.

### Starting

Background processes encapsulated within `Task` can be run with queueing them.
Example:

Kotlin:

```KOTLIN
object : Task.Backgroundable(project, "Synchronizing data", true) {
  override fun run(indicator: ProgressIndicator) {
    // operation
  }
}
  .setCancelText("Stop loading")
  .queue()
```

Java:

```JAVA
new Task.Backgroundable(project, "Synchronizing data", true) {
  public void run(ProgressIndicator indicator) {
    // operation
  }
}
  .setCancelText("Stop loading")
  .queue();
```

Tip:

To run a backgroundable task under a custom progress indicator, for example, `EmptyProgressIndicator` to hide progress, use:

```KOTLIN
ProgressManager.getInstance()
    .runProcessWithProgressAsynchronously(
        backgroundableTask, EmptyProgressIndicator())
```

Note that hiding progress from users should be avoided as it may break the [UX](https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html).

`ProgressManager` also allows running `Runnable` and `Computable` instances not wrapped within `Task` with several `run*()` methods.
Example:

Kotlin:

```KOTLIN
ProgressManager.getInstance().runProcessWithProgressSynchronously(
    ThrowableComputable {
      // operation
    },
    "Synchronizing data", true, project
)
```

Java:

```JAVA
ProgressManager.getInstance().runProcessWithProgressSynchronously(
    () -> {
      // operation
    },
    "Synchronizing data", true, project
);
```

### Cancellation (sdk.background-processes.progress-api.cancellation)
### Tracking Progress

Displaying progress to the user is achieved with:

* `ProgressIndicator` - if available in the current context

* `ProgressManager` - if no indicator instance is available in the current context

To report progress with `ProgressIndicator`, use the following methods:

* `setText(String)` – sets the progress text displayed above the progress bar

* `setText2(String)` – sets the progress details text displayed under the progress bar

* `setFraction(double)` – sets the progress fraction: a number between 0.0 (nothing) and 1.0 (all) reflecting the ratio of work that has already been done. Only works for determinate indicator. The fraction should provide the user with an estimation of the time left. If this is impossible, consider making the progress indeterminate.

* `setIndeterminate(boolean)` – marks the progress indeterminate (for processes that can't estimate the amount of work to be done) or determinate (for processes that can display the fraction of the work done using `setFraction(double)`).

`ProgressManager` allows for reporting progress texts through `progress()`/`progress2()` methods, which are counterparts of `ProgressIndicator.setText()`/`setText2()`.
In addition, it exposes the `ProgressIndicator.getProgressIndicator()` method for getting an indicator instance associated with the current thread.
