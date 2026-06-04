---
id: sdk.psi-files.what-can-i-do-with-a-psi-file
title: PSI Files: What can I do with a PSI file?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, what, can, with, psi, file]
---
Part of `sdk.psi-files`.

Most interesting modification operations are performed on the level of individual PSI elements, not files as a whole.

To iterate over the elements in a file, use

```JAVA
psiFile.accept(new PsiRecursiveElementWalkingVisitor() {
  // visitor implementation ...
});
```

See also [Navigating the PSI](https://plugins.jetbrains.com/docs/intellij/navigating-psi.html).

> Source: IntelliJ Platform SDK docs — PSI Files: What can I do with a PSI file? (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
