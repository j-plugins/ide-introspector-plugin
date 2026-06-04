---
id: sdk.code-inspections.creating-an-inspection.inspection-implementation-java-class
title: Code Inspections: Inspection Implementation Java Class
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, inspection, implementation, java, class]
---
Part of `sdk.code-inspections.creating-an-inspection`.

Inspection implementations for Java files, like [ComparingStringReferencesInspection](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/comparing_string_references_inspection/src/main/java/org/intellij/sdk/codeInspection/ComparingStringReferencesInspection.java), are often based on the Java class [AbstractBaseJavaLocalInspectionTool](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-analysis-api/src/com/intellij/codeInspection/AbstractBaseJavaLocalInspectionTool.java).
The [AbstractBaseJavaLocalInspectionTool](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-analysis-api/src/com/intellij/codeInspection/AbstractBaseJavaLocalInspectionTool.java) base class offers methods to inspect Java classes, fields, and methods.

More generally, `localInspection` types are based on the class [LocalInspectionTool](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInspection/LocalInspectionTool.java).
Examining the class hierarchy for `LocalInspectionTool` shows that the IntelliJ Platform provides many child inspection classes for a variety of languages and frameworks.
One of these classes is a good basis for a new inspection implementation, but a bespoke implementation can also be based directly on `LocalInspectionTool`.

The primary responsibilities of the inspection implementation class are to provide:

* A `PsiElementVisitor` object to traverse the PSI tree of the file being inspected.

* A `LocalQuickFix` class to fix an identified problem (optional).

* An options panel to be displayed in the Inspections settings dialog (optional). See [Inspection Options](https://plugins.jetbrains.com/docs/intellij/inspection-options.html) for more details.

The overridden `ComparingStringReferencesInspection` methods are discussed in the sections below.

> Source: IntelliJ Platform SDK docs — Code Inspections: Inspection Implementation Java Class (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
