# Registering Actions

There are two main ways to register an action: either by [registering it in the plugin.xml file](#registering-actions-in-pluginxml) or [through code](#registering-actions-from-code).

### Registering Actions in plugin.xml (sdk.action-system.registering-actions.registering-actions-in-plugin-xml)
### Registering Actions from Code

Two steps are required to register an action from code:

* First, an instance of the class derived from `AnAction` must be passed to the `registerAction()` method of [ActionManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/ActionManager.java), to associate the action with an ID.

* Second, the action needs to be added to one or more groups. To get an instance of an action group by ID, it is necessary to call `ActionManager.getAction()` and cast the returned value to [DefaultActionGroup](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/actionSystem/DefaultActionGroup.java).
