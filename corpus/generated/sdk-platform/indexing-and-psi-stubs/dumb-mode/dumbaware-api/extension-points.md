---
id: sdk.indexing-and-psi-stubs.dumb-mode.dumbaware-api.extension-points
title: Indexing and PSI Stubs: Extension Points
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, extension, points]
---
Implementations of certain [extension points](https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html) can be marked as available during Dumb Mode by implementing
[DumbAware](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/DumbAware.java).
Such extension points are marked with the
![DumbAware](https://img.shields.io/badge/-DumbAware-darkgreen?style=flat-square)
tag in [IntelliJ Platform Extension Point and Listener List](https://plugins.jetbrains.com/docs/intellij/intellij-platform-extension-point-list.html).

Commonly used extension points include [CompletionContributor](https://plugins.jetbrains.com/docs/intellij/code-completion.html), [(External)Annotator](https://plugins.jetbrains.com/docs/intellij/syntax-highlighting-and-error-highlighting.html#annotator) and various
[run configuration](https://plugins.jetbrains.com/docs/intellij/run-configurations.html) EPs.
Since 2024.2, this includes also [intentions](https://plugins.jetbrains.com/docs/intellij/code-intentions.html) and [quick-fixes](https://plugins.jetbrains.com/docs/intellij/quick-fix.html).

