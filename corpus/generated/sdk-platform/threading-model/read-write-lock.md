---
id: sdk.threading-model.read-write-lock
title: Threading Model: Read-Write Lock
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, read, write, lock]
---
Part of `sdk.threading-model`.

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

> Source: IntelliJ Platform SDK docs — Threading Model: Read-Write Lock (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
