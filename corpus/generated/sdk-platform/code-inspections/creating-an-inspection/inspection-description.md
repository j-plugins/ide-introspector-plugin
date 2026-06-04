---
id: sdk.code-inspections.creating-an-inspection.inspection-description
title: Code Inspections: Inspection Description
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, inspection, description]
---
The inspection description is an HTML file.
The description is displayed in the upper right panel of the Inspections settings dialog when an inspection is selected from the list.

See the [Inspections](https://plugins.jetbrains.com/docs/intellij/inspections.html) topic in UI Guidelines on important guidelines for writing the description.

Implicit in using [LocalInspectionTool](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInspection/LocalInspectionTool.java) in the class hierarchy of the inspection implementation means following some conventions.

* The inspection description file is expected to be located under `$RESOURCES_ROOT_DIRECTORY$/inspectionDescriptions/`.

* The name of the description file is expected to be the inspection `$SHORT_NAME$.html` as provided by the inspection description, or the inspection implementation class. If a short name is not provided, the IntelliJ Platform computes one by removing `Inspection` suffix from the implementation class name.

Warning:

If a plugin project is multi-module, and it combines resources into a single JAR, make sure that all inspection description files have unique names or paths.
Otherwise, only the last packed description file will exist in the distribution package.

Tip:

See the [Bundled Translations](https://plugins.jetbrains.com/docs/intellij/providing-translations.html#bundled-translations) section for information about how to provide inspection description translations in plugins.

#### Code Snippets (code-inspections/creating-an-inspection/inspection-description/code-snippets.md)
#### Settings Link (code-inspections/creating-an-inspection/inspection-description/settings-link.md)
