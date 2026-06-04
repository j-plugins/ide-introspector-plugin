---
id: sdk.modifying-the-psi.combining-psi-and-document-modifications
title: Modifying the PSI: Combining PSI and Document Modifications
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, combining, psi, document, modifications]
---
In some cases, after modifying a PSI, it is required to perform an operation on the modified document (for example, start a [live template](https://plugins.jetbrains.com/docs/intellij/live-templates.html)).
To complete the PSI-based post-processing (such as formatting) and commit the changes to the document, call [PsiDocumentManager.doPostponedOperationsAndUnblockDocument()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiDocumentManager.java).

