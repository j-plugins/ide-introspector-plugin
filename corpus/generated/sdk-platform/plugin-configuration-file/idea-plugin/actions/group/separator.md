---
id: sdk.plugin-configuration-file.idea-plugin.actions.group.separator
title: Plugin Configuration File: separator
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, separator]
---
`separator`

Defines a separator between actions in a group.
The element can be used directly under the [&lt;actions&gt;](#idea-plugin__actions) element with the child
[&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group) element defining the target group, or in the
[&lt;group&gt;](#idea-plugin__actions__group) element.

Required
: no

Attributes
: * `text` (optional) Text displayed on the separator. Separator text is displayed only in specific contexts such as popup menus, toolbars, etc.

* `key` (optional) The [message key](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups) for the separator text. The message bundle for use should be registered via the `resource-bundle` attribute of the [&lt;actions&gt;](#idea-plugin__actions) element. The attribute is ignored if the `text` attribute is specified.

Children
: * [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group)

Examples
: * A separator dividing two actions in a group: ```XML <group ...> <action .../> <separator/> <action .../> </group> ```

* A separator registered directly in the [&lt;actions&gt;](#idea-plugin__actions) element: ```XML <actions> <separator> <add-to-group group-id="com.example.MyGroup" anchor="first"/> </separator> </group> ```

* A separator with a defined text: ```XML <separator text="Group By"/> ```

* A separator with a text defined by a message key: ```XML <separator key="message.key"/> ```

