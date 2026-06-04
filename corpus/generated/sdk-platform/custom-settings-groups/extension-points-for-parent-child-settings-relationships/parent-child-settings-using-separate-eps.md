---
id: sdk.custom-settings-groups.extension-points-for-parent-child-settings-relationships.parent-child-settings-using-separate-eps
title: Custom Settings Groups: Parent-Child Settings Using Separate EPs
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, parent, child, settings, using, separate]
---
One way of declaring a parent-child relationship is by using two separate declarations.
This form can be used regardless of whether the parent Settings declaration is in the same plugin.
If the `id` attribute of the parent is known, a plugin can add Settings as a child of that parent.

For example, below are two declarations for project Settings.
The first gets added to the `tools` group, and the second gets added to the `id` of the parent.
The `id` of the second, child `<projectConfigurable>` adds a suffix (`servers`) to the `id` of the parent.

```XML
<extensions defaultExtensionNs="com.intellij">
  <projectConfigurable
      parentId="tools"
      id="com.intellij.sdk.tasks"
      displayName="Tasks"
      nonDefaultProject="true"
      instance="com.intellij.sdk.TaskConfigurable"/>

  <projectConfigurable
      parentId="com.intellij.sdk.tasks"
      id="com.intellij.sdk.tasks.servers"
      displayName="Servers"
      nonDefaultProject="true"
      instance="com.intellij.sdk.TaskRepositoriesConfigurable"/>
</extensions>
```

See the [Attributes for Parent-Child Settings EPs](#attributes-for-parent-child-settings-eps) section for details about the suffix `id`.

