# Indexing and PSI Stubs

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

## Dumb Mode (sdk.indexing-and-psi-stubs.dumb-mode)
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

## Improving Indexing Performance (sdk.indexing-and-psi-stubs.improving-indexing-performance)
