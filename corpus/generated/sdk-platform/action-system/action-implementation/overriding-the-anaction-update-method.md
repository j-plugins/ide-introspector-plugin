---
id: sdk.action-system.action-implementation.overriding-the-anaction-update-method
title: Action System: Overriding the `AnAction.update()` Method
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, overriding, anaction, update, method]
---
Part of `sdk.action-system.action-implementation`.

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

#### Determining the Action Context

The `AnActionEvent` object passed to `update()` carries information about the current context for the action.
Context information is available from the methods of `AnActionEvent`, providing information such as the Presentation and whether the action is triggered by a Toolbar.
Additional context information is available using the method `AnActionEvent.getData()`.
Keys defined, for example, in [CommonDataKeys](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/CommonDataKeys.java) are passed to the `getData()` method to retrieve objects such as `Project`, `Editor`, `PsiFile`, and other information.
Accessing this information is relatively light-weight and is suited for `AnAction.update()`.

#### Enabling and Setting Visibility for an Action

Based on information about the action context, the `AnAction.update()` method can enable, disable, or hide an action.
An action's enabled/disabled state and visibility are set using methods of the `Presentation` object, which is accessed using `AnActionEvent.getPresentation()`.

The default `Presentation` object is a set of descriptive information about a menu or toolbar action.
Every context for an action – it might appear in multiple menus, toolbars, or Navigation search locations – has a unique presentation.
Attributes such as an action's text, description, and icons and visibility and enable/disable state, are stored in the presentation.
The attributes in a presentation get initialized from the [action registration](#registering-actions).
However, some can be changed at runtime using the methods of the `Presentation` object associated with an action.

The enabled/disabled state of an action is set using `Presentation.setEnabled()`.
The visibility state of an action is set using `Presentation.setVisible()`.
If an action is enabled, the `AnAction.actionPerformed()` can be called if a user selects an action in the IDE.
A menu action shows in the UI location specified in its registration.
A toolbar action displays its enabled (or selected) icon, depending on the user interaction.

When an action is disabled, `AnAction.actionPerformed()` will not be called.
Toolbar actions display their respective icons for the disabled state.
The visibility of a disabled action in a menu depends on whether the host menu (for example, "ToolsMenu") containing the action has the `compact` attribute set.
See [Grouping Actions](#grouping-actions) for more information about the `compact` attribute and menu actions' visibility.

Note:

If an action is added to a toolbar, its `update()` can be called if there was any user activity or focus transfer.
If the action's availability changes in the absence of these events, then call [ActivityTracker.getInstance().inc()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/ide/ActivityTracker.java) to notify the action subsystem to update all toolbar actions.

An example of enabling a menu action based on whether a project is open is demonstrated in [PopupDialogAction.update()](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/action_basics/src/main/java/org/intellij/sdk/action/PopupDialogAction.java).

> Source: IntelliJ Platform SDK docs — Action System: Overriding the `AnAction.update()` Method (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
