---
id: sdk.plugin-configuration-file.idea-plugin.actions.group.reference
title: Plugin Configuration File: reference
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, reference]
---
`reference`

Allows adding an existing action to the group.
The element can be used directly under the [&lt;actions&gt;](#idea-plugin__actions) element, or in
the [&lt;group&gt;](#idea-plugin__actions__group) element.

Required
: no

Attributes
: * `ref` (required) The ID of the action to add to a group.

* `id` (optional) Deprecated: Use `ref` instead. The ID of the action to add to a group.

Children
: * [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group)

Examples
: * An action reference in a group: ```XML <group ...> <reference ref="EditorCopy"/> </group> ```

* An action reference registered directly in the [&lt;actions&gt;](#idea-plugin__actions) element: ```XML <actions> <reference ref="com.example.MyAction"> <add-to-group group-id="ToolsMenu"/> </reference> </group> ```

