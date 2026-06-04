---
id: sdk.threading-model.read-write-lock
title: Threading Model: Read-Write Lock
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, read, write, lock]
---
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

### Locks and EDT (threading-model/read-write-lock/locks-and-edt.md)
