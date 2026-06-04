---
id: sdk.action-system.building-a-toolbar-popup-menu-from-actions
title: Action System: Building a Toolbar/Popup Menu from Actions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, building, toolbar, popup, menu, from]
---
If a plugin needs to include a toolbar or popup menu built from a group of actions in its user interface, that is achieved through [ActionPopupMenu](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/ActionPopupMenu.java) and [ActionToolbar](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/ActionToolbar.java).
These objects can be created through calls to the `ActionManager.createActionPopupMenu()` and `createActionToolbar()` methods.
To get a Swing component from such an object, call the respective `getComponent()` method.
See also [Action Groups](https://plugins.jetbrains.com/docs/intellij/popups.html#action-groups) for more advanced popups.

If an action toolbar is attached to a specific component (for example, a panel in a tool window), call `ActionToolbar.setTargetComponent()` and pass the related component's instance as a parameter.
Setting the target ensures that the toolbar buttons' state depends on the state of the related component, not on the current focus location within the IDE frame.

To add an action group to the list of customizable actions in `Settings | Appearance & Behavior | Menus and Toolbars`, implement
[CustomizableActionGroupProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-impl/src/com/intellij/ide/ui/customization/CustomizableActionGroupProvider.java)
and register in [com.intellij.customizableActionGroupProvider](https://jb.gg/ipe?extensions=com.intellij.customizableActionGroupProvider) extension point
,
and ensure that the [action group](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__actions__group) defines the `text` attribute or is [localized](#localizing-actions-and-groups).

See [Toolbar](https://plugins.jetbrains.com/docs/intellij/toolbar.html) in UI Guidelines for an overview.

