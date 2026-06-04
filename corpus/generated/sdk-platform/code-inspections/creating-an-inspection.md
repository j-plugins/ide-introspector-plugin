---
id: sdk.code-inspections.creating-an-inspection
title: Code Inspections: Creating an Inspection
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, creating, inspection]
---
The [comparing_string_references_inspection](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/comparing_string_references_inspection) code sample reports when the `==` or `!=` operators are used between String expressions.
The user can apply a quick fix to change `a==b` to `a.equals(b)`, or `a!=b` to `!a.equals(b)`.

The details of the `comparing_string_references_inspection` implementation illustrate the components of an inspection plugin.

### Plugin Configuration File (code-inspections/creating-an-inspection/plugin-configuration-file.md)
### Inspection Implementation Java Class (code-inspections/creating-an-inspection/inspection-implementation-java-class.md)
### Visitor Implementation Class (code-inspections/creating-an-inspection/visitor-implementation-class.md)
### Quick Fix Implementation (code-inspections/creating-an-inspection/quick-fix-implementation.md)
### Inspection Description (code-inspections/creating-an-inspection/inspection-description.md)
#### Code Snippets (code-inspections/creating-an-inspection/inspection-description/code-snippets.md)
#### Settings Link (code-inspections/creating-an-inspection/inspection-description/settings-link.md)
### Inspection Test (code-inspections/creating-an-inspection/inspection-test.md)
