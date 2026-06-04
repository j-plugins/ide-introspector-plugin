---
id: sdk.virtual-file-system.synchronous-and-asynchronous-refreshes
title: Virtual File System: Synchronous and Asynchronous Refreshes
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, synchronous, asynchronous, refreshes]
---
Part of `sdk.virtual-file-system`.

From the point of view of the caller, refresh operations can be either synchronous or asynchronous.
In fact, the refresh operations are executed according to their own threading policy.
The synchronous flag simply means that the calling thread will be blocked until the refresh operation (which will most likely run on a different thread) is completed.

Both synchronous and asynchronous refreshes can be initiated from any thread.
If a refresh is initiated from a background thread, the calling thread must not hold a read lock, because otherwise, a deadlock would occur.
See [IntelliJ Platform Architectural Overview](https://plugins.jetbrains.com/docs/intellij/threading-model.html) for more details on the threading model and read/write actions.

The same threading requirements also apply to functions like [LocalFileSystem.refreshAndFindFileByPath()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/openapi/vfs/LocalFileSystem.java), which perform a partial refresh if the file with the specified path is not found in the snapshot.

In nearly all cases, using asynchronous refreshes is strongly preferred.
If there is some code that needs to be executed after the refresh is complete, the code should be passed as a `postRunnable` parameter to one of the refresh methods:

* [RefreshQueue.createSession()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/openapi/vfs/newvfs/RefreshQueue.kt)

* [VirtualFile.refresh()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/VirtualFile.java)

In some cases, synchronous refreshes can cause deadlocks, depending on which [locks](https://plugins.jetbrains.com/docs/intellij/threading-model.html#read-write-lock) are held by the thread invoking the refresh operation.

> Source: IntelliJ Platform SDK docs — Virtual File System: Synchronous and Asynchronous Refreshes (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
