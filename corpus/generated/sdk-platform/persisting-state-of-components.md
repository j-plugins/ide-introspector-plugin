# Persisting State of Components

The IntelliJ Platform provides an API that allows components or services to persist their state between restarts of the IDE.
The API allows for persisting simple key-value entries and complex state classes.

Note: Split Mode

[Split plugins](https://plugins.jetbrains.com/docs/intellij/split-mode-and-remote-development.html) may need explicit frontend and backend synchronization metadata in addition to a regular `PersistentStateComponent` implementation.
See [Persistent State Component in Split Mode](https://plugins.jetbrains.com/docs/intellij/persistent-state-in-split-mode.html).

Warning:

For persisting sensitive data like passwords, see [Persisting Sensitive Data](https://plugins.jetbrains.com/docs/intellij/persisting-sensitive-data.html).

## Using `PersistentStateComponent (sdk.persisting-state-of-components.using-persistentstatecomponent)
## Using `PropertiesComponent` for Simple Non-Roamable Persiste

Using `PropertiesComponent` for Simple Non-Roamable Persistence

If the plugin needs to persist a few simple values, the easiest way to do so is to use the [PropertiesComponent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/ide/util/PropertiesComponent.java) service.
It can save both application-level values and project-level values in the workspace file.
Roaming is disabled for `PropertiesComponent`, so use it only for temporary, non-roamable properties.

Use the `PropertiesComponent.getInstance()` method for storing application-level values and the `PropertiesComponent.getInstance(Project)` method for storing project-level values.

Since all plugins share the same namespace, it is highly recommended to prefix key names (for example, using plugin ID `com.example.myCustomSetting`).

## Legacy API (`JDOMExternalizable`)

Legacy API (`JDOMExternalizable`)

Older components use the [JDOMExternalizable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/util/JDOMExternalizable.java) interface for persisting state.
It uses the `readExternal()` method for reading the state from a JDOM element, and `writeExternal()` to write the state.

Implementations can manually store the state in attributes and sub-elements or use the [DefaultJDOMExternalizer](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/util/DefaultJDOMExternalizer.java) class to store the values of all public fields automatically.

Components save their state in the following files:

* Project-level: project (`.ipr`) file. However, if the workspace option in the `[plugin.xml](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html)` file is set to `true`, then the workspace (`.iws`) file is used instead.

* Module-level: module (`.iml`) file.
