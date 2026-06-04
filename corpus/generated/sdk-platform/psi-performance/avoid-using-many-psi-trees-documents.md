---
id: sdk.psi-performance.avoid-using-many-psi-trees-documents
title: PSI Performance: Avoid Using Many PSI Trees/Documents
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, avoid, using, many, psi, trees]
---
Part of `sdk.psi-performance`.

Avoid loading too many parsed trees or documents into memory at the same time.
Ideally, only AST nodes from files open in the editor should be present in the memory.
Everything else, even if it's necessary for resolve/highlighting purposes, can be accessed via PSI interfaces, but its implementations should [use stubs](https://plugins.jetbrains.com/docs/intellij/stub-indexes.html) underneath, which are less CPU- and memory-expensive.

If stubs don't suit your case well (e.g., the information you need is large and/or very rarely needed, or you're developing a plugin for a language whose PSI you don't control), you can create a [custom index or gist](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html).

To ensure you're not loading AST accidentally, you can use [AstLoadingFilter](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/util/AstLoadingFilter.java) in production and `PsiManagerEx.setAssertOnFileLoadingFilter()` in tests.

The same applies to documents: only the ones opened in editors should be loaded.
Usually, you shouldn't need document contents (as most information can be retrieved from PSI).
If you nevertheless need documents, consider saving the information you need to provide in a [custom index or gist](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html) to get it more cheaply later.
If you still need documents, then at least ensure you load them one by one and don't hold them on strong references to let GC free the memory as quickly as possible.

> Source: IntelliJ Platform SDK docs — PSI Performance: Avoid Using Many PSI Trees/Documents (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
