---
id: sdk.modifying-the-psi.maintaining-tree-structure-consistency
title: Modifying the PSI: Maintaining Tree Structure Consistency
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, maintaining, tree, structure, consistency]
---
Part of `sdk.modifying-the-psi`.

The PSI modification methods do not restrict the way of building the resulting tree structure.

For example, when working with a Java class, it is possible to add a `for` statement as a direct child of a `PsiMethod` element, even though the Java parser will never produce such a structure (the `for` statement will always be a child of the `PsiCodeBlock`) representing the method body.

Modifications that produce incorrect tree structures may appear to work, but they will lead to problems and exceptions later.
Therefore, always ensure that the structure built with PSI modification operations is the same as what the parser would produce when parsing the created code.

To make sure inconsistencies are not introduced, use `PsiTestUtil.checkFileStructure()` in the tests for actions modifying the PSI.
This method ensures that the built structure is the same as what the parser produces.

> Source: IntelliJ Platform SDK docs — Modifying the PSI: Maintaining Tree Structure Consistency (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
