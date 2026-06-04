---
id: sdk.virtual-files.where-does-it-come-from
title: Virtual Files: Where does it come from?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, where, does, come, from]
---
Part of `sdk.virtual-files`.

The VFS is built incrementally by scanning the file system up and down, starting from the project root.
VFS refresh operations detect new files appearing in the file system.
A refresh operation can be initiated programmatically using `VirtualFileManager.syncRefresh()`/`asyncRefresh()` or `VirtualFile.refresh()`.
VFS refreshes are also triggered whenever file system watchers receive file system change notifications.

Invoking a VFS refresh might be necessary for accessing a file that has just been created by an external tool through the IntelliJ Platform APIs.

> Source: IntelliJ Platform SDK docs — Virtual Files: Where does it come from? (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
