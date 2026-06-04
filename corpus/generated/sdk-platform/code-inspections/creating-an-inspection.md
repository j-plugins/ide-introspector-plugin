---
id: sdk.code-inspections.creating-an-inspection
title: Code Inspections: Creating an Inspection
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, creating, inspection]
---
Part of `sdk.code-inspections`.

The [comparing_string_references_inspection](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/comparing_string_references_inspection) code sample reports when the `==` or `!=` operators are used between String expressions.
The user can apply a quick fix to change `a==b` to `a.equals(b)`, or `a!=b` to `!a.equals(b)`.

The details of the `comparing_string_references_inspection` implementation illustrate the components of an inspection plugin.

## Subtopics

- Plugin Configuration File — `sdk.code-inspections.creating-an-inspection.plugin-configuration-file`
- Inspection Implementation Java Class — `sdk.code-inspections.creating-an-inspection.inspection-implementation-java-class`
- Visitor Implementation Class — `sdk.code-inspections.creating-an-inspection.visitor-implementation-class`
- Quick Fix Implementation — `sdk.code-inspections.creating-an-inspection.quick-fix-implementation`
- Inspection Description — `sdk.code-inspections.creating-an-inspection.inspection-description`
- Inspection Test — `sdk.code-inspections.creating-an-inspection.inspection-test`

> Source: IntelliJ Platform SDK docs — Code Inspections: Creating an Inspection (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
