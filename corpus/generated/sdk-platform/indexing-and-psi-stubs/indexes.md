---
id: sdk.indexing-and-psi-stubs.indexes
title: Indexing and PSI Stubs: Indexes
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, indexes]
---
Part of `sdk.indexing-and-psi-stubs`.

The indexing framework provides a quick way to locate specific elements, e.g., files containing a certain word or methods with a particular name, in large codebases.
Plugin developers can use the existing indexes built by the IDE itself and build and use their own indexes.

It supports two main types of indexes:

* [File-Based Indexes](https://plugins.jetbrains.com/docs/intellij/file-based-indexes.html)

* [Stub Indexes](https://plugins.jetbrains.com/docs/intellij/stub-indexes.html)

File-based indexes are built directly over the content of files.
Stub indexes are built over serialized stub trees.
A stub tree for a source file is a subset of its [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html) tree, which contains only externally visible declarations and is serialized in a compact binary format.

Querying a file-based index gets you the set of files matching a specific condition.
Querying a stub index gets you the set of matching PSI elements.
Therefore, custom language plugin developers typically use [stub indexes](https://plugins.jetbrains.com/docs/intellij/stub-indexes.html) in their plugin implementations.

Tip:

[Index Viewer](https://plugins.jetbrains.com/plugin/13029-index-viewer/) plugin can be used to inspect indexes' contents and properties.

> Source: IntelliJ Platform SDK docs — Indexing and PSI Stubs: Indexes (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
