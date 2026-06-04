---
id: sdk.action-system.action-implementation.overriding-the-anaction-update-method
title: Action System: Overriding the `AnAction.update()` Method
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, overriding, anaction, update, method]
---
Overriding the `AnAction.update()` Method

The method `AnAction.update()` is periodically called by the IntelliJ Platform in response to user gestures.
The `update()` method gives an action to evaluate the current context and enable or disable its functionality.
Implementors must ensure that changing presentation and availability status handles all variants and state transitions; otherwise, the given Action will get "stuck".

Warning: Performance

The `AnAction.update()` method can be called frequently on [Event Dispatch Thread (EDT)](https://plugins.jetbrains.com/docs/intellij/threading-model.html).
It must execute very quickly; no real work must be performed.
For example, checking selection in a tree or a list is considered valid, but working with the file system is not.

If the new state of an action cannot be determined quickly, evaluation should be performed in the `AnAction.actionPerformed()` method
and the user [notified](https://plugins.jetbrains.com/docs/intellij/informing-users.html) accordingly if the context isn't suitable.

#### Determining the Action Context (action-system/action-implementation/overriding-the-anaction-update-method/determining-the-action-context.md)
#### Enabling and Setting Visibility for an Action (action-system/action-implementation/overriding-the-anaction-update-method/enabling-and-setting-visibility-for-an-action.md)
