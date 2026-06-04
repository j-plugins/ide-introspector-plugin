---
id: sdk.virtual-files.how-do-i-create-a-virtual-file
title: Virtual Files: How do I create a virtual file?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, how, create, virtual, file]
---
Part of `sdk.virtual-files`.

Usually, you don't.
As a general rule, files are created either through the [PSI API](https://plugins.jetbrains.com/docs/intellij/psi.html) or through the regular [java.io.File](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/io/File.html) API.

If one needs to create a file through VFS, use `VirtualFile.createChildData()` to create a `VirtualFile` instance and `VirtualFile.setBinaryContent()` to write some data to the file.

> Source: IntelliJ Platform SDK docs — Virtual Files: How do I create a virtual file? (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
