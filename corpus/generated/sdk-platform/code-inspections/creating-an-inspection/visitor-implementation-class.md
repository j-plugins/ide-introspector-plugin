---
id: sdk.code-inspections.creating-an-inspection.visitor-implementation-class
title: Code Inspections: Visitor Implementation Class
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, visitor, implementation, class]
---
The visitor class evaluates whether elements of the file's PSI tree are of interest to an inspection.

The `ComparingStringReferencesInspection.buildVisitor()` method creates an anonymous visitor class based on [JavaElementVisitor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/JavaElementVisitor.java) to traverse the PSI tree of the Java file being edited, inspecting for suspect syntax.
The anonymous class overrides `visitBinaryExpression()`, which checks if a `PsiBinaryExpression`'s operator is `==` or `!=`, and if both operand types are `String`.

