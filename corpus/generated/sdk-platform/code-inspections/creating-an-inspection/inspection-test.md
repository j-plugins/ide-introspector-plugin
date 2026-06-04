---
id: sdk.code-inspections.creating-an-inspection.inspection-test
title: Code Inspections: Inspection Test
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, inspection, test]
---
Note:

Please note that running the test requires setting system property `idea.home.path` in the `test` task configuration of the Gradle build script.

The `comparing_string_references_inspection` code sample provides a test for the inspection.
See the [Testing Overview](https://plugins.jetbrains.com/docs/intellij/testing-plugins.html) section for general information about plugin testing.

The `comparing_string_references_inspection` test is based on the [UsefulTestCase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/testFramework/src/com/intellij/testFramework/UsefulTestCase.java) class, part of the JUnit framework APIs.
This class handles much of the underlying boilerplate for tests.

By convention, the folder `test/testData/` contains the test files.
The folder contains pairs of files for each test using the name convention `∗.java` and `∗.after.java`, e.g., `Eq.java` / `Eq.after.java`.

The `comparing_string_references_inspection` tests run the inspection on the `∗.java` files, apply the quick fix, and compare the results with the respective `∗.after.java` files containing expected results.

