---
id: sdk.settings-guide.extension-points-for-settings.settings-declaration-attributes.table-of-attributes
title: Settings Guide: Table of Attributes
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, table, attributes]
---
Part of `sdk.settings-guide.extension-points-for-settings.settings-declaration-attributes`.

The attributes supported by [com.intellij.applicationConfigurable](https://jb.gg/ipe?extensions=com.intellij.applicationConfigurable) extension point
and [com.intellij.projectConfigurable](https://jb.gg/ipe?extensions=com.intellij.projectConfigurable) extension point
are in the table below:

| Attribute |Required |Attribute  Value   |Implementation  Basis   |
--------------------------------------------------------------------
| `instance` |yes [(1)](#attribute-notes) |FQN of implementation. See [The Configurable Interface](#the-configurable-interface) for more information. |`Configurable` |
| `provider` |yes [(1)](#attribute-notes) |FQN of implementation. See [The ConfigurableProvider Class](#the-configurableprovider-class) for more information. |`ConfigurableProvider` |
| `nonDefaultProject` |yes |Applicable only to the `com.intellij.projectConfigurable` (project Settings) EP.  `true` = show Settings for all projects except the [default project](https://www.jetbrains.com/help/idea/configure-project-settings.html#new-default-settings).  `false` = show Settings for all projects.   |`Configurable` |
| `displayName` |yes [(2)](#attribute-notes) |The non-localized Settings name visible to users, which is needed for the Settings dialog left-side menu.  For a localized visible name omit `displayName` and use the `key` and `bundle` attributes.   |`Configurable`  `ConfigurableProvider`   |
| `key` and `bundle` |yes [(2)](#attribute-notes) |The [localization](https://plugins.jetbrains.com/docs/intellij/internationalization.html#message-bundles) key and bundle for the Settings name visible to users.  For non-localized visible names omit `key` and `bundle` and use `displayName`.   |`Configurable`  `ConfigurableProvider`   |
| `id` |yes |The unique, FQN identifier for this implementation.  The FQN should be based on the plugin `id` to ensure uniqueness.   |`Configurable`  `ConfigurableProvider`   |
| `parentId` |yes |This attribute is used to create a hierarchy of Settings. This component is declared one of the specified `parentId` component's children. Typically used for placing a Settings panel within the Settings Dialog menu. Acceptable values for `parentId` are given in [Values for Parent ID Attribute](#values-for-parent-id-attribute).  `groupId` is deprecated. [(3)](#attribute-notes)   |`Configurable`  `ConfigurableProvider`   |
| `groupWeight` |no |Specifies the weight (stacking order) of this component within the group of a parent configurable component. The default weight is 0, meaning lowest in the order.  If one child in a group or a parent component has non-zero weight, all children will be sorted descending by their weight. If the weights are equal, the components will be sorted ascending by their display name.   |`Configurable`  `ConfigurableProvider`   |
| `dynamic` |no |This component's children are dynamically calculated by calling the `getConfigurables()` method.  Not recommended because it requires loading additional classes while building a Settings tree. If possible, use XML attributes instead.   |`Configurable.Composite` |
| `childrenEPName` |no |Specifies the FQN name of the Extension Point that will be used to calculate the children of this component. |`Configurable` |

##### Attribute Notes

(1) Either `instance` or `provider` must be specified depending on the implementation.

(2) Either `displayName` or `key` and `bundle` must be specified depending on whether the displayed Settings name is localized.

(3) If both `groupId` and `parentId` are specified, a warning is logged. Also, see default entry in [Values for Parent ID Attribute](#values-for-parent-id-attribute).

> Source: IntelliJ Platform SDK docs — Settings Guide: Table of Attributes (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
