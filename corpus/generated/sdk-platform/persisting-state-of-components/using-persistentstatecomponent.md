---
id: sdk.persisting-state-of-components.using-persistentstatecomponent
title: Persisting State of Components: Using `PersistentStateComponent
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, using, persistentstatecomponent]
---
Using `PersistentStateComponent`

The [PersistentStateComponent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/PersistentStateComponent.java) interface allows for persisting state classes and gives the most flexibility for defining the values to be persisted, their format, and storage location.

To use it:

* mark a [service](https://plugins.jetbrains.com/docs/intellij/plugin-services.html) (project or application-level service for storing project or application data, respectively) as implementing the `PersistentStateComponent` interface

* define the state class

* specify the [storage location](#defining-the-storage-location) using [@State](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/State.java)

Note that instances of extensions can't persist their state by implementing `PersistentStateComponent`.
If an extension needs to have a persistent state, define a separate service responsible for managing that state.

### Implementing the `PersistentStateComponent` Interface (persisting-state-of-components/using-persistentstatecomponent/implementing-the-persistentstatecomponent-interface.md)
#### SimplePersistentStateComponent (persisting-state-of-components/using-persistentstatecomponent/implementing-the-persistentstatecomponent-interface/simplepersistentstatecomponent.md)
#### SerializablePersistentStateComponent (persisting-state-of-components/using-persistentstatecomponent/implementing-the-persistentstatecomponent-interface/serializablepersistentstatecomponent.md)
#### Persistent Component with Separate State Class (persisting-state-of-components/using-persistentstatecomponent/implementing-the-persistentstatecomponent-interface/persistent-component-with-separate-state-class.md)
#### Persistent Component Being a State Class (persisting-state-of-components/using-persistentstatecomponent/implementing-the-persistentstatecomponent-interface/persistent-component-being-a-state-class.md)
### Implementing the State Class (persisting-state-of-components/using-persistentstatecomponent/implementing-the-state-class.md)
#### Converter Example (persisting-state-of-components/using-persistentstatecomponent/implementing-the-state-class/converter-example.md)
### Defining the Storage Location (persisting-state-of-components/using-persistentstatecomponent/defining-the-storage-location.md)
### Sharing Settings Between IDE Installations (persisting-state-of-components/using-persistentstatecomponent/sharing-settings-between-ide-installations.md)
#### Backup and Sync Plugin (persisting-state-of-components/using-persistentstatecomponent/sharing-settings-between-ide-installations/backup-and-sync-plugin.md)
#### Settings Repository Plugin and Export Settings Feature (persisting-state-of-components/using-persistentstatecomponent/sharing-settings-between-ide-installations/settings-repository-plugin-and-export-settings-feature.md)
### Customizing the XML Format of Persisted Values (persisting-state-of-components/using-persistentstatecomponent/customizing-the-xml-format-of-persisted-values.md)
### Migrating Persisted Values (persisting-state-of-components/using-persistentstatecomponent/migrating-persisted-values.md)
### Persistent Component Lifecycle (persisting-state-of-components/using-persistentstatecomponent/persistent-component-lifecycle.md)
