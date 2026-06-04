---
id: sdk.plugin-configuration-file.idea-plugin.actions.action.keyboard-shortcut
title: Plugin Configuration File: keyboard-shortcut
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, keyboard, shortcut]
---
`keyboard-shortcut`

Specifies the keyboard shortcut for the action.
A single action can have several keyboard shortcuts.

Required
: no

Attributes
: * `keymap` (required) Specifies the keymap for which the action shortcut is active. IDs of the standard keymaps are defined as constants in the [KeymapManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/keymap/KeymapManager.java) class.

* `first-keystroke` (required) Specifies the first keystroke of the action shortcut. The keystrokes are specified according to the regular Swing rules.

* `second-keystroke` (optional) Specifies the second keystroke of the action shortcut.

* `remove` (optional) Removes a shortcut from the specified action.

* `replace-all` (optional) Removes all keyboard and mouse shortcuts from the specified action before adding the specified shortcut.

Examples
: * Add the first and second keystrokes to all keymaps: ```XML <keyboard-shortcut keymap="$default" first-keystroke="control alt G" second-keystroke="C"/> ```

* Remove the given shortcut from the Mac OS X keymap: ```XML <keyboard-shortcut keymap="Mac OS X" first-keystroke="control alt G" second-keystroke="C" remove="true"/> ```

* Remove all existing keyboard and mouse shortcuts and register one for the Mac OS X 10.5+ keymap only: ```XML <keyboard-shortcut keymap="Mac OS X 10.5+" first-keystroke="control alt G" second-keystroke="C" replace-all="true"/> ```

