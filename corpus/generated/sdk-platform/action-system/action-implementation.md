---
id: sdk.action-system.action-implementation
title: Action System: Action Implementation
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, action, implementation]
---
Part of `sdk.action-system`.

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

## Subtopics

- Principal Implementation Overrides — `sdk.action-system.action-implementation.principal-implementation-overrides`
- Overriding the `AnAction.update()` Method — `sdk.action-system.action-implementation.overriding-the-anaction-update-method`
- Overriding the `AnAction.actionPerformed()` Method — `sdk.action-system.action-implementation.overriding-the-anaction-actionperformed-method`
- Action IDs — `sdk.action-system.action-implementation.action-ids`
- Grouping Actions — `sdk.action-system.action-implementation.grouping-actions`

> Source: IntelliJ Platform SDK docs — Action System: Action Implementation (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
