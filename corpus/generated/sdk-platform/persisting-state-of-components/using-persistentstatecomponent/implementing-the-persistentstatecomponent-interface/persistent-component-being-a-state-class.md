---
id: sdk.persisting-state-of-components.using-persistentstatecomponent.implementing-the-persistentstatecomponent-interface.persistent-component-being-a-state-class
title: Persisting State of Components: Persistent Component Being a State Class
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, persistent, component, being, state, class]
---
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

