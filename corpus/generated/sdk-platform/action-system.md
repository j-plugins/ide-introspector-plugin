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

## Action Implementation (action-system/action-implementation.md)
### Principal Implementation Overrides (action-system/action-implementation/principal-implementation-overrides.md)
#### AnAction.update() (action-system/action-implementation/principal-implementation-overrides/anaction-update.md)
#### AnAction.getActionUpdateThread() (action-system/action-implementation/principal-implementation-overrides/anaction-getactionupdatethread.md)
#### AnAction.actionPerformed() (action-system/action-implementation/principal-implementation-overrides/anaction-actionperformed.md)
#### Miscellaneous (action-system/action-implementation/principal-implementation-overrides/miscellaneous.md)
### Overriding the `AnAction.update()` Method (action-system/action-implementation/overriding-the-anaction-update-method.md)
#### Determining the Action Context (action-system/action-implementation/overriding-the-anaction-update-method/determining-the-action-context.md)
#### Enabling and Setting Visibility for an Action (action-system/action-implementation/overriding-the-anaction-update-method/enabling-and-setting-visibility-for-an-action.md)
### Overriding the `AnAction.actionPerformed()` Method (action-system/action-implementation/overriding-the-anaction-actionperformed-method.md)
### Action IDs (action-system/action-implementation/action-ids.md)
### Grouping Actions (action-system/action-implementation/grouping-actions.md)
#### Presentation (action-system/action-implementation/grouping-actions/presentation.md)
#### The `compact` Attribute (action-system/action-implementation/grouping-actions/the-compact-attribute.md)
## Registering Actions (action-system/registering-actions.md)
### Registering Actions in plugin.xml (action-system/registering-actions/registering-actions-in-plugin-xml.md)
#### Action Declaration Reference (action-system/registering-actions/registering-actions-in-plugin-xml/action-declaration-reference.md)
#### Localizing Actions and Groups (action-system/registering-actions/registering-actions-in-plugin-xml/localizing-actions-and-groups.md)
##### Dedicated Resource Bundle (action-system/registering-actions/registering-actions-in-plugin-xml/localizing-actions-and-groups/dedicated-resource-bundle.md)
### Registering Actions from Code (action-system/registering-actions/registering-actions-from-code.md)
## Building a Toolbar/Popup Menu from Actions (action-system/building-a-toolbar-popup-menu-from-actions.md)
## Useful Action Base Classes (action-system/useful-action-base-classes.md)
### Toggle/Selection (action-system/useful-action-base-classes/toggle-selection.md)
#### Popup Menus (action-system/useful-action-base-classes/toggle-selection/popup-menus.md)
### Back/Forward Navigation (action-system/useful-action-base-classes/back-forward-navigation.md)
### Runtime Placeholder Action (action-system/useful-action-base-classes/runtime-placeholder-action.md)
## Executing Actions Programmatically (action-system/executing-actions-programmatically.md)
## Action ID Code Insight (action-system/action-id-code-insight.md)
### Builtin Places (action-system/action-id-code-insight/builtin-places.md)
### Custom Places (action-system/action-id-code-insight/custom-places.md)
#### Code (action-system/action-id-code-insight/custom-places/code.md)
#### Other Places (action-system/action-id-code-insight/custom-places/other-places.md)

> Source: IntelliJ Platform SDK docs — Action System (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
