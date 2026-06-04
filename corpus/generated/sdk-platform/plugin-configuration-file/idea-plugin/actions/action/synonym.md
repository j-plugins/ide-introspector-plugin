---
id: sdk.plugin-configuration-file.idea-plugin.actions.action.synonym
title: Plugin Configuration File: synonym
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, synonym]
---
`synonym`

Defines an alternative text for searching the action in `Help | Find Action...` or
`Navigate | Search Everywhere` popups.
A single action can have multiple synonyms.

Required
: no

Attributes
: * `key` (`key` or `text` is required) The key of the synonym text provided in a [message bundle](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups).

* `text` (`key` or `text` is required) The synonym text.

Example
: ```XML
<!-- Default action text: Delete Element -->
<synonym key="my.action.text.remove.element"/>
<synonym text="Remove Element"/>
```

