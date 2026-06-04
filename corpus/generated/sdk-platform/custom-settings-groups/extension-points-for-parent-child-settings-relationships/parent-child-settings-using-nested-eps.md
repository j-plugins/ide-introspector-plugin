---
id: sdk.custom-settings-groups.extension-points-for-parent-child-settings-relationships.parent-child-settings-using-nested-eps
title: Custom Settings Groups: Parent-Child Settings Using Nested EPs
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, parent, child, settings, using, nested]
---
A shorthand for the separate declaration approach is using the `configurable` property.
This approach nests the child's Settings declaration within the `com.intellij.projectConfigurable` or `com.intellij.applicationConfigurable` EP.

When using `configurable` there isn't a `parentId` for the child because the nesting implies it.
As with using separate EP declarations, formatting restrictions are placed on the child's `id` attribute - the suffix (`servers`) gets added.
See the [Attributes for Parent-Child Settings EPs](#attributes-for-parent-child-settings-eps) section.

The example below demonstrates a nested `configurable` declaration:

```XML
<extensions defaultExtensionNs="com.intellij">
  <projectConfigurable
        parentId="tools"
        id="com.intellij.sdk.tasks"
        displayName="Tasks"
        nonDefaultProject="true"
        instance="com.intellij.sdk.TaskConfigurable">
    <configurable
        id="com.intellij.sdk.tasks.servers"
        displayName="Servers"
        nonDefaultProject="true"
        instance="com.intellij.sdk.TaskRepositoriesConfigurable"/>
  </projectConfigurable>
</extensions>
```

Within the parent `<projectConfigurable>` EP declaration above, more `<configurable>` declarations could be added as sibling Settings.

