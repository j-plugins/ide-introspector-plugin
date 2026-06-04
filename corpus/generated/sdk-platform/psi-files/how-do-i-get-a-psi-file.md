---
id: sdk.psi-files.how-do-i-get-a-psi-file
title: PSI Files: How do I get a PSI file?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, how, get, psi, file]
---
| Context |API |
----------------
| [Action](https://plugins.jetbrains.com/docs/intellij/action-system.html) |[AnActionEvent.getData(CommonDataKeys.PSI_FILE)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnActionEvent.java) |
| [Document](https://plugins.jetbrains.com/docs/intellij/documents.html) |[PsiDocumentManager.getPsiFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiDocumentManager.java) |
| [PSI Element](https://plugins.jetbrains.com/docs/intellij/psi-elements.html) |[PsiElement.getContainingFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiElement.java) (may return `null` if the PSI element is not contained in a file) |
| [Virtual File](https://plugins.jetbrains.com/docs/intellij/virtual-file.html) |[PsiManager.findFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiManager.java), [PsiUtilCore.toPsiFiles()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/util/PsiUtilCore.java) |
| File Name |[FilenameIndex.getVirtualFilesByName()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/indexing-api/src/com/intellij/psi/search/FilenameIndex.java) and locate via [PsiManager.findFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiManager.java) or [PsiUtilCore.toPsiFiles()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/util/PsiUtilCore.java) |

