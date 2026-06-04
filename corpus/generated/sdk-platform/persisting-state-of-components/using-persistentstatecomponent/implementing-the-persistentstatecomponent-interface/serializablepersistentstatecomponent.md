---
id: sdk.persisting-state-of-components.using-persistentstatecomponent.implementing-the-persistentstatecomponent-interface.serializablepersistentstatecomponent
title: Persisting State of Components: SerializablePersistentStateComponent
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, serializablepersistentstatecomponent]
---
[SerializablePersistentStateComponent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/SerializablePersistentStateComponent.kt) is parameterized with an immutable state data class.

State properties are exposed for reading and modification via persistent state component class' properties.
The state properties are modified by copying the state and overwriting a modified value within `SerializablePersistentStateComponent.updateState()`, which ensures atomic modification and thread safety.

The copy-on-write approach allows for full internal modification tracking.

Example:

```KOTLIN
@Service
@State(...)
class MySettings : SerializablePersistentStateComponent<MySettings.State>(State()) {

  var stringValue: String
    get() = state.stringValue
    set(value) {
      updateState {
        it.copy(stringValue = value)
      }
    }

  data class State (
    @JvmField val stringValue: String = "default value"
  )
}
```

Java:

The implementation of `PersistentStateComponent` must be parameterized with the type of state class.
The state class can either be a separate class or the class implementing `PersistentStateComponent`.

