---
id: sdk.inspection-options.ui-based-inspection-options
title: Inspection Options: UI-Based Inspection Options
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, based, inspection, options]
---
Part of `sdk.inspection-options`.

Tip:

If you target versions 2023.1+ only, it is highly recommended to implement [Declarative Inspection Options](#declarative-inspection-options).

UI-based inspection options are provided by implementing a configuration panel using Swing components and returning it from [InspectionProfileEntry.createOptionsPanel()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInspection/InspectionProfileEntry.java).
It returns the panel with option components that bind the provided values to the inspection class fields or other properties, similarly as in the [declarative](#declarative-inspection-options) approach.
Note that since version 2023.1, this method is ignored if `InspectionProfileEntry.getOptionPane()` returns a non-empty panel.

Example:
[SizeReplaceableByIsEmptyInspection](https://github.com/JetBrains/intellij-community/tree/223/plugins/InspectionGadgets/src/com/siyeh/ig/style/SizeReplaceableByIsEmptyInspection.java)
in version 2022.3, implemented using the UI-approach

For simple customization requirements, see also:

* [SingleCheckboxOptionsPanel](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-api/src/com/intellij/codeInspection/ui/SingleCheckboxOptionsPanel.java) for single checkbox

* [MultipleCheckboxOptionsPanel](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-api/src/com/intellij/codeInspection/ui/MultipleCheckboxOptionsPanel.java) for multiple checkboxes

* [SingleIntegerFieldOptionsPanel](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-api/src/com/intellij/codeInspection/ui/SingleIntegerFieldOptionsPanel.java) for a single Integer (text field)

Warning:

Be careful when you have a hierarchy of inspection classes.
For example, if an inspection superclass is converted to the declarative approach, any `createOptionsPanel()` methods in subclasses will be ignored.
If you can't convert all of them at once, you may temporarily add `getOptionsPane()` returning `OptPane.EMPTY` to subclasses, where `createOptionsPanel()` is still used.

> Source: IntelliJ Platform SDK docs — Inspection Options: UI-Based Inspection Options (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
