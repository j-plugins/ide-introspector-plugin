---
id: sdk.action-system.action-implementation
title: Action System: Action Implementation
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, action, implementation]
---
An action is a class derived from the abstract class [AnAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnAction.java) (see also [Useful Action Base Classes](#useful-action-base-classes) below).
The IntelliJ Platform calls methods of actions when a user interacts with a menu item or toolbar button.

Warning: No fields allowed

Classes based on `AnAction` must not have class fields of any kind.
This is because an instance of `AnAction` class exists for the entire lifetime of the application.
If the `AnAction` class uses a field to store data that has a shorter lifetime and doesn't clear this data promptly, the data leaks.
For example, any `AnAction` data that exists only within the context of a `Project` causes the `Project` to be kept in memory after the user has closed it.

Tip: Actions available during indexing

For actions available during [dumb mode](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html#dumb-mode), extend from
[DumbAwareAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/project/DumbAwareAction.java) instead of `AnAction`.

Do not override `AnAction.isDumbAware()` instead.

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
