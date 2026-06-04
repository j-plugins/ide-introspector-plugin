---
id: sdk.psi-performance.cache-results-of-heavy-computations
title: PSI Performance: Cache Results of Heavy Computations
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, cache, results, heavy, computations]
---
Part of `sdk.psi-performance`.

Method calls such as `PsiElement.getReference()` (and `getReferences()`), `PsiReference.resolve()` (and `multiResolve()` and other equivalents) or computation of expression types, type inference results, control flow graphs, etc. can be expensive.
To avoid paying this cost several times, the result of such computation can be cached and reused.
Usually, [CachedValue](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/util/CachedValue.java) created with [CachedValuesManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/util/CachedValuesManager.java) works well for this purpose.

If the information you cache depends only on a subtree of the current PSI element (and nothing else: no resolve results or other files), you can cache it in a field in your `PsiElement` implementation and drop the cache in an override of `ASTDelegatePsiElement.subtreeChanged()`.

### 

Using `ProjectRootManager` as a Dependency

The platform no longer increments root changes modification tracker on finish of [dumb mode](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html#dumb-mode).
If cached values use [ProjectRootManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/roots/ProjectRootManager.java) as a dependency
(without [PsiModificationTracker](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/util/PsiModificationTracker.java))
and at the same time depend on [indexes](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html), a dependency on
[DumbService](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/DumbService.kt) must be added.

> Source: IntelliJ Platform SDK docs — PSI Performance: Cache Results of Heavy Computations (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
