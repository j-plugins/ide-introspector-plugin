---
id: sdk.virtual-file-system.virtual-file-system-events
title: Virtual File System: Virtual File System Events
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, virtual, file, system, events]
---
All changes happening in the virtual file system (either due to refresh operations or caused by user actions) are reported as virtual file system events.
VFS events are always fired in the event dispatch thread and in a write action.

The most efficient way to listen to VFS events is to implement [BulkFileListener](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/newvfs/BulkFileListener.java) and to subscribe with it to the [VirtualFileManager.VFS_CHANGES](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/VirtualFileManager.java) topic.
A non-blocking variant [AsyncFileListener](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/AsyncFileListener.java) is also available.
See [How do I get notified when VFS changes?](https://plugins.jetbrains.com/docs/intellij/virtual-file.html#how-do-i-get-notified-when-vfs-changes) for implementation details.

Warning:

VFS listeners are application level and will receive events for changes happening in all the projects opened by the user.
You may need to filter out events that aren't relevant to your task (e.g., via [ProjectFileIndex.isInContent()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/roots/ProjectFileIndex.java)).

VFS events are sent both before and after each change, and you can access the old contents of the file in the before event.
Note that events caused by a refresh are sent after the changes have already occurred on disk.
So when you process the `beforeFileDeletion` event, for example, the file has already been deleted from the disk.
However, it is still present in the VFS snapshot, and you can access its last contents using the VFS API.

Note that a refresh operation fires events only for changes in files that have been loaded in the snapshot.
For example, if you accessed a `VirtualFile` for a directory but never loaded its contents using [VirtualFile.getChildren()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/VirtualFile.java), you may not get `fileCreated` notifications when files are created in that directory.

If you load only a single file in a directory using `VirtualFile.findChild()`, you will get notifications for changes to that file, but you may not get created/deleted notifications for other files in the same directory.

