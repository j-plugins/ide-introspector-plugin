---
id: sdk.persisting-state-of-components.using-persistentstatecomponent.implementing-the-persistentstatecomponent-interface.simplepersistentstatecomponent
title: Persisting State of Components: SimplePersistentStateComponent
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, simplepersistentstatecomponent]
---
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

