---
id: sdk.psi-files.where-does-a-psi-file-come-from
title: PSI Files: Where does a PSI file come from?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, where, does, psi, file, come]
---
Part of `sdk.psi-files`.

As PSI is language-dependent, PSI files are created using the [Language](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/lang/Language.java) instance:

```JAVA
LanguageParserDefinitions.INSTANCE
    .forLanguage(MyLanguage.INSTANCE)
    .createFile(fileViewProvider);
```

Like [Documents](https://plugins.jetbrains.com/docs/intellij/documents.html), PSI files are created on-demand when the PSI is accessed for a particular file.

> Source: IntelliJ Platform SDK docs — PSI Files: Where does a PSI file come from? (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
