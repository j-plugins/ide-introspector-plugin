# Creating an Inspection

The [comparing_string_references_inspection](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/comparing_string_references_inspection) code sample reports when the `==` or `!=` operators are used between String expressions.
The user can apply a quick fix to change `a==b` to `a.equals(b)`, or `a!=b` to `!a.equals(b)`.

The details of the `comparing_string_references_inspection` implementation illustrate the components of an inspection plugin.

### Plugin Configuration File

The `comparing_string_references_inspection` is described as a [com.intellij.localInspection](https://jb.gg/ipe?extensions=com.intellij.localInspection) extension point
in the `comparing_string_references_inspection` plugin configuration ([plugin.xml](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/comparing_string_references_inspection/src/main/resources/META-INF/plugin.xml)) file.

There exist two types of inspection extensions:

* The [com.intellij.localInspection](https://jb.gg/ipe?extensions=com.intellij.localInspection) extension point is used for inspections that operate on one file at a time, and also operate "on-the-fly" as the user edits the file.

* The [com.intellij.globalInspection](https://jb.gg/ipe?extensions=com.intellij.globalInspection) extension point is used for inspections that operate across multiple files, and the associated fix might, for example, refactor code between files.

The minimum inspection setup must declare the `implementationClass` and `language` attribute (unless the inspection works on any supported language).
As shown in the `comparing_string_references_inspection` plugin configuration file, other attributes can be defined in the `localInspection` element, either with or without localization.
In most cases, it is simplest to define the attributes in the plugin configuration file because the underlying parent classes handle most of the class responsibilities based on the configuration file description.

If required, inspections can define all the attribute information (except `implementationClass`) by overriding methods in the inspection implementation class (not recommended in general).

### Inspection Implementation Java Class

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

### Visitor Implementation Class

The visitor class evaluates whether elements of the file's PSI tree are of interest to an inspection.

The `ComparingStringReferencesInspection.buildVisitor()` method creates an anonymous visitor class based on [JavaElementVisitor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/JavaElementVisitor.java) to traverse the PSI tree of the Java file being edited, inspecting for suspect syntax.
The anonymous class overrides `visitBinaryExpression()`, which checks if a `PsiBinaryExpression`'s operator is `==` or `!=`, and if both operand types are `String`.

### Quick Fix Implementation

The quick fix class acts much like an intention, allowing the user to invoke it on the `PsiElement` (or `TextRange`) highlighted by the inspection.

The `ComparingStringReferencesInspection` implementation uses the nested class `ReplaceWithEqualsQuickFix` to implement a quick fix based on [LocalQuickFix](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInspection/LocalQuickFix.java).
The `ReplaceWithEqualsQuickFix` class allows the user to change the use of `a == b` and `a != b` expression to `a.equals(b)` and `!a.equals(b)` respectively.

The heavy lifting is done in `ReplaceWithEqualsQuickFix.applyFix()`, which manipulates the PSI tree to convert the expressions.
The change to the PSI tree is accomplished by the usual approach to modification:

* Getting a `PsiElementFactory`.

* Creating a new `PsiMethodCallExpression`.

* Substituting the original left and right operands into the new `PsiMethodCallExpression`.

* Replacing the original binary expression with the `PsiMethodCallExpression`.

Note:

In case of providing multiple quick fixes for a single element, their ordering is indeterministic due to performance reasons.
It is possible to push specific items up or down by implementing
[HighPriorityAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInsight/intention/HighPriorityAction.java)
or
[LowPriorityAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInsight/intention/LowPriorityAction.java)
respectively.

### Inspection Description (sdk.code-inspections.creating-an-inspection.inspection-description)
### Inspection Test

Note:

Please note that running the test requires setting system property `idea.home.path` in the `test` task configuration of the Gradle build script.

The `comparing_string_references_inspection` code sample provides a test for the inspection.
See the [Testing Overview](https://plugins.jetbrains.com/docs/intellij/testing-plugins.html) section for general information about plugin testing.

The `comparing_string_references_inspection` test is based on the [UsefulTestCase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/testFramework/src/com/intellij/testFramework/UsefulTestCase.java) class, part of the JUnit framework APIs.
This class handles much of the underlying boilerplate for tests.

By convention, the folder `test/testData/` contains the test files.
The folder contains pairs of files for each test using the name convention `∗.java` and `∗.after.java`, e.g., `Eq.java` / `Eq.after.java`.

The `comparing_string_references_inspection` tests run the inspection on the `∗.java` files, apply the quick fix, and compare the results with the respective `∗.after.java` files containing expected results.
