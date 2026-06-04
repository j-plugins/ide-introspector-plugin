---
id: sdk.psi-files.how-long-do-psi-files-persist
title: PSI Files: How long do PSI files persist?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, how, long, psi, files, persist]
---
Like [Documents](https://plugins.jetbrains.com/docs/intellij/documents.html), PSI files are weakly referenced from the corresponding `VirtualFile` instances and can be garbage-collected if not referenced by anyone.

