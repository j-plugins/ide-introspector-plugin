---
id: sdk.custom-settings-groups
title: Custom Settings Groups
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, custom, settings, groups]
---
As described in [Extension Points for Settings](https://plugins.jetbrains.com/docs/intellij/settings-guide.html#extension-points-for-settings), custom Settings can be declared as children of existing parent groups such as `Tools`.
These parent groups are the existing categories of Settings in the IntelliJ Platform-based IDE.

However, suppose the custom Settings are rich enough to require multiple levels?
For example, a custom Setting implementation has multiple sub-Settings implementations.
Extension Point declarations can create this kind of multilayer Settings hierarchy.

Tip:

See [Inspecting Settings](https://plugins.jetbrains.com/docs/intellij/internal-ui-inspector.html#inspecting-settings) on how to gather information in the IDE instance for Settings dialog.

## Extension Points for Parent-Child Settings Relationships

There are multiple ways of creating parent-child relationships in groups of Settings: in implementations, or Extension Point declarations.
However, there are performance penalties for creating these relationships in implementations because the objects must be instantiated to determine the relationships.
This section describes the syntax for declaring more complex parent-child relationships in `com.intellij.projectConfigurable` or `com.intellij.applicationConfigurable` EPs.

Note:

An application configurable can be a parent of a project configurable.

There are two ways of declaring parent-child relationships using the `com.intellij.projectConfigurable` EP or `com.intellij.applicationConfigurable` EP.
The first is to use separate EP declarations that are tied together by the value of one attribute.
The second method is to use nested declarations.

### Parent-Child Settings Using Separate EPs

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

### Parent-Child Settings Using Nested EPs

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

### Attributes for Parent-Child Settings EPs

There is only one unique attribute when declaring a child Settings EP.
The other attributes are the same as discussed in [Settings Declaration Attributes](https://plugins.jetbrains.com/docs/intellij/settings-guide.html#settings-declaration-attributes).

For the child of a parent, the `id` attribute becomes compound:

| Attribute |Required |Value |
------------------------------
| `id` |Y |Compound FQN of implementation based on `com.intellij.openapi.options.Configurable` in the form: `XX.YY` where:    * `XX` - the parent Settings component FQN-based ID    * `YY` - unique to the child among other siblings   |

Tip:

All children share the parent's `id` as the basis of their own `id`.
All children have an `id` suffix that is unique among their siblings.

## Implementations for Parent-Child Settings

Implementations can be based on [Configurable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/Configurable.java), [ConfigurableProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/ConfigurableProvider.java) or one of their subtypes.
For more information about creating Settings implementations, see [Implementations for Settings Extension Points](https://plugins.jetbrains.com/docs/intellij/settings-guide.html#implementations-for-settings-extension-points).

### Configurable Marker Interfaces

The `Configurable.Composite` interface indicates a configurable component has child components.
The preferred approach is to specify child components in the [EP declaration](#extension-points-for-parent-child-settings-relationships).
Using the `Composite` interface incurs the penalty of loading child classes while building the tree of Settings Swing components.

> Source: IntelliJ Platform SDK docs — Custom Settings Groups (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
