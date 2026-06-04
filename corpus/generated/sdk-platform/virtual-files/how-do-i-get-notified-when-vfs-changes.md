---
id: sdk.virtual-files.how-do-i-get-notified-when-vfs-changes
title: Virtual Files: How do I get notified when VFS changes?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, how, get, notified, when, vfs]
---
Part of `sdk.virtual-files`.

Note:

See [Virtual File System Events](https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html#virtual-file-system-events) for important details.

Implement [BulkFileListener](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/newvfs/BulkFileListener.java) and subscribe to the [message bus](https://plugins.jetbrains.com/docs/intellij/messaging-infrastructure.html) topic `VirtualFileManager.VFS_CHANGES`.
For example:

```JAVA
project.getMessageBus().connect().subscribe(
    VirtualFileManager.VFS_CHANGES,
    new BulkFileListener() {
      @Override
      public void after(@NotNull List<? extends VFileEvent> events) {
        // handle the events
      }
    });
```

See [Message Infrastructure](https://plugins.jetbrains.com/docs/intellij/messaging-infrastructure.html) and [Plugin Listeners](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html) for more details.

For a non-blocking alternative see [AsyncFileListener](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/AsyncFileListener.java).

> Source: IntelliJ Platform SDK docs — Virtual Files: How do I get notified when VFS changes? (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
