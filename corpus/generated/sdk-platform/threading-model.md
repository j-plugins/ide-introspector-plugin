---
id: sdk.threading-model
title: Threading Model
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, threading, model]
---
Tip:

It is highly recommended that readers unfamiliar with Java threads go through the official [Java Concurrency](https://docs.oracle.com/javase/tutorial/essential/concurrency/index.html) tutorial before reading this section.

The IntelliJ Platform is a highly concurrent environment.
Code is executed in many threads simultaneously.
In general, as in a regular [Swing](https://docs.oracle.com/javase/tutorial/uiswing/) application, threads can be categorized into two main groups:

* [Event Dispatch Thread](https://docs.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html) (EDT) – also known as the UI thread. Its main purpose is handling UI events (such as reacting to clicking a button or updating the UI), but the platform uses it also for writing data. EDT executes events taken from the Event Queue. Operations performed on EDT must be as fast as possible to not block other events in the queue and freeze the UI.

* background threads (BGT) – used for performing long-running and costly operations, or background tasks.

There is only one EDT and multiple BGT in the running application:

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
    section EDT
        UI task : 0, 1
        UI task : 1, 2
        write   : 3, 4
        UI task : 4, 5
        write   : 5, 6
        UI task : 7, 8
        UI task : 8, 9
        write   : 9, 10
    section BGT 1
        task : done, 0, 3
        task : done, 4, 6
        task : done, 7, 10
    section BGT 2
        task : done, 1, 2
        task : done, 3, 7
        task : done, 8, 10
    section ...
        ‎ : 0, 0
    section BGT N
        task : done, 0, 2
        task : done, 3, 6
        task : done, 7, 8
        task : done, 9, 10
```

It is possible to switch between BGT and EDT in both directions.
Operations can be scheduled to execute on EDT from BGT (and EDT) with `invokeLater()` methods (see the rest of this page for details).
Executing on BGT from EDT can be achieved with [background processes](https://plugins.jetbrains.com/docs/intellij/background-processes.html).

Warning:

Plugins targeting versions 2024.1+ should use [coroutine dispatchers](https://plugins.jetbrains.com/docs/intellij/coroutine-dispatchers.html) for switching between threads.

## Read-Write Lock

The IntelliJ Platform data structures (such as [Program Structure Interface](https://plugins.jetbrains.com/docs/intellij/psi.html), [Virtual File System](https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html), or [Project root model](https://plugins.jetbrains.com/docs/intellij/project-model.html)) aren't thread-safe.
Accessing them requires a synchronization mechanism ensuring that all threads see the data in a consistent and up-to-date state.
This is implemented with a single application-wide [read-write (RW) lock](https://w.wiki/7dBy) that must be acquired by threads requiring reading or writing to data models.

If a thread requires accessing a data model, it must acquire one of the locks:

|  |Read Lock |Write Intent Lock |Write Lock |
----------------------------------------------
| Allows for: |Reading data |Reading data and potentially upgrade to the write lock |Reading and writing data |
| Can be acquired from: |Any thread concurrently with other read locks and write intent lock |Any thread concurrently with read locks |Only from EDT concurrently with a write intent lock acquired on EDT |
| Can't be acquired if: |A write lock is held on another thread |Another write intent lock or write lock is held on another thread |Any other lock is held on another thread |

Tip:

See the [reasons](#why-can-write-intent-lock-be-acquired-from-any-thread-but-write-lock-only-from-edt) for allowing to acquire write intent lock from any thread and the write lock only from EDT.

The following table shows compatibility between locks in a simplified form:

|  |Read Lock |Write Intent Lock |Write Lock |
----------------------------------------------
| Read Lock |![+](images/green_checkmark.svg) |![+](images/green_checkmark.svg) |![-](images/red_cross.svg) |
| Write Intent Lock |![+](images/green_checkmark.svg) |![-](images/red_cross.svg) |![-](images/red_cross.svg) |
| Write Lock |![-](images/red_cross.svg) |![-](images/red_cross.svg) |![-](images/red_cross.svg) |

The described lock characteristics conclude the following:

* multiple threads can read data at the same time

* once a thread acquires the write lock, no other threads can read or write data

Note that acquiring write locks is prioritized over read locks.

Acquiring and releasing locks explicitly in code would be verbose and error-prone and must never be done by plugins.
The IntelliJ Platform enables write intent lock implicitly on EDT (see [Locks and EDT](#locks-and-edt) for details) and provides an [API for accessing data under read or write locks](#accessing-data).

### Locks and EDT

Although acquiring all types of locks can be, in theory, done from any threads, the platform implicitly acquires write intent lock and allows acquiring the write lock only on EDT.
It means that writing data can be done only on EDT.

Tip:

It is known that writing data only on EDT has negative consequences of potentially freezing the UI.
There is an in-progress effort to [allow writing data from any thread](https://youtrack.jetbrains.com/issue/IJPL-53).
See the [historical reason](#why-write-actions-are-currently-allowed-only-on-edt) for this behavior in the current platform versions.

The scope of implicitly acquiring the write intent lock on EDT differs depending on the platform version:

2023.3+:

Write intent lock is acquired automatically when operation is invoked on EDT with [Application.invokeLater()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java).

Earlier versions:

Write intent lock is acquired automatically when operation is invoked on EDT with methods such as:

* [Application.invokeLater()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java),

* [SwingUtilities.invokeLater()](https://docs.oracle.com/javase/8/docs/api/javax/swing/SwingUtilities.html#invokeLater-java.lang.Runnable-),

* [UIUtil.invokeAndWaitIfNeeded()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/ui/src/com/intellij/util/ui/UIUtil.java),

* [EdtInvocationManager.invokeLaterIfNeeded()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/ui/EdtInvocationManager.java),

* and other similar methods

It is recommended to use `Application.invokeLater()` if the operation is supposed to write data.
Use other methods for pure UI operations.

## Accessing Data

The IntelliJ Platform provides a simple API for accessing data under read or write locks in the form of read and write actions.

Read and write actions allow executing a piece of code under a lock, automatically acquiring it before an action starts, and releasing it after the action is finished.

Warning: Minimize Locking Scopes

Always try to wrap only the required operations into read/write actions, minimizing the time of holding locks.
If the read operation itself is long, consider using [non-blocking read actions](#non-blocking-read-actions) to avoid blocking the write lock and EDT.

### Read Actions

#### API

* [ReadAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/ReadAction.java) `run()` or `compute()`: Kotlin: ```KOTLIN val psiFile = ReadAction.compute<PsiFile, Throwable> { // read and return PsiFile } ``` Warning: Plugins implemented in Kotlin and targeting versions 2024.1+ should use suspending [readAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/coroutines.kt). See also [Coroutine Read Actions](https://plugins.jetbrains.com/docs/intellij/coroutine-read-actions.html). Java: ```JAVA PsiFile psiFile = ReadAction.compute(() -> { // read and return PsiFile }); ```

##### Alternative APIs

* [Application.runReadAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java): Kotlin: ```KOTLIN val psiFile = ApplicationManager.application.runReadAction { // read and return PsiFile } ``` Java: ```JAVA PsiFile psiFile = ApplicationManager.getApplication() .runReadAction((Computable<PsiFile>)() -> { // read and return PsiFile }); ``` Note that this API is considered low-level and should be avoided.

* Kotlin [runReadAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/actions.kt): ```KOTLIN val psiFile = runReadAction { // read and return PsiFile } ``` Note that this API is obsolete since 2024.1.

#### Rules

2023.3+:

Reading data is allowed from any thread.

Reading data on EDT invoked with `Application.invokeLater()` doesn't require an explicit read action, as the write intent lock allowing to read data is [acquired implicitly](#locks-and-edt).

Earlier versions:

Reading data is allowed from any thread.

Reading data on EDT doesn't require an explicit read action, as the write intent lock allowing to read data is [acquired implicitly](#locks-and-edt).

In all other cases, it is required to wrap a read operation in a read action with one of the [API](#read-actions-api) methods.

##### Objects Validity

The read objects aren't guaranteed to survive between several consecutive read actions.
Whenever starting a read action, check if the PSI/VFS/project/module is still valid.
Example:

Kotlin:

```KOTLIN
val virtualFile = runReadAction { // read action 1
  // read a virtual file
}
// do other time-consuming work...
val psiFile = runReadAction { // read action 2
  if (virtualFile.isValid()) { // check if the virtual file is valid
    PsiManager.getInstance(project).findFile(virtualFile)
  } else null
}
```

Java:

```JAVA
VirtualFile virtualFile = ReadAction.compute(() -> { // read action 1
  // read a virtual file
});
// do other time-consuming work...
PsiFile psiFile = ReadAction.compute(() -> { // read action 2
  if (virtualFile.isValid()) { // check if the virtual file is valid
    return PsiManager.getInstance(project).findFile(virtualFile);
  }
  return null;
});
```

Between executing first and second read actions, another thread could invalidate the virtual file:

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
    section Thread 1
        read action 1       : 0, 1
        time-consuming work : done, 1, 4
        read action 2       : 4, 5
    section Thread 2
        delete virtual file : crit, 2, 3
```

### Write Actions

#### API

* [WriteAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/WriteAction.java) `run()` or `compute()`: Kotlin: ```KOTLIN WriteAction.run<Throwable> { // write data } ``` Warning: Plugins implemented in Kotlin and targeting versions 2024.1+ should use suspending [writeAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/coroutines.kt). Java: ```JAVA WriteAction.run(() -> { // write data }); ```

##### Alternative APIs

* [Application.runWriteAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java): Kotlin: ```KOTLIN ApplicationManager.application.runWriteAction { // write data } ``` Java: ```JAVA ApplicationManager.getApplication().runWriteAction(() -> { // write data }); ``` Note that this API is considered low-level and should be avoided.

* Kotlin [runWriteAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/actions.kt): ```KOTLIN runWriteAction { // write data } ``` Note that this API is obsolete since 2024.1.

#### Rules

2023.3+:

Writing data is only allowed on EDT invoked with `Application.invokeLater()`.

Write operations must always be wrapped in a write action with one of the [API](#write-actions-api) methods.

Modifying the model is only allowed from write-safe contexts (see [Invoking Operations on EDT and Modality](#invoking-operations-on-edt-and-modality)).

Earlier versions:

Writing data is only allowed on EDT.

Write operations must always be wrapped in a write action with one of the [API](#write-actions-api) methods.

Modifying the model is only allowed from write-safe contexts, including user actions and `SwingUtilities.invokeLater()` calls from them (see [Invoking Operations on EDT and Modality](#invoking-operations-on-edt-and-modality)).

It is forbidden to modify PSI, VFS, or project model from inside UI renderers or `SwingUtilities.invokeLater()`.

Tip:

[Thread Access Info](https://plugins.jetbrains.com/plugin/16815-thread-access-info) plugin visualizes Read/Write Access and Thread information in the debugger.

## Invoking Operations on EDT and Modality

Operations that write data on EDT should be invoked with `Application.invokeLater()` because it allows specifying the modality state ([ModalityState](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/ModalityState.java)) for the scheduled operation.
This is not supported by `SwingUtilities.invokeLater()` and similar APIs.

Warning:

Note that `Application.invokeLater()` must be used to write data in versions 2023.3+.

`ModalityState` represents the stack of active modal dialogs and is used in calls to `Application.invokeLater()` to ensure the scheduled runnable can execute within the given modality state, meaning when the same set of modal dialogs or a subset is present.

To better understand what problem `ModalityState` solves, consider the following scenario:

1. A user action is started.

2. In the meantime, another operation is scheduled on EDT with `SwingUtilities.invokeLater()` (without modality state support).

3. The action from 1. now shows a dialog asking a Yes/No question.

4. While the dialog is shown, the operation from 2. is now processed and does changes to the data model, which invalidates PSI.

5. The user clicks Yes or No in the dialog, and it executes some code based on the answer.

6. Now, the code to be executed as the result of the user's answer has to deal with the changed data model it was not prepared for. For example, it was supposed to execute changes in the PSI that might be already invalid.

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
    section EDT
        1. Start action         : 0, 2
        3. Show dialog          : 2, 3
        4. Modify data          : crit, active, 3, 4
        5. Answer dialog        : 4, 5
        6. Work on invalid data : crit, 5, 7
    section BGT
        2. invokeLater()        : crit, active, 1, 2
```

Passing the modality state solves this problem:

1. A user action is started.

2. In the meantime, another operation is scheduled on EDT with `Application.invokeLater()` (supporting modality state).
The operation is scheduled with `ModalityState.defaultModalityState()` (see the table below for other helper methods).

3. The action from 1. now shows a dialog asking a Yes/No question.
This adds a modal dialog to the modality state stack.

4. While the dialog is shown, the scheduled operation waits as it was scheduled with a "lower" modality state than the current state with an additional dialog.

5. The user clicks Yes or No in the dialog, and it executes some code based on the answer.

6. The code is executed on data in the same state as before the dialog was shown.

7. The operation from 1. is executed now without interfering with the user's action.

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
    section EDT
        1. Start action          : 0, 2
        3. Show dialog           : 2, 3
        5. Answer dialog         : 3, 4
        6. Work on correct data  : 4, 6
        7. Modify data           : active, 6, 7
    section BGT
        2. invokeLater()         : active, 1, 2
        4. Wait for dialog close : done, 2, 4
```

The following table presents methods providing useful modality states to be passed to `Application.invokeLater()`:

| [ModalityState](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/ModalityState.java) |Description |
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
| `defaultModalityState()`  Used if none specified   |If invoked from EDT, it uses the `ModalityState.current()`.  If invoked from a background process started with `ProgressManager`, the operation can be executed in the same dialog that the process started.  This is the optimal choice in most cases.   |
| `current()` |The operation can be executed when the modality state stack doesn't grow since the operation was scheduled. |
| `stateForComponent()` |The operation can be executed when the topmost shown dialog is the one that contains the specified component or is one of its parent dialogs. |
| `nonModal()` or  `NON_MODAL`   |The operation will be executed after all modal dialogs are closed. If any of the open (unrelated) projects displays a per-project modal dialog, the operation will be performed after the dialog is closed. |
| `any()` |The operation will be executed as soon as possible regardless of modal dialogs (the same as with `SwingUtilities.invokeLater()`). It can be used for scheduling only pure UI operations. Modifying PSI, VFS, or project model is prohibited.Don't use it unless absolutely needed.   |

Note:

If EDT activity needs to access a [file-based index](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html) (for example, it is doing any project-wide PSI analysis, resolves references, or performs other tasks depending on indexes), use [DumbService.smartInvokeLater()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/DumbService.kt).
This API also supports `ModalityState` and runs the operation after all possible indexing processes have been completed.

## Non-Blocking Read Actions

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

## Avoiding UI Freezes

### Don't Perform Long Operations on EDT

In particular, don't traverse [VFS](https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html), parse [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html), resolve [references,](https://plugins.jetbrains.com/docs/intellij/psi-references.html) or query [indexes](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html).

There are still some cases when the platform itself invokes such expensive code (for example, resolve in `AnAction.update()`), but these are being worked on.
Meanwhile, try to speed up what you can in your plugin as it will be generally beneficial and will also improve background highlighting performance.

#### Action Update

For implementations of [AnAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnAction.java), plugin authors should specifically
review the documentation of `AnAction.getActionUpdateThread()` in the [Action System](https://plugins.jetbrains.com/docs/intellij/action-system.html) section as it describes how threading works for actions.

#### Minimize Write Actions Scope

Write actions currently [have to happen on EDT](#locks-and-edt).
To speed them up, as much as possible should be moved out of the write action into a preparation step which can be then invoked in the [background](https://plugins.jetbrains.com/docs/intellij/background-processes.html) or inside an [NBRA](#non-blocking-read-actions-api).

#### Slow Operations on EDT Assertion

Some of the long operations are reported by [SlowOperations.assertSlowOperationsAreAllowed()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/util/SlowOperations.java).
According to its Javadoc, they must be moved to BGT.
This can be achieved with the techniques mentioned in the Javadoc, [background processes](https://plugins.jetbrains.com/docs/intellij/background-processes.html), [Application.executeOnPooledThread()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java), or [coroutines](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html) (recommended for plugins targeting 2024.1+).
Note that the assertion is enabled in IDE EAP versions, [internal mode](https://plugins.jetbrains.com/docs/intellij/enabling-internal.html), or [development instance](https://plugins.jetbrains.com/docs/intellij/ide-development-instance.html), and regular users don't see them in the IDE.
This will change in the future, so fixing these exceptions is required.

### Event Listeners

Listeners mustn't perform any heavy operations.
Ideally, they should only clear some caches.

It is also possible to schedule background processing of events.
In such cases, be prepared that some new events might be delivered before the background processing starts – and thus the world might have changed by that moment or even in the middle of background processing.
Consider using [MergingUpdateQueue](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/util/ui/update/MergingUpdateQueue.kt) and [NBRA](#non-blocking-read-actions-api) to mitigate these issues.

### VFS Events

Massive batches of VFS events can be pre-processed in the background with [AsyncFileListener](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/AsyncFileListener.java).

### Investigating UI Freezes

See the [Investigating IntelliJ Platform UI Freezes](https://blog.jetbrains.com/platform/2025/09/investigating-intellij-platform-ui-freezes/) blog post for techniques to investigate UI freezes.

## Kotlin Notebooks

IntelliJ Platform sources include Kotlin Notebooks helping to understand the core concepts of the threading model:

1. [Context Propagation](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/docs/notebooks/1-ContextPropagation.ipynb)

2. [Cancellation Model](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/docs/notebooks/2-CancellationModel.ipynb)

3. [Read-Write Lock](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/docs/notebooks/3-ReadWriteLock.ipynb)

The notebooks contain concept explanations accompanied by code that can be run interactively in the IDE.
Code can be changed for experimentation.

See more information about the integration of [Kotlin Notebooks with the IntelliJ Platform](https://plugins.jetbrains.com/docs/intellij/tools-kotlin-notebook.html).

## FAQ

### How to check whether the current thread is the EDT/UI thread?

Use `Application.isDispatchThread()`.

If code must be invoked on EDT and the current thread can be EDT or BGT, use [UIUtil.invokeLaterIfNeeded()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/ui/src/com/intellij/util/ui/UIUtil.java).
If the current thread is EDT, this method will run code immediately or will schedule a later invocation if the current thread is BGT.

### Why write actions are currently allowed only on EDT?

Reading data model was often performed on EDT to display results in the UI.
The IntelliJ Platform is more than 20 years old, and in its beginnings Java didn't offer features like generics and lambdas.
Code that acquired read locks was very verbose.
For convenience, it was decided that reading data can be done on EDT without read locks (even implicitly acquired).

The consequence of this was that writing had to be allowed only on EDT to avoid read/write conflicts.
The nature of EDT provided this possibility out-of-the-box due to being a single thread.
Event queue guaranteed that reads and writes were ordered and executed one by one and couldn't interweave.

### Why can write intent lock be acquired from any thread but write lock only from EDT?

In the current platform state, technically, write intent lock can be acquired on any thread (it is done only on EDT in practice), but write lock can be acquired only on EDT.

Write intent lock was introduced as a "replacement" for EDT in the context of acquiring write lock.
Instead of allowing to acquire write lock on EDT only, it was planned to make it possible to acquire it from under write intent lock on any thread.
Write intent lock provides read access that was also available on EDT.
This behavior wasn't enabled in production, and the planned locking mechanism has changed.
It is planned to allow for acquiring write lock from any thread, even without a write intent lock.
Write intent lock will be still available and will allow performing read sessions finished with data writing.

Note: Something missing?

If a topic is not covered in the above sections,
let us know via the Feedback widget displayed on the right,
or [other channels](https://plugins.jetbrains.com/docs/intellij/getting-help.html#problems-with-the-guide).

Be specific about the topics and reasons for adding them and leave your email in case we need
more details. Thanks for your feedback!

> Source: IntelliJ Platform SDK docs — Threading Model (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
