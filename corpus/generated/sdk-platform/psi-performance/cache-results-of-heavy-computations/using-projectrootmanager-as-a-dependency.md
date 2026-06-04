---
id: sdk.psi-performance.cache-results-of-heavy-computations.using-projectrootmanager-as-a-dependency
title: PSI Performance: Using `ProjectRootManager` as a Dependency
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, using, projectrootmanager, dependency]
---
Using `ProjectRootManager` as a Dependency

The platform no longer increments root changes modification tracker on finish of [dumb mode](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html#dumb-mode).
If cached values use [ProjectRootManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/roots/ProjectRootManager.java) as a dependency
(without [PsiModificationTracker](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/util/PsiModificationTracker.java))
and at the same time depend on [indexes](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html), a dependency on
[DumbService](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/DumbService.kt) must be added.

