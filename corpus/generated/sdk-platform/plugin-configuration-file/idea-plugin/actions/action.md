# action

`action`



<tldr>
Reference: [Registering Actions in plugin.xml](https://plugins.jetbrains.com/docs/intellij/action-system.html#registering-actions-in-pluginxml)
</tldr>





A single action entry of the [&lt;actions&gt;](#idea-plugin__actions) implemented by the plugin.
A single `<actions>` element can contain multiple `<action>` elements.



Required
: no


Attributes
: * `id` (optional; defaults to the action class short name if not specified) A unique action identifier. It is recommended to specify the `id` attribute explicitly. The action identifier must be unique across different plugins. To ensure uniqueness, consider prepending it with the value of the plugin's [&lt;id&gt;](#idea-plugin__id).

  * `class` (required) The fully qualified name of the action implementation class.

  * `text` (required if the action is not [localized](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups)) The default long-version text to be displayed for the action (tooltip for toolbar button or text for menu item).

  * `description` (optional) The text which is displayed in the status bar when the action is focused.

  * `icon` (optional) The icon that is displayed on the toolbar button or next to the action menu item. See [Working with Icons](https://plugins.jetbrains.com/docs/intellij/icons.html) for more information about defining and using icons.

  * `use-shortcut-of` (optional) The ID of the action whose keyboard shortcut this action will use.


Children
: * [&lt;abbreviation&gt;](#idea-plugin__actions__action__abbreviation)

  * [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group)

  * [&lt;keyboard-shortcut&gt;](#idea-plugin__actions__action__keyboard-shortcut)

  * [&lt;mouse-shortcut&gt;](#idea-plugin__actions__action__mouse-shortcut)

  * [&lt;override-text&gt;](#idea-plugin__actions__action__override-text)

  * [&lt;synonym&gt;](#idea-plugin__actions__action__synonym)


Examples
: * Action declaring explicit `text`: ```XML <action id="com.example.myframeworksupport.MyAction" class="com.example.impl.MyAction" text="Do Action" description="Do something with the code" icon="AllIcons.Actions.GC"> <!-- action children elements --> </action> ```

  * Action without the `text` attribute must use the texts from the resource bundle declared with the [&lt;resource-bundle&gt;](#idea-plugin__resource-bundle) element, or the `resource-bundle` attribute of the [&lt;actions&gt;](#idea-plugin__actions) element: ```XML <action id="com.example.myframeworksupport.MyAction" class="com.example.impl.MyAction" icon="AllIcons.Actions.GC"/> ```

##### add-to-group

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

##### keyboard-shortcut

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

##### mouse-shortcut

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

##### override-text

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

##### synonym

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

##### abbreviation

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
