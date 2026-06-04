---
id: sdk.virtual-files
title: Virtual Files
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, virtual, files]
---
A [VirtualFile](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/VirtualFile.java) (VF) is the IntelliJ Platform's representation of a file in a [Virtual File System (VFS)](https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html).

Most commonly, a virtual file is a file in a local file system.
However, the IntelliJ Platform supports multiple pluggable file system implementations, so virtual files can also represent classes in a JAR file, old revisions of files loaded from a version control repository, and so on.

The VFS level deals only with binary content.
Contents of a `VirtualFile` are treated as a stream of bytes, but concepts like encodings and line separators are handled at higher system levels.

## Subtopics

- How do I get a virtual file? — `sdk.virtual-files.how-do-i-get-a-virtual-file`
- What can I do with it? — `sdk.virtual-files.what-can-i-do-with-it`
- Where does it come from? — `sdk.virtual-files.where-does-it-come-from`
- How long does a virtual file persist? — `sdk.virtual-files.how-long-does-a-virtual-file-persist`
- How do I create a virtual file? — `sdk.virtual-files.how-do-i-create-a-virtual-file`
- When are `VirtualFile` changes persisted on disk and loaded — `sdk.virtual-files.when-are-virtualfile-changes-persisted-on-disk-and-loaded`
- How do I get notified when VFS changes? — `sdk.virtual-files.how-do-i-get-notified-when-vfs-changes`
- Are there any utilities for analyzing and manipulating virtual files? — `sdk.virtual-files.are-there-any-utilities-for-analyzing-and-manipulating-virtual-files`
- How do I extend VFS? — `sdk.virtual-files.how-do-i-extend-vfs`
- What are the rules for working with VFS? — `sdk.virtual-files.what-are-the-rules-for-working-with-vfs`
- How can I store additional metadata in files? — `sdk.virtual-files.how-can-i-store-additional-metadata-in-files`

> Source: IntelliJ Platform SDK docs — Virtual Files (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
