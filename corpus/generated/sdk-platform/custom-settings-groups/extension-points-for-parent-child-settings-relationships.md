---
id: sdk.custom-settings-groups.extension-points-for-parent-child-settings-relationships
title: Custom Settings Groups: Extension Points for Parent-Child Settings Relationships
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, extension, points, for, parent, child]
---
There are multiple ways of creating parent-child relationships in groups of Settings: in implementations, or Extension Point declarations.
However, there are performance penalties for creating these relationships in implementations because the objects must be instantiated to determine the relationships.
This section describes the syntax for declaring more complex parent-child relationships in `com.intellij.projectConfigurable` or `com.intellij.applicationConfigurable` EPs.

Note:

An application configurable can be a parent of a project configurable.

There are two ways of declaring parent-child relationships using the `com.intellij.projectConfigurable` EP or `com.intellij.applicationConfigurable` EP.
The first is to use separate EP declarations that are tied together by the value of one attribute.
The second method is to use nested declarations.

### Parent-Child Settings Using Separate EPs (custom-settings-groups/extension-points-for-parent-child-settings-relationships/parent-child-settings-using-separate-eps.md)
### Parent-Child Settings Using Nested EPs (custom-settings-groups/extension-points-for-parent-child-settings-relationships/parent-child-settings-using-nested-eps.md)
### Attributes for Parent-Child Settings EPs (custom-settings-groups/extension-points-for-parent-child-settings-relationships/attributes-for-parent-child-settings-eps.md)
