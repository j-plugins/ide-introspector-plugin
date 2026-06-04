---
id: sdk.persisting-state-of-components.using-persistentstatecomponent
title: Persisting State of Components: Using `PersistentStateComponent
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, using, persistentstatecomponent]
---
Part of `sdk.persisting-state-of-components`.

Using `PersistentStateComponent`

The [PersistentStateComponent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/PersistentStateComponent.java) interface allows for persisting state classes and gives the most flexibility for defining the values to be persisted, their format, and storage location.

To use it:

* mark a [service](https://plugins.jetbrains.com/docs/intellij/plugin-services.html) (project or application-level service for storing project or application data, respectively) as implementing the `PersistentStateComponent` interface

* define the state class

* specify the [storage location](#defining-the-storage-location) using [@State](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/State.java)

Note that instances of extensions can't persist their state by implementing `PersistentStateComponent`.
If an extension needs to have a persistent state, define a separate service responsible for managing that state.

## Subtopics

- Implementing the `PersistentStateComponent` Interface — `sdk.persisting-state-of-components.using-persistentstatecomponent.implementing-the-persistentstatecomponent-interface`
- Implementing the State Class — `sdk.persisting-state-of-components.using-persistentstatecomponent.implementing-the-state-class`
- Defining the Storage Location — `sdk.persisting-state-of-components.using-persistentstatecomponent.defining-the-storage-location`
- Sharing Settings Between IDE Installations — `sdk.persisting-state-of-components.using-persistentstatecomponent.sharing-settings-between-ide-installations`
- Customizing the XML Format of Persisted Values — `sdk.persisting-state-of-components.using-persistentstatecomponent.customizing-the-xml-format-of-persisted-values`
- Migrating Persisted Values — `sdk.persisting-state-of-components.using-persistentstatecomponent.migrating-persisted-values`
- Persistent Component Lifecycle — `sdk.persisting-state-of-components.using-persistentstatecomponent.persistent-component-lifecycle`

> Source: IntelliJ Platform SDK docs — Persisting State of Components: Using `PersistentStateComponent (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
