---
id: sdk.plugin-configuration-file.idea-plugin.actions.action
title: Plugin Configuration File: action
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, action]
---
`action`



<tldr>
Reference: [Registering Actions in plugin.xml](https://plugins.jetbrains.com/docs/intellij/action-system.html#registering-actions-in-pluginxml)
</tldr>





A single action entry of the [&lt;actions&gt;](#idea-plugin__actions) implemented by the plugin.
A single `<actions>` element can contain multiple `<action>` elements.



Required
: no


Attributes
: * `id` (optional; defaults to the action class short name if not specified) A unique action identifier. It is recommended to specify the `id` attribute explicitly. The action identifier must be unique across different plugins. To ensure uniqueness, consider prepending it with the value of the plugin's [&lt;id&gt;](#idea-plugin__id).

  * `class` (required) The fully qualified name of the action implementation class.

  * `text` (required if the action is not [localized](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups)) The default long-version text to be displayed for the action (tooltip for toolbar button or text for menu item).

  * `description` (optional) The text which is displayed in the status bar when the action is focused.

  * `icon` (optional) The icon that is displayed on the toolbar button or next to the action menu item. See [Working with Icons](https://plugins.jetbrains.com/docs/intellij/icons.html) for more information about defining and using icons.

  * `use-shortcut-of` (optional) The ID of the action whose keyboard shortcut this action will use.


Children
: * [&lt;abbreviation&gt;](#idea-plugin__actions__action__abbreviation)

  * [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group)

  * [&lt;keyboard-shortcut&gt;](#idea-plugin__actions__action__keyboard-shortcut)

  * [&lt;mouse-shortcut&gt;](#idea-plugin__actions__action__mouse-shortcut)

  * [&lt;override-text&gt;](#idea-plugin__actions__action__override-text)

  * [&lt;synonym&gt;](#idea-plugin__actions__action__synonym)


Examples
: * Action declaring explicit `text`: ```XML <action id="com.example.myframeworksupport.MyAction" class="com.example.impl.MyAction" text="Do Action" description="Do something with the code" icon="AllIcons.Actions.GC"> <!-- action children elements --> </action> ```

  * Action without the `text` attribute must use the texts from the resource bundle declared with the [&lt;resource-bundle&gt;](#idea-plugin__resource-bundle) element, or the `resource-bundle` attribute of the [&lt;actions&gt;](#idea-plugin__actions) element: ```XML <action id="com.example.myframeworksupport.MyAction" class="com.example.impl.MyAction" icon="AllIcons.Actions.GC"/> ```

##### add-to-group (plugin-configuration-file/idea-plugin/actions/action/add-to-group.md)
##### keyboard-shortcut (plugin-configuration-file/idea-plugin/actions/action/keyboard-shortcut.md)
##### mouse-shortcut (plugin-configuration-file/idea-plugin/actions/action/mouse-shortcut.md)
##### override-text (plugin-configuration-file/idea-plugin/actions/action/override-text.md)
##### synonym (plugin-configuration-file/idea-plugin/actions/action/synonym.md)
##### abbreviation (plugin-configuration-file/idea-plugin/actions/action/abbreviation.md)
