# Virtual Files

A [VirtualFile](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/VirtualFile.java) (VF) is the IntelliJ Platform's representation of a file in a [Virtual File System (VFS)](https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html).

Most commonly, a virtual file is a file in a local file system.
However, the IntelliJ Platform supports multiple pluggable file system implementations, so virtual files can also represent classes in a JAR file, old revisions of files loaded from a version control repository, and so on.

The VFS level deals only with binary content.
Contents of a `VirtualFile` are treated as a stream of bytes, but concepts like encodings and line separators are handled at higher system levels.

## How do I get a virtual file?

| Context |API |
----------------
| [Action](https://plugins.jetbrains.com/docs/intellij/action-system.html) |[AnActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnActionEvent.java)  [AnActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnActionEvent.java) for multiple selection   |
| [Document](https://plugins.jetbrains.com/docs/intellij/documents.html) |[FileDocumentManager.getFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/fileEditor/FileDocumentManager.java) |
| [PSI File](https://plugins.jetbrains.com/docs/intellij/psi-files.html) |[PsiFile.getVirtualFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiFile.java) (may return `null` if the PSI file exists only in memory) |
| File Name |[FilenameIndex.getVirtualFilesByName()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/indexing-api/src/com/intellij/psi/search/FilenameIndex.java) |
| Local File System Path |[LocalFileSystem.findFileByIoFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/openapi/vfs/LocalFileSystem.java)  [VirtualFileManager.findFileByNioPath()/refreshAndFindFileByNioPath()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/VirtualFileManager.java)   |

## What can I do with it?

Typical file operations are available, such as traverse the file system, get file contents, rename, move, or delete.
Recursive iteration should be performed using `VfsUtilCore.iterateChildrenRecursively()` to prevent endless loops caused by recursive symlinks.

## Where does it come from?

The VFS is built incrementally by scanning the file system up and down, starting from the project root.
VFS refresh operations detect new files appearing in the file system.
A refresh operation can be initiated programmatically using `VirtualFileManager.syncRefresh()`/`asyncRefresh()` or `VirtualFile.refresh()`.
VFS refreshes are also triggered whenever file system watchers receive file system change notifications.

Invoking a VFS refresh might be necessary for accessing a file that has just been created by an external tool through the IntelliJ Platform APIs.

## How long does a virtual file persist?

A particular file on a disk is represented by equal `VirtualFile` instances for the IDE process's entire lifetime.
There may be several instances corresponding to the same file, and they can be garbage-collected.

The file is a [UserDataHolder](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/util/UserDataHolder.java), and the user data is shared between those equal instances.
If a file is deleted, its corresponding `VirtualFile` instance becomes invalid (`isValid()` returns `false`), and operations cause exceptions.

## How do I create a virtual file?

Usually, you don't.
As a general rule, files are created either through the [PSI API](https://plugins.jetbrains.com/docs/intellij/psi.html) or through the regular [java.io.File](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/io/File.html) API.

If one needs to create a file through VFS, use `VirtualFile.createChildData()` to create a `VirtualFile` instance and `VirtualFile.setBinaryContent()` to write some data to the file.

## When are `VirtualFile` changes persisted on disk and loaded

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

## How do I get notified when VFS changes?

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

## Are there any utilities for analyzing and manipulating virtual files?

[VfsUtil](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/openapi/vfs/VfsUtil.java) and [VfsUtilCore](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/VfsUtilCore.java) provide utility methods for analyzing files in the Virtual File System.

For storing a large set of Virtual Files, use the dedicated `VfsUtilCore.createCompactVirtualFileSet()` method.

Use [ProjectLocator](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/ProjectLocator.kt) to find the projects that contain a given virtual file.

## How do I extend VFS?

To provide an alternative file system implementation (for example, an FTP file system), implement the [VirtualFileSystem](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/VirtualFileSystem.java) class (most likely you'll also need to implement `VirtualFile`),
and register your implementation via [com.intellij.virtualFileSystem](https://jb.gg/ipe?extensions=com.intellij.virtualFileSystem) extension point
.

To hook into operations performed in the local file system (for example, when developing a version control system integration that needs custom rename/move handling), implement [LocalFileOperationsHandler](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/openapi/vfs/LocalFileOperationsHandler.java) and register it via `LocalFileSystem.registerAuxiliaryFileOperationsHandler()`.

## What are the rules for working with VFS?

See [Virtual File System](https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html) for a detailed description of the VFS architecture and usage guidelines.

## How can I store additional metadata in files?

See:

* [FilePropertyPusher](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/roots/impl/FilePropertyPusher.java)

* [FileAttribute](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/openapi/vfs/newvfs/FileAttribute.java)


> Source: IntelliJ Platform SDK docs — Virtual Files (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
