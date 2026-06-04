---
id: sdk.plugin-configuration-file.idea-plugin.actions.group
title: Plugin Configuration File: group
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, group]
---
`group`

<tldr>
Reference: [Grouping Actions](https://plugins.jetbrains.com/docs/intellij/action-system.html#grouping-actions)
</tldr>

Defines an action group.
The [&lt;action&gt;](#idea-plugin__actions__action), `<group>` and [&lt;separator&gt;](#idea-plugin__actions__group__separator) elements defined inside the group are automatically included in it.
The `<group>` elements can be nested.

Required
: no

Attributes
: * `id` (required) A unique group identifier. The group identifier must be unique between different plugins. Thus, it is recommended to prepend it with the value of the plugin [&lt;id&gt;](#idea-plugin__id).

* `class` (optional) The fully qualified name of the group implementation class. If not specified, [DefaultActionGroup](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/actionSystem/DefaultActionGroup.java) is used.

* `text` (required if the `popup` is `true` and the group is not [localized](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups)) The default long-version text to be displayed for the group (text for the menu item showing the submenu).

* `description` (optional) The text which is displayed in the status bar when the group is focused.

* `icon` (optional) The icon that is displayed next to the group menu item. See [Working with Icons](https://plugins.jetbrains.com/docs/intellij/icons.html) for more information about defining and using icons.

* `popup` (optional) Boolean flag defining whether the group items are presented in the submenu popup. * `true` - group actions are placed in a submenu * `false` (default) - actions are displayed as a section of the same menu delimited by separators

* `compact` (optional) Boolean flag defining whether disabled actions within this group are hidden. If the value is: * `true` - disabled actions are hidden * `false` (default) - disabled actions are visible

* `use-shortcut-of` (optional) The ID of the action whose keyboard shortcut this group will use.

* `searchable` (optional; available since 2020.3) Boolean flag defining whether the group is displayed in `Help | Find Action...` or `Navigate | Search Everywhere` popups. Default value: `true`.

Children
: * [&lt;action&gt;](#idea-plugin__actions__action)

* [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group)

* [&lt;group&gt;](#idea-plugin__actions__group)

* [&lt;override-text&gt;](#idea-plugin__actions__action__override-text)

* [&lt;reference&gt;](#idea-plugin__actions__group__reference)

* [&lt;separator&gt;](#idea-plugin__actions__group__separator)

Examples
: * Group declaring explicit `text`: ```XML <group id="com.example.myframeworksupport.MyGroup" popup="true" text="My Tools"> <!-- group children elements --> </group> ```

* A popup group without the `text` attribute must use the texts from the resource bundle declared with the [&lt;resource-bundle&gt;](#idea-plugin__resource-bundle) element, or the `resource-bundle` attribute of the [&lt;actions&gt;](#idea-plugin__actions) element: ```XML <group id="com.example.myframeworksupport.MyGroup" popup="true"/> ```

* A group with custom implementation and icon: ```XML <group id="com.example.myframeworksupport.MyGroup" class="com.example.impl.MyGroup" icon="AllIcons.Actions.GC"/> ```

##### reference (plugin-configuration-file/idea-plugin/actions/group/reference.md)
##### separator (plugin-configuration-file/idea-plugin/actions/group/separator.md)
