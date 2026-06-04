---
id: sdk.persisting-state-of-components
title: Persisting State of Components
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, persisting, state, components]
---
The IntelliJ Platform provides an API that allows components or services to persist their state between restarts of the IDE.
The API allows for persisting simple key-value entries and complex state classes.

Note: Split Mode

[Split plugins](https://plugins.jetbrains.com/docs/intellij/split-mode-and-remote-development.html) may need explicit frontend and backend synchronization metadata in addition to a regular `PersistentStateComponent` implementation.
See [Persistent State Component in Split Mode](https://plugins.jetbrains.com/docs/intellij/persistent-state-in-split-mode.html).

Warning:

For persisting sensitive data like passwords, see [Persisting Sensitive Data](https://plugins.jetbrains.com/docs/intellij/persisting-sensitive-data.html).

## Using `PersistentStateComponent (persisting-state-of-components/using-persistentstatecomponent.md)
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
## Using `PropertiesComponent` for Simple Non-Roamable Persiste (persisting-state-of-components/using-propertiescomponent-for-simple-non-roamable-persiste.md)
## Legacy API (`JDOMExternalizable`) (persisting-state-of-components/legacy-api-jdomexternalizable.md)

> Source: IntelliJ Platform SDK docs — Persisting State of Components (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
