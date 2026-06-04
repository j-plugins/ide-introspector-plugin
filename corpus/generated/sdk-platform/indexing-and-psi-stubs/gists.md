---
id: sdk.indexing-and-psi-stubs.gists
title: Indexing and PSI Stubs: Gists
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, gists]
---
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

