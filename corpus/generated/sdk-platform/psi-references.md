---
id: sdk.psi-references
title: PSI References
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, psi, references]
---
A reference in a PSI tree is an object that represents a link from a usage of a particular element in the code to the corresponding declaration. Resolving a reference means locating the declaration to which a specific usage refers.

The most common type of reference is defined by language semantics.
For example, consider a simple Java method:

```JAVA
public void hello(String message) {
  System.out.println(message);
}
```

This simple code fragment contains five references.
The references created by the identifiers `String`, `System`, `out`, and `println` can be resolved to the corresponding declarations in the JDK: the `String` and `System` classes, the `out` field, and the `println` method.
The reference created by the second occurrence of the `message` identifier in `println(message)` can be resolved to the `message` parameter, declared by `String message` in the method header.

Note that `String message` is not a reference and cannot be resolved.
Instead, it's a declaration.
It does not refer to any name defined elsewhere; instead, it defines a name by itself.

A reference is an instance of a class implementing the [PsiReference](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiReference.java) interface.
Note that references are distinct from PSI elements.
References created by a PSI element are returned from `PsiElement.getReferences()`, the underlying PSI element of a reference can be obtained from `PsiReference.getElement()`.

To resolve the reference - to locate the declaration being referenced - call `PsiReference.resolve()`.
It's very important to understand the difference between `PsiReference.getElement()` and `PsiReference.resolve()`.
The former method returns the source of a reference, while the latter returns its target.
In the example above, for the `message` reference, `getElement()` will return the `message` identifier on the second line of the snippet, and `resolve()` will return the `message` identifier on the first line (inside the parameter list).

The process of resolving references is distinct from parsing and is not performed at the same time.
Moreover, it is not always successful.
If the code currently open in the IDE does not compile, or in other situations, it's normal for `PsiReference.resolve()` to return `null` - all code working with references must be prepared to handle that.

Tip:

Please see also [Cache Results of Heavy Computations](https://plugins.jetbrains.com/docs/intellij/psi-performance.html#cache-results-of-heavy-computations).

## Subtopics

- Contributed References — `sdk.psi-references.contributed-references`
- References with Optional or Multiple Resolve Results — `sdk.psi-references.references-with-optional-or-multiple-resolve-results`
- Searching for References — `sdk.psi-references.searching-for-references`
- Implementing References — `sdk.psi-references.implementing-references`

> Source: IntelliJ Platform SDK docs — PSI References (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
