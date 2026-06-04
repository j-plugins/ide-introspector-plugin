---
id: sdk.background-processes.progress-api.starting
title: Background Processes: Starting
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, starting]
---
Part of `sdk.background-processes.progress-api`.

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

> Source: IntelliJ Platform SDK docs — Background Processes: Starting (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
