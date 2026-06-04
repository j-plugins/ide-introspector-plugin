---
id: sdk.plugin-configuration-file.idea-plugin.actions
title: Plugin Configuration File: actions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, actions]
---
`actions`

<tldr>
Reference: [Actions](https://plugins.jetbrains.com/docs/intellij/action-system.html)
</tldr>

Defines the plugin actions.

Required
: no

Attributes
: * `resource-bundle` (optional; available since 2020.1) Defines the dedicated actions resource bundle. See [Localizing Actions and Groups](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups) for more details.

Children
: * [&lt;action&gt;](#idea-plugin__actions__action)

* [&lt;group&gt;](#idea-plugin__actions__group)

* [&lt;reference&gt;](#idea-plugin__actions__group__reference)

Example
: ```XML
<actions resource-bundle="messages.ActionsBundle">
<!--
Actions/Groups defined here will use keys
from the ActionsBundle.properties bundle.
-->
</actions>
```

#### action (plugin-configuration-file/idea-plugin/actions/action.md)
##### add-to-group (plugin-configuration-file/idea-plugin/actions/action/add-to-group.md)
##### keyboard-shortcut (plugin-configuration-file/idea-plugin/actions/action/keyboard-shortcut.md)
##### mouse-shortcut (plugin-configuration-file/idea-plugin/actions/action/mouse-shortcut.md)
##### override-text (plugin-configuration-file/idea-plugin/actions/action/override-text.md)
##### synonym (plugin-configuration-file/idea-plugin/actions/action/synonym.md)
##### abbreviation (plugin-configuration-file/idea-plugin/actions/action/abbreviation.md)
#### group (plugin-configuration-file/idea-plugin/actions/group.md)
##### reference (plugin-configuration-file/idea-plugin/actions/group/reference.md)
##### separator (plugin-configuration-file/idea-plugin/actions/group/separator.md)
