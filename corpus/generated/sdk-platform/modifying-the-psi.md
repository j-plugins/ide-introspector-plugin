---
id: sdk.modifying-the-psi
title: Modifying the PSI
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, modifying, psi]
---
The PSI is a read/write representation of the source code as a tree of elements corresponding to a source file's structure.
The PSI can be modified by adding, replacing, and deleting PSI elements.

To perform these operations, use methods such as `PsiElement.add()`, `PsiElement.delete()`, `PsiElement.replace()`, and similar methods allowing to process multiple elements in a single operation, or to specify the exact location in the tree where an element needs to be added.

Like document operations, PSI modifications need to be wrapped in a write action and in command (and can only be performed in the event dispatch thread).
See [the Documents article](https://plugins.jetbrains.com/docs/intellij/documents.html#what-are-the-rules-of-working-with-documents) for more information on commands and write actions.

## Subtopics

- Creating the New PSI — `sdk.modifying-the-psi.creating-the-new-psi`
- Maintaining Tree Structure Consistency — `sdk.modifying-the-psi.maintaining-tree-structure-consistency`
- Whitespaces and Imports — `sdk.modifying-the-psi.whitespaces-and-imports`
- Combining PSI and Document Modifications — `sdk.modifying-the-psi.combining-psi-and-document-modifications`

> Source: IntelliJ Platform SDK docs — Modifying the PSI (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
