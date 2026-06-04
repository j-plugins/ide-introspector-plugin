---
id: sdk.persisting-state-of-components.using-persistentstatecomponent.implementing-the-persistentstatecomponent-interface.persistent-component-with-separate-state-class
title: Persisting State of Components: Persistent Component with Separate State Class
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, persistent, component, with, separate, state]
---
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

