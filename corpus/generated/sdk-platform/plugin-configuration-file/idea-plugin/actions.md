---
id: sdk.plugin-configuration-file.idea-plugin.actions
title: Plugin Configuration File: actions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, actions]
---
Part of `sdk.plugin-configuration-file.idea-plugin`.

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

## Subtopics

- action — `sdk.plugin-configuration-file.idea-plugin.actions.action`
- group — `sdk.plugin-configuration-file.idea-plugin.actions.group`

> Source: IntelliJ Platform SDK docs — Plugin Configuration File: actions (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
