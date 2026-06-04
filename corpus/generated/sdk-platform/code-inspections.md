---
id: sdk.code-inspections
title: Code Inspections
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, code, inspections]
---
<tldr>
Product Help: [Code Inspection](https://www.jetbrains.com/idea/webhelp/code-inspection.html), [Creating Custom Inspections](https://www.jetbrains.com/idea/help/creating-custom-inspections.html)

UI Guidelines: [Inspections](https://plugins.jetbrains.com/docs/intellij/inspections.html)
</tldr>

The IntelliJ Platform provides tools designed for static code analysis called code inspections, which help the user maintain and clean up code without actually executing it.
Custom code inspections can be implemented as IntelliJ Platform plugins.
An example of the plugin approach is the [comparing_string_references_inspection](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/comparing_string_references_inspection) code sample.

See the [Inspections](https://plugins.jetbrains.com/docs/intellij/inspections.html) topic in UI Guidelines on naming, writing description, and message texts for inspections.

## Creating an Inspection Plugin (code-inspections/creating-an-inspection-plugin.md)
## Creating an Inspection (code-inspections/creating-an-inspection.md)
### Plugin Configuration File (code-inspections/creating-an-inspection/plugin-configuration-file.md)
### Inspection Implementation Java Class (code-inspections/creating-an-inspection/inspection-implementation-java-class.md)
### Visitor Implementation Class (code-inspections/creating-an-inspection/visitor-implementation-class.md)
### Quick Fix Implementation (code-inspections/creating-an-inspection/quick-fix-implementation.md)
### Inspection Description (code-inspections/creating-an-inspection/inspection-description.md)
#### Code Snippets (code-inspections/creating-an-inspection/inspection-description/code-snippets.md)
#### Settings Link (code-inspections/creating-an-inspection/inspection-description/settings-link.md)
### Inspection Test (code-inspections/creating-an-inspection/inspection-test.md)
## Running the Comparing String References Inspection Code Sample (code-inspections/running-the-comparing-string-references-inspection-code-sample.md)
### How does it work? (code-inspections/running-the-comparing-string-references-inspection-code-sample/how-does-it-work.md)

> Source: IntelliJ Platform SDK docs — Code Inspections (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
