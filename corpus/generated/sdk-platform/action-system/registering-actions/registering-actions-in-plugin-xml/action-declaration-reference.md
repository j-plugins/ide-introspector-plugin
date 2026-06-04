---
id: sdk.action-system.registering-actions.registering-actions-in-plugin-xml.action-declaration-reference
title: Action System: Action Declaration Reference
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, action, declaration, reference]
---
The places where actions can appear are defined by constants in [ActionPlaces](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/actionSystem/ActionPlaces.java).
Group IDs for the IntelliJ Platform are defined in [PlatformActions.xml](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-resources/src/idea/PlatformActions.xml).

This and additional information can also be found by using the [Code Completion](https://www.jetbrains.com/help/idea/auto-completing-code.html#invoke-basic-completion), [Quick Definition](https://www.jetbrains.com/help/idea/viewing-reference-information.html#view-definition-symbols), and [Quick Documentation](https://www.jetbrains.com/help/idea/viewing-reference-information.html#inline-quick-documentation) features.

Tip:

To look up existing Action ID (for example, for use in `relative-to-action`), [UI Inspector](https://plugins.jetbrains.com/docs/intellij/internal-ui-inspector.html) can be used.

Note:

See the [&lt;actions&gt;](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__actions) element and its children documentation for details.

```XML
<actions>

  <action
      id="VssIntegration.GarbageCollection"
      class="com.example.impl.CollectGarbage"
      text="Garbage Collector: Collect _Garbage"
      description="Run garbage collector"
      icon="icons/garbage.png">

    <!--
    The second <override-text> element uses the alternate attribute
    "use-text-of-place" to define a location (EditorPopup) to use the
    same text as is used in MainMenu. It is a way to specify the use
    of an alternate menu text in multiple discrete menu groups.
    -->
    <override-text place="MainMenu" text="Collect _Garbage"/>
    <override-text place="EditorPopup" use-text-of-place="MainMenu"/>

    <!-- Provide alternative names for searching action by name -->
    <synonym text="GC"/>

    <add-to-group
        group-id="ToolsMenu"
        relative-to-action="GenerateJavadoc"
        anchor="after"/>

    <!-- Add the first and second keystrokes to all keymaps... -->
    <keyboard-shortcut
        keymap="$default"
        first-keystroke="control alt G"
        second-keystroke="C"/>

    <!-- ...except the "Mac OS X" keymap and its children. -->
    <keyboard-shortcut
        keymap="Mac OS X"
        first-keystroke="control alt G"
        second-keystroke="C"
        remove="true"/>

    <!-- The "Mac OS X 10.5+" keymap and its children will have only
    this keyboard shortcut for this action. -->
    <keyboard-shortcut
        keymap="Mac OS X 10.5+"
        first-keystroke="control alt G"
        second-keystroke="C"
        replace-all="true"/>

    <mouse-shortcut
        keymap="$default"
        keystroke="control button3 doubleClick"/>
  </action>

  <!--
  This action declares neither a text nor a description attribute.
  If it has a resource bundle declared, the text and descriptions
  will be retrieved based on the action-id incorporated in the key
  for a translated string.
  -->
  <action
      id="sdk.action.PopupDialogAction"
      class="sdk.action.PopupDialogAction"
      icon="SdkIcons.Sdk_default_icon"/>

  <group
      class="com.example.impl.MyActionGroup"
      id="TestActionGroup"
      text="Test Group"
      description="Group with test actions"
      icon="icons/testGroup.png"
      popup="true"
      compact="true">

    <action
        id="VssIntegration.TestAction"
        class="com.example.impl.TestAction"
        text="My Test Action"
        description="My test action"/>

    <!-- The <separator> element defines a separator between actions.
    It can also have an <add-to-group> child element. -->
    <separator/>

    <!-- A group that is excluded from "Help | Find Action..."
    and "Navigate | Search Everywhere" -->
    <group id="TestActionSubGroup" searchable="false"/>

    <!-- The <reference> element allows adding an existing action to
    the group. The mandatory "ref" attribute specifies the ID of
    the action to add. -->
    <reference ref="EditorCopy"/>

    <add-to-group
        group-id="MainMenu"
        relative-to-action="HelpMenu"
        anchor="before"/>
  </group>
</actions>
```

