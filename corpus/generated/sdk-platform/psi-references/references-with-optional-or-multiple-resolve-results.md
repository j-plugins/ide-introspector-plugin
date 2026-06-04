---
id: sdk.psi-references.references-with-optional-or-multiple-resolve-results
title: PSI References: References with Optional or Multiple Resolve Results
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, references, with, optional, multiple, resolve]
---
In the simplest case, a reference resolves to a single element, and if resolving fails, the code is incorrect, and the IDE needs to highlight it as an error.
However, there are cases when the situation is different.

The first case is soft references.
Consider the `new File("foo.txt")` example above.
If the IDE can't find the file "foo.txt", it doesn't mean that an error needs to be highlighted - maybe the file is only available at runtime.
Such references return `true` from the `PsiReference.isSoft()` method, which can then be used in inspection/annotator to skip highlighting them completely or use a lower severity.

The second case is polyvariant references.
Consider the case of a JavaScript program.
JavaScript is a dynamically typed language, so the IDE cannot always precisely determine which method is being called at a particular location.
To handle this, it provides a reference that can be resolved to multiple possible elements.
Such references implement the [PsiPolyVariantReference](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiPolyVariantReference.java) interface.

For resolving a `PsiPolyVariantReference`, you call its `multiResolve()` method.
The call returns an array of [ResolveResult](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/ResolveResult.java) objects.
Each of the objects identifies a PSI element and also specifies whether the result is valid.
For example, suppose you have multiple Java method overloads and a call with arguments not matching any of the overloads.
In that case, you will get back `ResolveResult` objects for all the overloads, and `isValidResult()` returns `false` for all of them.

