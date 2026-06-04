---
id: sdk.inspection-options.declarative-inspection-options.non-profile-inspection-options
title: Inspection Options: Non-Profile Inspection Options
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, non, profile, inspection, options]
---
Sometimes, inspections use options that are rendered in a non-standard way or are shared with other inspections or other IDE features.
Such a shared configuration can be implemented as a [persistent component](https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html) and not have a single owner.
It is still convenient to be able to configure these options from the inspection panel.

An example of such a case is the `Java | Probable bugs | Nullability problems | @NotNull/@Nullable problems` inspection, which contains the Configure Annotations… button that opens the Nullable/NotNull Configuration dialog.

Custom Swing controls can be provided by implementing
[CustomComponentExtensionWithSwingRenderer](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-api/src/com/intellij/codeInspection/ui/CustomComponentExtensionWithSwingRenderer.java)
and registering the implementation in the [com.intellij.inspectionCustomComponent](https://jb.gg/ipe?extensions=com.intellij.inspectionCustomComponent) extension point
.
Please note that this API is still in the experimental state and may be changed without preserving backward compatibility.

Example:
[JavaInspectionButtons](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-impl/src/com/intellij/codeInsight/options/JavaInspectionButtons.java)
providing buttons for configuring options in custom dialogs

