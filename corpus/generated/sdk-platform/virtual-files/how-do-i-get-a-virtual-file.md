---
id: sdk.virtual-files.how-do-i-get-a-virtual-file
title: Virtual Files: How do I get a virtual file?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, how, get, virtual, file]
---
Part of `sdk.virtual-files`.

| Context |API |
----------------
| [Action](https://plugins.jetbrains.com/docs/intellij/action-system.html) |[AnActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnActionEvent.java)  [AnActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnActionEvent.java) for multiple selection   |
| [Document](https://plugins.jetbrains.com/docs/intellij/documents.html) |[FileDocumentManager.getFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/fileEditor/FileDocumentManager.java) |
| [PSI File](https://plugins.jetbrains.com/docs/intellij/psi-files.html) |[PsiFile.getVirtualFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiFile.java) (may return `null` if the PSI file exists only in memory) |
| File Name |[FilenameIndex.getVirtualFilesByName()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/indexing-api/src/com/intellij/psi/search/FilenameIndex.java) |
| Local File System Path |[LocalFileSystem.findFileByIoFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/openapi/vfs/LocalFileSystem.java)  [VirtualFileManager.findFileByNioPath()/refreshAndFindFileByNioPath()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/VirtualFileManager.java)   |

> Source: IntelliJ Platform SDK docs — Virtual Files: How do I get a virtual file? (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
