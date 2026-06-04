---
id: sdk.persisting-state-of-components.using-persistentstatecomponent.implementing-the-persistentstatecomponent-interface
title: Persisting State of Components: Implementing the `PersistentStateComponent` Interface
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, implementing, persistentstatecomponent, interface]
---
Implementing the `PersistentStateComponent` Interface

Kotlin:

The recommended approach to implementing a persistent state component in Kotlin is to extend one of the base classes:

1. [SimplePersistentStateComponent](#SimplePersistentStateComponent)

2. [SerializablePersistentStateComponent](#SerializablePersistentStateComponent) (available and recommended since 2022.2)

Both classes implement [PersistentStateComponentWithModificationTracker](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/PersistentStateComponentWithModificationTracker.java) and track modifications count internally (in most cases; see details below).
The `getStateModificationCount()` method helps avoid calling `PersistentStateComponent.getState()` to check whether the state is changed and must be saved.

#### SimplePersistentStateComponent (persisting-state-of-components/using-persistentstatecomponent/implementing-the-persistentstatecomponent-interface/simplepersistentstatecomponent.md)
#### SerializablePersistentStateComponent (persisting-state-of-components/using-persistentstatecomponent/implementing-the-persistentstatecomponent-interface/serializablepersistentstatecomponent.md)
#### Persistent Component with Separate State Class (persisting-state-of-components/using-persistentstatecomponent/implementing-the-persistentstatecomponent-interface/persistent-component-with-separate-state-class.md)
#### Persistent Component Being a State Class (persisting-state-of-components/using-persistentstatecomponent/implementing-the-persistentstatecomponent-interface/persistent-component-being-a-state-class.md)
