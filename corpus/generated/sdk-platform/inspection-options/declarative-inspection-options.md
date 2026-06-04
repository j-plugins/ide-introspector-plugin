---
id: sdk.inspection-options.declarative-inspection-options
title: Inspection Options: Declarative Inspection Options
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, declarative, inspection, options]
---
Declarative API allows:

* delegate component rendering to the platform and make all the inspection options UI consistent and compliant with the [UI guidelines](https://plugins.jetbrains.com/docs/intellij/ui-guidelines-welcome.html)

* optimize checking whether the inspection contains any options

* manipulate options in places other than inspection panels (e.g., in quick fixes)

* render options in contexts other than IntelliJ Platform-based IDEs

Providing the inspection options is achieved by implementing
[InspectionProfileEntry.getOptionsPane()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInspection/InspectionProfileEntry.java),
which returns an
[OptPane](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInspection/options/OptPane.java)
object describing available configuration possibilities.
Note that `InspectionProfileEntry` is a parent of inspection base classes like
[LocalInspectionTool](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInspection/LocalInspectionTool.java)
and
[GlobalInspectionTool](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInspection/GlobalInspectionTool.java).

Building the inspection options is achieved by using a DSL-like facade, which contains methods for creating option controls and binding them to the fields declared in an inspection class.

Building the options for `Java | Code style issues | 'size() == 0' can be replaced with 'isEmpty()'` is implemented as follows:

```JAVA
public OrderedSet<String> ignoredTypes = new OrderedSet<>();
public boolean ignoreNegations = false;

@Override
public @NotNull OptPane getOptionsPane() {
  return pane(
    stringList(
      "ignoredTypes",
      message("options.label.ignored.classes"),
      new JavaClassValidator()),
    checkbox(
      "ignoreNegations",
      message("size.replaceable.by.isempty.negation.ignore"))
  );
}
```

The above example builds a form with two options (see
[SizeReplaceableByIsEmptyInspection](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-impl-inspections/src/com/siyeh/ig/style/SizeReplaceableByIsEmptyInspection.java)
for the full implementation context):

* List of strings, which are validated for being Java classes. The provided list is bound to the `ignoredTypes` field in the inspection class.

* Checkbox, which value is bound to the boolean `ignoreNegations` field in the inspection class.

The `OptPane` class exposes methods for building fields of other types, e.g., number or dropdown fields.

Note that the bind identifiers passed as a first string argument of methods creating form controls contain injected references that resolve to the bound fields.
It enables resolving and other resolve-related features available, making it easy to rename fields and minimizing the risk of introducing typos resulting in bugs, as unresolved references will be highlighted as errors.

### Custom Options Binding Protocol (inspection-options/declarative-inspection-options/custom-options-binding-protocol.md)
### Non-Profile Inspection Options (inspection-options/declarative-inspection-options/non-profile-inspection-options.md)
