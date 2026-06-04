# Code Inspections

<tldr>
Product Help: [Code Inspection](https://www.jetbrains.com/idea/webhelp/code-inspection.html), [Creating Custom Inspections](https://www.jetbrains.com/idea/help/creating-custom-inspections.html)

UI Guidelines: [Inspections](https://plugins.jetbrains.com/docs/intellij/inspections.html)
</tldr>

The IntelliJ Platform provides tools designed for static code analysis called code inspections, which help the user maintain and clean up code without actually executing it.
Custom code inspections can be implemented as IntelliJ Platform plugins.
An example of the plugin approach is the [comparing_string_references_inspection](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/comparing_string_references_inspection) code sample.

See the [Inspections](https://plugins.jetbrains.com/docs/intellij/inspections.html) topic in UI Guidelines on naming, writing description, and message texts for inspections.

## Creating an Inspection Plugin

The [comparing_string_references_inspection](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/comparing_string_references_inspection) code sample adds a new inspection to the Java | Probable Bugs group in the [Inspections list](https://www.jetbrains.com/help/idea/inspections-settings.html).
The inspection reports when the `==` or `!=` operator is used between String expressions.

It illustrates the components for a custom inspection plugin:

* Describing an [inspection](#plugin-configuration-file) in the plugin configuration file.

* Implementing a [local inspection class](#inspection-implementation-java-class) to inspect Java code in the editor.

* Creating a [visitor](#visitor-implementation-class) to traverse the PSI tree of the Java file being edited, inspecting for problematic syntax.

* Implementing a [quick fix](#quick-fix-implementation) class to correct syntax problems by altering the PSI tree as needed. Quick fixes are displayed to the user like [intentions](https://plugins.jetbrains.com/docs/intellij/code-intentions.html).

* Writing an HTML [description](#inspection-description) of the inspection for display in the inspection preferences panel.

* Creating a [test](#inspection-test) for the implemented inspection and quick fix.

Although the code sample illustrates implementations of these components, it is often useful to see examples of inspections implemented in the [IntelliJ Platform](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/README.md) code base.
To identify a given inspection's implementation classes, try to find an inspection [by name](https://plugins.jetbrains.com/docs/intellij/explore-api.html#search-for-symbol-names) or [by UI texts](https://plugins.jetbrains.com/docs/intellij/explore-api.html#search-by-ui-text).
Consider also searching for existing implementations in [IntelliJ Platform Explorer](https://jb.gg/ipe?extensions=com.intellij.localInspection).

## Creating an Inspection (sdk.code-inspections.creating-an-inspection)
## Running the Comparing String References Inspection Code Sample

The [comparing_string_references_inspection](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/comparing_string_references_inspection) code sample adds a new inspection to the Java | Probable Bugs group in the [Inspections](https://www.jetbrains.com/help/idea/inspections-settings.html) configuration.

See [Code Samples](https://plugins.jetbrains.com/docs/intellij/code-samples.html) on how to set up and run the plugin.

### How does it work?

The plugin inspects your code opened in the IntelliJ IDEA editor.
The plugin highlights the code fragments where two `String` expressions are compared by `==` or `!=` and proposes to replace this code fragment with `.equals()`:

![Comparing String References inspection highlighting and quick fix](images/comparing_references.png)
In this example, the `str1` and `str2` are variables of the String type.
Invoking SDK: Use equals() will result in transforming expression to the form visible in the [preview](https://plugins.jetbrains.com/docs/intellij/code-intentions-preview.html) popup (code fragment on the right).
