---
id: sdk.persisting-state-of-components.using-persistentstatecomponent.implementing-the-persistentstatecomponent-interface
title: Persisting State of Components: Implementing the `PersistentStateComponent` Interface
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, implementing, persistentstatecomponent, interface]
---
Part of `sdk.persisting-state-of-components.using-persistentstatecomponent`.

Implementing the `PersistentStateComponent` Interface

Kotlin:

The recommended approach to implementing a persistent state component in Kotlin is to extend one of the base classes:

1. [SimplePersistentStateComponent](#SimplePersistentStateComponent)

2. [SerializablePersistentStateComponent](#SerializablePersistentStateComponent) (available and recommended since 2022.2)

Both classes implement [PersistentStateComponentWithModificationTracker](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/PersistentStateComponentWithModificationTracker.java) and track modifications count internally (in most cases; see details below).
The `getStateModificationCount()` method helps avoid calling `PersistentStateComponent.getState()` to check whether the state is changed and must be saved.

#### SimplePersistentStateComponent

[SimplePersistentStateComponent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/SimplePersistentStateComponent.kt) is parameterized by a subclass of [BaseState](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/BaseState.kt).
`BaseState` provides a set of handy [property delegates](https://kotlinlang.org/docs/delegated-properties.html), which make it easy to create properties with default values.

Delegates track simple property modifications internally.
Note that incremental collection modification (adding, removing, or modifying collection objects) may require manual `BaseState.incrementModificationCount()` invocation (see its Javadoc for details).

Example:

```KOTLIN
@Service
@State(...)
class MySettings : SimplePersistentStateComponent<MySettings.State>(State()) {
  class State : BaseState() {
    var value by string("default value")
  }
}
```

#### SerializablePersistentStateComponent

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

#### Persistent Component with Separate State Class

In this case, the state class instance is typically stored as a field in the `PersistentStateComponent` class.
When the state is loaded from the storage, it is assigned to the state field (see `loadState()`):

```JAVA
@Service
@State(...)
class MySettings implements PersistentStateComponent<MySettings.State> {

  static class State {
    public String value;
  }

  private State myState = new State();

  @Override
  public State getState() {
    return myState;
  }

  @Override
  public void loadState(State state) {
    myState = state;
  }
}
```

Using a separate state class is the recommended approach.

#### Persistent Component Being a State Class

In this case, `getState()` returns the component itself, and `loadState()` copies properties of the state loaded from storage to the component instance:

```JAVA
@Service
@State(...)
class MySettings implements PersistentStateComponent<MySettings> {

  public String stateValue;

  @Override
  public MySettings getState() {
    return this;
  }

  @Override
  public void loadState(MySettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
```

> Source: IntelliJ Platform SDK docs — Persisting State of Components: Implementing the `PersistentStateComponent` Interface (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
