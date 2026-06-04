# Non-Blocking Read Actions

BGT shouldn't hold [read locks](#read-actions) for a long time.
The reason is that if EDT needs a write action (for example, the user types something in the editor), it must be acquired as soon as possible.
Otherwise, the UI will freeze until all BGTs have released their read actions.
The following diagram presents this problem:

```MERMAID
---
config:
  gantt:
    numberSectionStyles: 2
displayMode: compact
---
gantt
    dateFormat X
    %% do not remove trailing space in axisFormat:
    axisFormat ‎
    section BGT
        very long read action               : 0, 6
    section EDT
        write action (waiting for the lock) : done, 2, 6
        write action (executing)            : 6, 8
        UI freeze                           : crit, 2, 8
        UI update (delayed)                 : crit, 8, 10
```

Sometimes, it is required to run a long read action, and it isn't possible to speed it up.
In such a case, the recommended approach is to cancel the read action whenever there is a write action about to occur and restart that read action later from scratch:

```MERMAID
---
config:
  gantt:
    numberSectionStyles: 2
displayMode: compact
---
gantt
    dateFormat X
    %% do not remove trailing space in axisFormat:
    axisFormat ‎
    section BGT
        very long read action               : 0, 2
        very long read action (2nd attempt) : 4, 10
        RA canceled                         : milestone, crit, 0, 4
        RA restarted from scratch           : milestone, 4, 4
    section EDT
        write action : 2, 4
        UI update    : 4, 6
```

In this case, the EDT won't be blocked and the UI freeze is avoided.
The total execution time of the read action will be longer due to multiple attempts, but not affecting the UI responsiveness is more important.

The canceling approach is widely used in various areas of the IntelliJ Platform: editor highlighting, code completion, "go to class/file/…" actions all work like this.
Read the [Background Processes](https://plugins.jetbrains.com/docs/intellij/background-processes.html) section for more details.

Warning:

Note that APIs mentioned in [Read Actions API](#read-actions-api) (except suspending `readAction()`) are blocking.

### Non-Blocking Read Actions API

Warning:

Plugins targeting versions 2024.1+ should use Write Allowing Read Actions available in the [Kotlin Coroutines Read Actions API](https://plugins.jetbrains.com/docs/intellij/coroutine-read-actions.html#coroutine-read-actions-api).

To run a non-blocking read action, use one of the available APIs:

* [ReadAction.nonBlocking()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/ReadAction.java) which returns [NonBlockingReadAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/NonBlockingReadAction.java) (NBRA). NBRA handles restarting the action out-of-the-box.

* [ReadAction.computeCancellable()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/ReadAction.java) which computes the result immediately in the current thread or throws an exception if there is a running or requested write action.

In both cases, when a read action is started and a write action occurs in the meantime, the read action is marked as canceled.
Read actions must [check for cancellation](https://plugins.jetbrains.com/docs/intellij/background-processes.html#handling-cancellation) often enough to trigger actual cancellation.
Although the cancellation mechanism may differ under the hood ([Progress API](https://plugins.jetbrains.com/docs/intellij/background-processes.html#progress-api) or [Kotlin Coroutines](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html)), the cancellation handling rules are the same in both cases.

Always check at the start of each read action if the [objects are still valid](#objects-validity) and if the whole operation still makes sense.
With `ReadAction.nonBlocking()`, use `expireWith()` or `expireWhen()` for that.

Note:

If NBRA needs to access a [file-based index](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html) (for example, it is doing any project-wide PSI analysis, resolves references, or performs other tasks depending on indexes), use `ReadAction.nonBlocking(…).inSmartMode()`.
