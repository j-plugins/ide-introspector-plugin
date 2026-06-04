---
id: sdk.plugin-configuration-file.idea-plugin.actions.action.mouse-shortcut
title: Plugin Configuration File: mouse-shortcut
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, mouse, shortcut]
---
`mouse-shortcut`

Specifies the mouse shortcut for the action.
A single action can have several mouse shortcuts.

Required
: no

Attributes
: * `keymap` (required) Specifies the keymap for which the action shortcut is active. IDs of the standard keymaps are defined as constants in the [KeymapManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/keymap/KeymapManager.java) class.

* `keystroke` (required) Specifies the clicks and modifiers for the action. It is defined as a sequence of words separated by spaces: * modifier keys: `shift`, `control`, `meta`, `alt`, `altGraph` * mouse buttons: `button1`, `button2`, `button3` * button double-click: `doubleClick`

* `remove` (optional) Removes a shortcut from the specified action.

* `replace-all` (optional) Removes all keyboard and mouse shortcuts from the specified action before adding the specified shortcut.

Examples
: * Add the shortcut to all keymaps: ```XML <mouse-shortcut keymap="$default" keystroke="control button3 doubleClick"/> ```

* Remove the given shortcut from the Mac OS X keymap: ```XML <mouse-shortcut keymap="Mac OS X" keystroke="control button3 doubleClick" remove="true"/> ```

* Remove all existing keyboard and mouse shortcuts and register one for the Mac OS X 10.5+ keymap only: ```XML <mouse-shortcut keymap="Mac OS X 10.5+" keystroke="control button3 doubleClick" replace-all="true"/> ```

