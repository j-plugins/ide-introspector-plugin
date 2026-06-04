---
id: sdk.virtual-files.what-can-i-do-with-it
title: Virtual Files: What can I do with it?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, what, can, with]
---
Typical file operations are available, such as traverse the file system, get file contents, rename, move, or delete.
Recursive iteration should be performed using `VfsUtilCore.iterateChildrenRecursively()` to prevent endless loops caused by recursive symlinks.

