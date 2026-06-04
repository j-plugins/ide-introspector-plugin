---
id: sdk.persisting-state-of-components.using-persistentstatecomponent.implementing-the-state-class
title: Persisting State of Components: Implementing the State Class
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, implementing, state, class]
---
The implementation of `PersistentStateComponent` works by serializing public fields, [annotated](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations) private fields (see also [Customizing the XML format of persisted values](#customizing-the-xml-format-of-persisted-values)), and bean properties into an XML format.

To exclude a public field or bean property from serialization, annotate the field or getter with [@Transient](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/Transient.java).

Note that the state class must have a default constructor.
It should return the component's default state: the one used if there is nothing persisted in the XML files yet.

State class should have an `equals()` method, but state objects are compared by fields if it is not implemented.

The following types of values can be persisted:

* numbers (both primitive types, such as `int`, and boxed types, such as `Integer`)

* booleans

* strings

* collections

* maps

* enums

For other types, extend [Converter](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/api.kt).
See the example below.

#### Converter Example (persisting-state-of-components/using-persistentstatecomponent/implementing-the-state-class/converter-example.md)
