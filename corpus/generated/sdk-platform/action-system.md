---
id: sdk.action-system
title: Action System
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, action, system]
---
<tldr>
Product Help: [Menus and toolbars](https://www.jetbrains.com/help/idea/customize-actions-menus-and-toolbars.html)

UI Guidelines: [Toolbar](https://plugins.jetbrains.com/docs/intellij/toolbar.html)
</tldr>

The Action System allows plugins to add their items to IntelliJ Platform-based IDE menus and toolbars.
For example, one of the action classes is responsible for the `File | Open File...` menu item and the Open... toolbar button.

Actions in the IntelliJ Platform require a [code implementation](#action-implementation) and must be [registered](#registering-actions).
The action implementation determines the contexts in which an action is available and its functionality when selected in the UI.
Registration determines where an action appears in the IDE UI.
Once implemented and registered, an action receives callbacks from the IntelliJ Platform in response to user gestures.

The [Creating Actions](https://plugins.jetbrains.com/docs/intellij/creating-actions-tutorial.html) tutorial describes the process of adding a custom action to a plugin.
The [Grouping Actions](https://plugins.jetbrains.com/docs/intellij/grouping-actions-tutorial.html) tutorial demonstrates three types of groups that can contain actions.

## Subtopics

- Action Implementation — `sdk.action-system.action-implementation`
- Registering Actions — `sdk.action-system.registering-actions`
- Building a Toolbar/Popup Menu from Actions — `sdk.action-system.building-a-toolbar-popup-menu-from-actions`
- Useful Action Base Classes — `sdk.action-system.useful-action-base-classes`
- Executing Actions Programmatically — `sdk.action-system.executing-actions-programmatically`
- Action ID Code Insight — `sdk.action-system.action-id-code-insight`

> Source: IntelliJ Platform SDK docs — Action System (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
