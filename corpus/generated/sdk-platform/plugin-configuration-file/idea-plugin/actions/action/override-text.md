---
id: sdk.plugin-configuration-file.idea-plugin.actions.action.override-text
title: Plugin Configuration File: override-text
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, override, text]
---
`override-text`

Defines an alternate menu action or group text depending on context: menu location, toolbar, and other.

Supported
: 2020.1+ for actions

2020.3+ for groups

Required
: no

Attributes
: * `place` (required) Declares where the alternate text should be used.

* `text` (`text` or `use-text-of-place` is required) Defines the text to be displayed for the action.

* `use-text-of-place` (`text` or `use-text-of-place` is required) Defines a location whose text should be displayed for this action.

Examples
: * Explicitly overridden text: ```XML <!-- Default action text: "Garbage Collector: Collect _Garbage" --> <action class="com.example.CollectGarbage" text="Garbage Collector: Collect _Garbage" ...> <!-- Alternate text displayed anywhere in the main menu: "Collect _Garbage" --> <override-text place="MainMenu" text="Collect _Garbage"/> </action> ```

* Overridden text reused from the `MainMenu` place: ```XML <override-text place="EditorPopup" use-text-of-place="MainMenu"/> ```

