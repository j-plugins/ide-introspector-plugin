---
id: sdk.psi-references.contributed-references
title: PSI References: Contributed References
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, contributed, references]
---
In addition to references defined by the semantics of the programming language, the IDE recognizes many references determined by the semantics of the APIs and frameworks used in code.
Consider the following example:

```JAVA
File file = new File("foo.txt");
```

Here, "foo.txt" has no special meaning from the point of view of the Java syntax - it's just a string literal.
However, opening this example in IntelliJ IDEA and having a file called "foo.txt" in the same directory, one can `Ctrl/Cmd+Click` on "foo.txt" and navigate to the file.
This works because the IDE recognizes the semantics of `new File(...)` and contributes a reference into the string literal passed as a parameter to the method.

Typically, references can be contributed to elements that don't have their own references, such as string literals and comments.
References are also often contributed to non-code files, such as XML or JSON.

Contributing references is one of the most common ways to extend an existing language.
For example, your plugin can contribute references to Java code, even though the Java PSI is part of the platform and not defined in your plugin.

Implement [PsiReferenceContributor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiReferenceContributor.java) registered in [com.intellij.psi.referenceContributor](https://jb.gg/ipe?extensions=com.intellij.psi.referenceContributor) extension point
.

Attribute `language` should be set to the Language ID where this contributor applies to.
The exact places to contribute references to are then specified using [Element Patterns](https://plugins.jetbrains.com/docs/intellij/element-patterns.html) in calls to `PsiReferenceRegistrar.registerReferenceProvider()`.

See also [Reference Contributor tutorial](https://plugins.jetbrains.com/docs/intellij/reference-contributor.html).

