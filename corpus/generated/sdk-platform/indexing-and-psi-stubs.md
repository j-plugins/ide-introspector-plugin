---
id: sdk.indexing-and-psi-stubs
title: Indexing and PSI Stubs
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, indexing, psi, stubs]
---
## Indexes

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

## Dumb Mode

Indexing is a potentially lengthy process.
It's performed in the background, and during this time, all IDE features are restricted to the ones that don't require indexes: basic text editing, version control, etc.
This restriction is managed by [DumbService](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/DumbService.kt).
Violations are reported via [IndexNotReadyException](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/IndexNotReadyException.java), see its documentation for information on how to adapt callers.

`DumbService` provides API to query whether the IDE is currently in "dumb" mode (where index access is not allowed) or "smart" mode (with all index built and ready to use).
It also provides ways of delaying code execution until indexes are ready.

[Video](https://www.youtube.com/v/ApdNfPuGJRU)
Learn how techniques like dumb mode index access, on-demand indexing, and lightweight heuristics can boost plugin performance and streamline your development process,
all while maintaining robust coding assistance.

### 

`DumbAware` API

Tip: Finding Candidates

Use inspection Plugin DevKit | Code | Can be DumbAware (2025.1+) to find implementations
that can potentially be marked as `DumbAware`.

#### Extension Points

Implementations of certain [extension points](https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html) can be marked as available during Dumb Mode by implementing
[DumbAware](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/DumbAware.java).
Such extension points are marked with the
![DumbAware](https://img.shields.io/badge/-DumbAware-darkgreen?style=flat-square)
tag in [IntelliJ Platform Extension Point and Listener List](https://plugins.jetbrains.com/docs/intellij/intellij-platform-extension-point-list.html).

Commonly used extension points include [CompletionContributor](https://plugins.jetbrains.com/docs/intellij/code-completion.html), [(External)Annotator](https://plugins.jetbrains.com/docs/intellij/syntax-highlighting-and-error-highlighting.html#annotator) and various
[run configuration](https://plugins.jetbrains.com/docs/intellij/run-configurations.html) EPs.
Since 2024.2, this includes also [intentions](https://plugins.jetbrains.com/docs/intellij/code-intentions.html) and [quick-fixes](https://plugins.jetbrains.com/docs/intellij/quick-fix.html).

#### Actions

For [actions](https://plugins.jetbrains.com/docs/intellij/action-system.html) available during Dumb Mode, extend [DumbAwareAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/project/DumbAwareAction.java) (do not override `AnAction.isDumbAware()` instead).

#### Other API

Other API might indicate its Dumb Mode compatibility by extending [PossiblyDumbAware](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/PossiblyDumbAware.java).

### Testing

To toggle Dumb Mode for testing purposes, invoke `Tools | Internal Actions | Enter/Exit Dumb Mode`
while the IDE is running in [internal mode](https://plugins.jetbrains.com/docs/intellij/enabling-internal.html).

## Gists

Sometimes, the following conditions hold:

* The aggregation functionality of file-based indexes is not needed. One just needs to calculate some data based on a particular file's contents and cache it on disk.

* Eagerly calculating the data for the entire project during indexing isn't needed (e.g., it slows down the indexing, and/or this data probably will ever be required for a minor subset of all project files).

* The data can be recalculated lazily on request without significant performance penalties.

A [file-based index](https://plugins.jetbrains.com/docs/intellij/file-based-indexes.html) can be used in such cases, but file gists provide a way to perform data calculation lazily, caching on disk, and a more lightweight API.
Please see [VirtualFileGist](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/indexing-api/src/com/intellij/util/gist/VirtualFileGist.java) and [PsiFileGist](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/indexing-api/src/com/intellij/util/gist/PsiFileGist.java) documentation.

Tip:

Note performance implications noted in [VirtualFileGist](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/indexing-api/src/com/intellij/util/gist/VirtualFileGist.java) Javadoc.

Example:

* `VirtualFileGist`: [ImageInfoIndex](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/images/src/org/intellij/images/index/ImageInfoIndex.java) calculating image dimensions/bit depth needed to be displayed in specific parts of UI.

* `PsiFileGist`: [JavaSimplePropertyGist](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-indexing-impl/src/com/intellij/psi/impl/JavaSimplePropertyGist.kt) providing simple properties in Java

## Improving Indexing Performance

### Performance Metrics

Indexing performance metrics in JSON format are generated in [logs directory](https://intellij-support.jetbrains.com/hc/en-us/articles/206544519-Directories-used-by-the-IDE-to-store-settings-caches-plugins-and-logs) (see [sandbox directory](https://plugins.jetbrains.com/docs/intellij/ide-development-instance.html#the-development-instance-sandbox-directory) for development instance).
These are additionally available in HTML format starting with 2021.1.

### Avoid Using AST

Use [lexer](https://plugins.jetbrains.com/docs/intellij/implementing-lexer.html) information instead of parsed trees if possible.

If impossible, use light AST which doesn't create memory-hungry AST nodes inside, so traversing it might be faster.
Obtain [LighterAST](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/lang/LighterAST.java) by casting `FileContent` input parameter to [PsiDependentFileContent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/util/indexing/PsiDependentFileContent.java) and calling `getLighterAST()`.
Make sure to traverse only the nodes you need to.
See also [RecursiveLighterASTNodeWalkingVisitor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-impl/src/com/intellij/psi/impl/source/tree/RecursiveLighterASTNodeWalkingVisitor.java) and [LightTreeUtil](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-impl/src/com/intellij/psi/impl/source/tree/LightTreeUtil.java) for useful utility methods.

For [stub index](https://plugins.jetbrains.com/docs/intellij/stub-indexes.html), implement [LightStubBuilder](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-impl/src/com/intellij/psi/stubs/LightStubBuilder.java).

If a custom language contains lazy-parseable elements that never or rarely contain any stubs, consider implementing [StubBuilder.skipChildProcessingWhenBuildingStubs()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/StubBuilder.java) (preferably using Lexer/node text).

For indexing XML, also consider using [NanoXmlUtil](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/indexing-impl/src/com/intellij/util/xml/NanoXmlUtil.java).

### Shared Project Indexes

For bigger projects, building and providing pre-built shared project indexes can be beneficial, see [Shared project indexes](https://www.jetbrains.com/help/idea/shared-indexes.html#project-shared-indexes).
See also [IntelliJ Shared Indexes Tool Example](https://github.com/JetBrains/intellij-shared-indexes-tool-example).

> Source: IntelliJ Platform SDK docs — Indexing and PSI Stubs (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
