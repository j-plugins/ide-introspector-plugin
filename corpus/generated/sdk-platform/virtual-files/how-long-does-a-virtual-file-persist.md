---
id: sdk.virtual-files.how-long-does-a-virtual-file-persist
title: Virtual Files: How long does a virtual file persist?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, how, long, does, virtual, file]
---
Part of `sdk.virtual-files`.

A particular file on a disk is represented by equal `VirtualFile` instances for the IDE process's entire lifetime.
There may be several instances corresponding to the same file, and they can be garbage-collected.

The file is a [UserDataHolder](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/util/UserDataHolder.java), and the user data is shared between those equal instances.
If a file is deleted, its corresponding `VirtualFile` instance becomes invalid (`isValid()` returns `false`), and operations cause exceptions.

> Source: IntelliJ Platform SDK docs — Virtual Files: How long does a virtual file persist? (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
