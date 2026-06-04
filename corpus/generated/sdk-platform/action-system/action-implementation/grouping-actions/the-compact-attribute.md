---
id: sdk.action-system.action-implementation.grouping-actions.the-compact-attribute
title: Action System: The `compact` Attribute
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, compact, attribute]
---
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

