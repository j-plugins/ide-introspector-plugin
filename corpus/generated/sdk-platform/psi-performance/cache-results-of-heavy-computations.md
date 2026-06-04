---
id: sdk.psi-performance.cache-results-of-heavy-computations
title: PSI Performance: Cache Results of Heavy Computations
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, cache, results, heavy, computations]
---
Method calls such as `PsiElement.getReference()` (and `getReferences()`), `PsiReference.resolve()` (and `multiResolve()` and other equivalents) or computation of expression types, type inference results, control flow graphs, etc. can be expensive.
To avoid paying this cost several times, the result of such computation can be cached and reused.
Usually, [CachedValue](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/util/CachedValue.java) created with [CachedValuesManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/util/CachedValuesManager.java) works well for this purpose.

If the information you cache depends only on a subtree of the current PSI element (and nothing else: no resolve results or other files), you can cache it in a field in your `PsiElement` implementation and drop the cache in an override of `ASTDelegatePsiElement.subtreeChanged()`.

### Using `ProjectRootManager` as a Dependency (psi-performance/cache-results-of-heavy-computations/using-projectrootmanager-as-a-dependency.md)
