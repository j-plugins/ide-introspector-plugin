---
id: sdk.modifying-the-psi.whitespaces-and-imports
title: Modifying the PSI: Whitespaces and Imports
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, whitespaces, imports]
---
When working with PSI modification functions, do not create individual whitespace nodes (spaces or line breaks) from text.
Instead, all whitespace modifications are performed by the formatter, which follows the code style settings selected by the user.
Formatting is automatically performed at the end of every command and can be also performed manually with [CodeStyleManager.reformat(PsiElement)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/codeStyle/CodeStyleManager.java) if needed.

Also, when working with Java code (or with code in other languages with a similar import mechanism such as Groovy or Python), do not create imports manually.
Instead, use fully qualified names in generated code and then call [JavaCodeStyleManager.shortenClassReferences()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/codeStyle/JavaCodeStyleManager.java) (or the equivalent API for the code language).
This ensures that the imports are created according to the user's code style settings and inserted into the file's correct place.

