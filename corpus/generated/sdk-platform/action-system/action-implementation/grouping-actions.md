---
id: sdk.action-system.action-implementation.grouping-actions
title: Action System: Grouping Actions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, grouping, actions]
---
Part of `sdk.action-system.action-implementation`.

Groups organize actions into logical UI structures, which in turn can contain other groups.
A group of actions can form a toolbar or a menu.
Subgroups of a group can form submenus of a menu.

Actions can be included in multiple groups and thus appear in different places within the UI.
An action must have a unique identifier for each place it appears in the UI.
See the [Action Declaration Reference](#action-declaration-reference) section for information about how to specify locations.

#### Presentation

A new [Presentation](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/Presentation.java) gets created for every place where the action appears.
Therefore, the same action can have a different text or icon when it appears in different places of the user interface.
Different presentations for the action are created by copying the Presentation returned by the `AnAction.getTemplatePresentation()` method.

#### 

The `compact` Attribute

A group's `compact` attribute specifies whether an action within that group is visible when disabled.
See [Registering Actions in plugin.xml](#registering-actions-in-pluginxml) for an explanation of how the `compact` attribute is set for a group.
If the `compact` attribute is `true` for a menu group, an action in the menu only appears if its state is both enabled and visible.
In contrast, if the `compact` attribute is `false`, an action in the menu appears if its state is disabled but visible.
Some menus like `Tools` have the `compact` attribute set, so there isn't a way to show an action on the `Tools` menu if it is not enabled.

| Host Menu `compact` Setting |Action Enabled |Visibility Enabled |Menu Item Visible? |Menu Item Appears Gray? |
----------------------------------------------------------------------------------------------------------------
| T |F |T |F |N/A |
| T |T |T |T |F |
| F |F |T |T |T |
| F |T |T |T |F |

All other combinations of `compact`, visibility, and enablement produce N/A for gray appearance because the menu item isn't visible.

See the [Grouping Actions](https://plugins.jetbrains.com/docs/intellij/grouping-actions-tutorial.html) tutorial for examples of creating action groups.

> Source: IntelliJ Platform SDK docs — Action System: Grouping Actions (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
