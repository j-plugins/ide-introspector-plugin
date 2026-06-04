---
id: sdk.code-inspections.running-the-comparing-string-references-inspection-code-sample.how-does-it-work
title: Code Inspections: How does it work?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, how, does, work]
---
The plugin inspects your code opened in the IntelliJ IDEA editor.
The plugin highlights the code fragments where two `String` expressions are compared by `==` or `!=` and proposes to replace this code fragment with `.equals()`:

![Comparing String References inspection highlighting and quick fix](images/comparing_references.png)
In this example, the `str1` and `str2` are variables of the String type.
Invoking SDK: Use equals() will result in transforming expression to the form visible in the [preview](https://plugins.jetbrains.com/docs/intellij/code-intentions-preview.html) popup (code fragment on the right).

