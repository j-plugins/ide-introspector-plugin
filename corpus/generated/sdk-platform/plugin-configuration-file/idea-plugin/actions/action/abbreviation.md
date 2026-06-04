---
id: sdk.plugin-configuration-file.idea-plugin.actions.action.abbreviation
title: Plugin Configuration File: abbreviation
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, abbreviation]
---
`abbreviation`







Defines an abbreviation for searching the action in `Help | Find Action...` or
`Navigate | Search Everywhere` popups.
A single action can have multiple abbreviations.



Required
: no


Attributes
: * `value` (required) The abbreviation value.


Example
: ```XML
<!-- Default action text: UI Inspector -->
<abbreviation value="uii"/>
```

