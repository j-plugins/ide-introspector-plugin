---
id: sdk.plugin-configuration-file.idea-plugin.actions.action.add-to-group
title: Plugin Configuration File: add-to-group
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, add, group]
---
`add-to-group`







Specifies that the action should be added to an existing [&lt;group&gt;](#idea-plugin__actions__group).
A single action can be added to multiple groups.



Required
: no


Attributes
: * `group-id` (required) Specifies the ID of the [&lt;group&gt;](#idea-plugin__actions__group) to which the action is added. The group must be an implementation of the [DefaultActionGroup](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/actionSystem/DefaultActionGroup.java) class.

  * `anchor` (optional) Specifies the position of the action relative to other actions. Allowed values: * `first` - the action is placed as the first in the group * `last` (default) - the action is placed as the last in the group * `before` - the action is placed before the action specified by the `relative-to-action` attribute * `after` - the action is placed after the action specified by the `relative-to-action` attribute

  * `relative-to-action` (required if `anchor` is `before`/`after`) The action before or after which the current action is inserted.


Example
: ```XML
<add-to-group
    group-id="ToolsMenu"
    anchor="after"
    relative-to-action="GenerateJavadoc"/>
```

