---
id: sdk.code-inspections.running-the-comparing-string-references-inspection-code-sample
title: Code Inspections: Running the Comparing String References Inspection Code Sample
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, running, comparing, string, references, inspection]
---
Part of `sdk.code-inspections`.

The [comparing_string_references_inspection](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/comparing_string_references_inspection) code sample adds a new inspection to the Java | Probable Bugs group in the [Inspections](https://www.jetbrains.com/help/idea/inspections-settings.html) configuration.

See [Code Samples](https://plugins.jetbrains.com/docs/intellij/code-samples.html) on how to set up and run the plugin.

### How does it work?

The plugin inspects your code opened in the IntelliJ IDEA editor.
The plugin highlights the code fragments where two `String` expressions are compared by `==` or `!=` and proposes to replace this code fragment with `.equals()`:

![Comparing String References inspection highlighting and quick fix](images/comparing_references.png)
In this example, the `str1` and `str2` are variables of the String type.
Invoking SDK: Use equals() will result in transforming expression to the form visible in the [preview](https://plugins.jetbrains.com/docs/intellij/code-intentions-preview.html) popup (code fragment on the right).

> Source: IntelliJ Platform SDK docs — Code Inspections: Running the Comparing String References Inspection Code Sample (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
