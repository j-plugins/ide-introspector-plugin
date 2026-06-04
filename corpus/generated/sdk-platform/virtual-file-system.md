# Virtual File System

The Virtual File System (VFS) is a component of the IntelliJ Platform that encapsulates most of its activity for working with files represented as [Virtual File](https://plugins.jetbrains.com/docs/intellij/virtual-file.html).

It serves the following main purposes:

* Providing a universal API for working with files regardless of their actual location (on disk, in an archive, on an HTTP server, etc.)

* Tracking file modifications and providing both old and new versions of the file content when a change is detected.

* Providing a possibility to [associate additional persistent data](https://plugins.jetbrains.com/docs/intellij/virtual-file.html#how-can-i-store-additional-metadata-in-files) with a file in the VFS.

To provide the last two features, the VFS manages a persistent snapshot of some of the user's hard disk contents.
The snapshot stores only those files which have been requested at least once through the VFS API and is asynchronously updated to match the changes happening on the disk.

The snapshot is application level, not project level - so, if some file (for example, a class in the JDK) is referenced by multiple projects, only one copy of its contents will be stored in the VFS.

All VFS access operations go through the snapshot.

If some information is requested through the VFS APIs and is not available in the snapshot, it is loaded from the disk and stored into the snapshot.
If the information is available in the snapshot, the snapshot data is returned.
The contents of files and the lists of files in directories are stored in the snapshot only if that specific information was accessed.
Otherwise, only file metadata like name, length, timestamp, attributes are stored.

Note:

This means that the state of the file system and the file contents displayed in the IntelliJ Platform UI come from the snapshot, which may not always match the disk's actual contents.
For example, in some cases, deleted files can still be visible in the UI for some time before the deletion is picked up by the IntelliJ Platform.

The snapshot is updated from the disk during refresh operations, which generally happen asynchronously.
All write operations made through the VFS are synchronous - i.e., the contents are saved to disk immediately.

A refresh operation synchronizes the state of a part of the VFS with the actual disk contents.
Refresh operations are explicitly invoked by the IntelliJ Platform or plugin code - i.e., when a file is changed on disk while the IDE is running, the change will not be immediately picked up by the VFS.
The VFS will be updated during the next refresh operation, which includes the file in its scope.

IntelliJ Platform refreshes the entire project contents asynchronously on startup.
By default, it performs a refresh operation when the user switches to it from another app.
Still, users can turn this off via `Settings | Appearance & Behavior | System Settings | Synchronize external changes\[...]`.

On Windows, Mac, and Linux, a native file watcher process is started that receives file change notifications from the file system and reports them to the IntelliJ Platform.
If a file watcher is available, a refresh operation looks only at the files that have been reported as changed by the file watcher.
If no file watcher is present, a refresh operation walks through all directories and files in the refresh scope.

Tip:

Invoke the [internal action](https://plugins.jetbrains.com/docs/intellij/internal-actions-intro.html) `Tools | Internal Actions | VFS | Show Watched VFS Roots` to see all registered roots for the current project.

Refresh operations are based on file timestamps.
If a file's contents were changed, but its timestamp remained the same, the IntelliJ Platform will not pick up the updated contents.

There is currently no facility for removing files from the snapshot.
If a file was loaded there once, it remains there forever unless it was deleted from the disk, and a refresh operation was called on one of its parent directories.

The VFS itself does not honor ignored files listed in `Settings | Editor | File Types` and folders to ignore and excluded folders listed in `Project Structure | Modules | Sources | Excluded`.
If the application code accesses them, the VFS will load and return their contents.
In most cases, the ignored files and excluded folders must be skipped from processing by higher-level code.

During the lifetime of a running instance of an IntelliJ Platform IDE, multiple `VirtualFile` instances may correspond to the same disk file.
They are equal, have the same `hashCode`, and share the user data.

## Synchronous and Asynchronous Refreshes

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

## Virtual File System Events (sdk.virtual-file-system.virtual-file-system-events)

> Source: IntelliJ Platform SDK docs — Virtual File System (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
