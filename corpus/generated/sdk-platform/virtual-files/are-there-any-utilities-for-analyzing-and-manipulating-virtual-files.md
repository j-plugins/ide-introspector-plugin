---
id: sdk.virtual-files.are-there-any-utilities-for-analyzing-and-manipulating-virtual-files
title: Virtual Files: Are there any utilities for analyzing and manipulating virtual files?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, are, there, any, utilities, for]
---
[VfsUtil](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/openapi/vfs/VfsUtil.java) and [VfsUtilCore](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/VfsUtilCore.java) provide utility methods for analyzing files in the Virtual File System.

For storing a large set of Virtual Files, use the dedicated `VfsUtilCore.createCompactVirtualFileSet()` method.

Use [ProjectLocator](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/ProjectLocator.kt) to find the projects that contain a given virtual file.

