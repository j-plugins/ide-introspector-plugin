---
id: sdk.psi-files.how-do-i-create-a-psi-file
title: PSI Files: How do I create a PSI file?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, how, create, psi, file]
---
Part of `sdk.psi-files`.

[PsiFileFactory.createFileFromText()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiFileFactory.java) creates an in-memory PSI file with the specified contents.

To save the PSI file to disk, use its parent directory's [PsiDirectory.add()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiDirectory.java).

> Source: IntelliJ Platform SDK docs — PSI Files: How do I create a PSI file? (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
