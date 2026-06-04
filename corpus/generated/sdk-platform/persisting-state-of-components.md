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

## Subtopics

- Using `PersistentStateComponent — `sdk.persisting-state-of-components.using-persistentstatecomponent`
- Using `PropertiesComponent` for Simple Non-Roamable Persiste — `sdk.persisting-state-of-components.using-propertiescomponent-for-simple-non-roamable-persiste`
- Legacy API (`JDOMExternalizable`) — `sdk.persisting-state-of-components.legacy-api-jdomexternalizable`

> Source: IntelliJ Platform SDK docs — Persisting State of Components (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
