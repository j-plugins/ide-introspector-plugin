---
id: sdk.virtual-files.how-do-i-extend-vfs
title: Virtual Files: How do I extend VFS?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, how, extend, vfs]
---
To provide an alternative file system implementation (for example, an FTP file system), implement the [VirtualFileSystem](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/VirtualFileSystem.java) class (most likely you'll also need to implement `VirtualFile`),
and register your implementation via [com.intellij.virtualFileSystem](https://jb.gg/ipe?extensions=com.intellij.virtualFileSystem) extension point
.

To hook into operations performed in the local file system (for example, when developing a version control system integration that needs custom rename/move handling), implement [LocalFileOperationsHandler](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/openapi/vfs/LocalFileOperationsHandler.java) and register it via `LocalFileSystem.registerAuxiliaryFileOperationsHandler()`.

