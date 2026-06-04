# Useful Action Base Classes

### Toggle/Selection

Use [ToggleAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/actionSystem/ToggleAction.java)
or [DumbAwareToggleAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/project/DumbAwareToggleAction.java)
for actions with the "selected"/"pressed" state (for example, menu item with checkbox, toolbar action button).
See also [ToggleOptionAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/actionSystem/ToggleOptionAction.java).

#### Popup Menus

In popup menus, `ToggleAction` no longer closes the popup by default.
Use [Presentation.setKeepPopupOnPerform()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/Presentation.java)
with [KeepPopupOnPerform.IfRequested](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/KeepPopupOnPerform.java)
in the action constructor or its `update()` method.

### Back/Forward Navigation

Use [BackAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/ui/navigation/BackAction.java) and
[ForwardAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/ui/navigation/ForwardAction.java) to provide a navigation trail taken from
[History](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/ui/navigation/History.java) provided by `History.KEY`.

### Runtime Placeholder Action

For actions registered at runtime (for example, in a tool window toolbar), add an [&lt;action&gt;](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__actions__action) entry with
[EmptyAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/actionSystem/EmptyAction.java)
to "reserve" Action ID, so they become visible in `Settings | Keymap`.
