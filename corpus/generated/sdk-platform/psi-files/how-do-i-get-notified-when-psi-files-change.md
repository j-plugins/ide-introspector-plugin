---
id: sdk.psi-files.how-do-i-get-notified-when-psi-files-change
title: PSI Files: How do I get notified when PSI files change?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, how, get, notified, when, psi]
---
`PsiManager.addPsiTreeChangeListener()` allows you to receive notifications about all changes to the PSI tree of a project.
Alternatively, register [PsiTreeChangeListener](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiTreeChangeListener.java)
in [com.intellij.psi.treeChangeListener](https://jb.gg/ipe?extensions=com.intellij.psi.treeChangeListener) extension point
.

Note:

Please see [PsiTreeChangeEvent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiTreeChangeEvent.java) Javadoc for common problems when dealing with PSI events.

