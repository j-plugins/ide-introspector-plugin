---
id: sdk.persisting-state-of-components.using-persistentstatecomponent.persistent-component-lifecycle
title: Persisting State of Components: Persistent Component Lifecycle
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, persistent, component, lifecycle]
---
The `PersistentStateComponent.loadState()` method is called in two cases:

1. After the component is created (only if there is some non-default state persisted for the component)

2. After the XML file with the persisted state is changed externally (for example, if the project file was updated from the version control system)

In the latter case, the component is responsible for updating the UI and other related components according to the changed state.

The `PersistentStateComponent.getState()` method is called every time the settings are saved (for example, on frame deactivation or when closing the IDE).
If the state returned from `getState()` is equal to the default state (obtained by creating the state class with a default constructor), nothing is persisted in the XML.
Otherwise, the returned state is serialized in XML and stored.

