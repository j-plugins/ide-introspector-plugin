---
id: sdk.virtual-files.when-are-virtualfile-changes-persisted-on-disk-and-loaded
title: Virtual Files: When are `VirtualFile` changes persisted on disk and loaded
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, when, are, virtualfile, changes, persisted]
---
Part of `sdk.virtual-files`.

When are `VirtualFile` changes persisted on disk and loaded from disk to VFS?

Since version 2026.2, changes to a `VirtualFile` (`getOutputStream()` or `setBinaryContent()`) may reach the underlying files with some delay – the actual IO is postponed and executed asynchronously.

With content writing I/O moved outside the enclosing write action, the enclosing write action finishes quicker, thus reducing UI freezes.
The I/O postponing is invisible for access via VFS – VFS maintains the illusion that the update is fully finished while it is still ongoing.
However, it may be visible if the same file is accessed bypassing VFS, either via `java.io`/`java.nio` API directly or from an external process.
To ensure changes have been applied forcibly – for example, to read the new file content from an external process – use [ManagingFS.getInstance().flushPendingUpdates()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/openapi/vfs/newvfs/ManagingFS.java) (outside write action).

Changes in the underlying files also reach VFS with some (normally, short) delay.
`VirtualFile.refresh()` could be used to ensure the changes are noticed.
It could introduce significant delay, so use it with caution.
More about refreshing in [Virtual File System (VFS)](https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html#synchronous-and-asynchronous-refreshes).

> Source: IntelliJ Platform SDK docs — Virtual Files: When are `VirtualFile` changes persisted on disk and loaded (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
