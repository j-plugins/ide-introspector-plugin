# Implementing the State Class

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

#### Converter Example

```JAVA
class LocalDateTimeConverter extends Converter<LocalDateTime> {
  public LocalDateTime fromString(@NotNull String value) {
    long epochMilli = Long.parseLong(value);
    ZoneId zoneId = ZoneId.systemDefault();
    return Instant.ofEpochMilli(epochMilli)
        .atZone(zoneId)
        .toLocalDateTime();
  }

  public String toString(LocalDateTime value) {
    ZoneId zoneId = ZoneId.systemDefault();
    long toEpochMilli = value.atZone(zoneId)
        .toInstant()
        .toEpochMilli();
    return Long.toString(toEpochMilli);
  }
}
```

Define the converter above in [@OptionTag](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/OptionTag.java) or [@Attribute](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/Attribute.java):

```JAVA
class State {
  @OptionTag(converter = LocalDateTimeConverter.class)
  public LocalDateTime dateTime;
}
```
