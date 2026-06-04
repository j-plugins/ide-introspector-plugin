---
id: sdk.psi-performance.avoid-expensive-methods-of-psielement
title: PSI Performance: Avoid Expensive Methods of `PsiElement
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, avoid, expensive, methods, psielement]
---
Part of `sdk.psi-performance`.

Avoid Expensive Methods of `PsiElement`

Avoid `PsiElement`'s methods, which are expensive with deep trees.

`getText()` traverses the whole tree under the given element and concatenates strings, consider using `textMatches()` instead.

`getTextRange()`, `getContainingFile()`, and `getProject()` traverse the tree up to the file, which can be long in very nested trees.
If you only need PSI element length, use `getTextLength()`.

`getContainingFile()` and `getProject()` often can be computed once per task and then stored in fields or passed via parameters.

Additionally, methods such as `getText()`, `getNode()`, or `getTextRange()` require the AST, and accessing it can be an expensive operation, as explained in the next section.

> Source: IntelliJ Platform SDK docs — PSI Performance: Avoid Expensive Methods of `PsiElement (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
