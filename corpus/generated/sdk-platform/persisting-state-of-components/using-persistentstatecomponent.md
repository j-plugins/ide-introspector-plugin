# Using `PersistentStateComponent

Using `PersistentStateComponent`

The [PersistentStateComponent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/PersistentStateComponent.java) interface allows for persisting state classes and gives the most flexibility for defining the values to be persisted, their format, and storage location.

To use it:

* mark a [service](https://plugins.jetbrains.com/docs/intellij/plugin-services.html) (project or application-level service for storing project or application data, respectively) as implementing the `PersistentStateComponent` interface

* define the state class

* specify the [storage location](#defining-the-storage-location) using [@State](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/State.java)

Note that instances of extensions can't persist their state by implementing `PersistentStateComponent`.
If an extension needs to have a persistent state, define a separate service responsible for managing that state.

### Implementing the `PersistentStateComponent` Interface (sdk.persisting-state-of-components.using-persistentstatecomponent.implementing-the-persistentstatecomponent-interface)
### Implementing the State Class (sdk.persisting-state-of-components.using-persistentstatecomponent.implementing-the-state-class)
### Defining the Storage Location (sdk.persisting-state-of-components.using-persistentstatecomponent.defining-the-storage-location)
### Sharing Settings Between IDE Installations (sdk.persisting-state-of-components.using-persistentstatecomponent.sharing-settings-between-ide-installations)
### Customizing the XML Format of Persisted Values (sdk.persisting-state-of-components.using-persistentstatecomponent.customizing-the-xml-format-of-persisted-values)
### Migrating Persisted Values

If the underlying persistence model or storage format has changed, a [ConverterProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-impl/src/com/intellij/conversion/ConverterProvider.java) can provide [ProjectConverter](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-impl/src/com/intellij/conversion/ProjectConverter.java), whose `getAdditionalAffectedFiles()` method returns affected files to migrate and performs programmatic migration of stored values.

### Persistent Component Lifecycle

The `PersistentStateComponent.loadState()` method is called in two cases:

1. After the component is created (only if there is some non-default state persisted for the component)

2. After the XML file with the persisted state is changed externally (for example, if the project file was updated from the version control system)

In the latter case, the component is responsible for updating the UI and other related components according to the changed state.

The `PersistentStateComponent.getState()` method is called every time the settings are saved (for example, on frame deactivation or when closing the IDE).
If the state returned from `getState()` is equal to the default state (obtained by creating the state class with a default constructor), nothing is persisted in the XML.
Otherwise, the returned state is serialized in XML and stored.
