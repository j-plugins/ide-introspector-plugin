# Action Implementation

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

### Principal Implementation Overrides (sdk.action-system.action-implementation.principal-implementation-overrides)
### Overriding the `AnAction.update()` Method (sdk.action-system.action-implementation.overriding-the-anaction-update-method)
### Overriding the `AnAction.actionPerformed()` Method

Overriding the `AnAction.actionPerformed()` Method

When the user selects an enabled action, be it from a menu or toolbar, the action's `AnAction.actionPerformed()` method is called.
This method contains the code executed to perform the action, and it is here that the real work gets done.

Warning: Reusable Logic

Reusable logic must not be exposed in the `AnAction` implementation via `static` methods (Java) or `companion object` (Kotlin).

Instead, introduce dedicated methods in utility classes or [Services](https://plugins.jetbrains.com/docs/intellij/plugin-services.html).

By using the `AnActionEvent` methods and `CommonDataKeys`, objects such as the `Project`, `Editor`, `PsiFile`, and other information is available.
For example, the `actionPerformed()` method can modify, remove, or add PSI elements to a file open in the editor.

The code that executes in the `AnAction.actionPerformed()` method should execute efficiently, but it does not have to meet the same stringent requirements as the `update()` method.

An example of inspecting PSI elements is demonstrated in the `action_basics` SDK code sample in [PopupDialogAction.actionPerformed()](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/action_basics/src/main/java/org/intellij/sdk/action/PopupDialogAction.java).

### Action IDs

Each action and action group must have a unique identifier (see the `id` attribute specification for [action](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__actions__action) and [group](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__actions__group)).

An action requires a unique identifier for every context where it appears in the IDE UI, even if the implementation FQN is shared.
Standard IntelliJ Platform action IDs are defined in [IdeActions](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/actionSystem/IdeActions.java).

### Grouping Actions (sdk.action-system.action-implementation.grouping-actions)
