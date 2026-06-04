# Principal Implementation Overrides

Every IntelliJ Platform action should override `AnAction.update()` and must override `AnAction.actionPerformed()`.

#### AnAction.update()

`AnAction.update()`

An action's method `AnAction.update()` is called by the IntelliJ Platform framework to update an action state.
The state (enabled, visible) of an action determines whether the action is available in the UI.
An object of the [AnActionEvent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnActionEvent.java) type is passed to this method and contains information about the current context for the action.

Actions are made available by changing the state in the [Presentation](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/Presentation.java) object associated with the event context.
As explained in [Overriding the AnAction.update() Method](#overriding-the-anactionupdate-method), it is vital `update()` methods execute quickly and return execution to platform.

#### AnAction.getActionUpdateThread()

`AnAction.getActionUpdateThread()`

`AnAction.getActionUpdateThread()` returns an [ActionUpdateThread](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/ActionUpdateThread.java),
which specifies if the `update()` method is called on a [background thread (BGT) or the event-dispatching thread (EDT)](https://plugins.jetbrains.com/docs/intellij/threading-model.html).
The preferred method is to run the update on the BGT, which has the advantage of guaranteeing application-wide read access to
[PSI](https://plugins.jetbrains.com/docs/intellij/psi.html), [the virtual file system](https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html) (VFS), or [project models](https://plugins.jetbrains.com/docs/intellij/project-model.html).
Actions that run the update session on the BGT should not access the Swing component hierarchy directly.
Conversely, actions that specify to run their update on EDT must not access PSI, VFS, or project data but have access to Swing components and other UI models.

All accessible data is provided by the `DataContext` as explained in [Determining the Action Context](#determining-the-action-context).
When switching from BGT to EDT is necessary, actions can use `AnActionEvent.getUpdateSession()` to
access the [UpdateSession](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/UpdateSession.java) and
then call `UpdateSession.compute()` to run a function on EDT.

Inspection `Plugin DevKit | Code | ActionUpdateThread is missing` highlights missing implementation of
`AnAction.getActionUpdateThread()`.

#### AnAction.actionPerformed()

`AnAction.actionPerformed()`

An action's method `AnAction.actionPerformed()` is called by the IntelliJ Platform if available and selected by the user.
This method does the heavy lifting for the action: it contains the code executed when the action gets invoked.
The `actionPerformed()` method also receives `AnActionEvent` as a parameter, which is used to access any context data like projects, files, selection, and similar.
See [Overriding the AnAction.actionPerformed() Method](#overriding-the-anactionactionperformed-method) for more information.

#### Miscellaneous

There are other methods to override in the `AnAction` class, such as changing the default `Presentation` object for the action.
There is also a use case for overriding action constructors when registering them with dynamic action groups, demonstrated in the [Grouping Actions](https://plugins.jetbrains.com/docs/intellij/grouping-actions-tutorial.html#adding-child-actions-to-the-dynamic-group) tutorial.
