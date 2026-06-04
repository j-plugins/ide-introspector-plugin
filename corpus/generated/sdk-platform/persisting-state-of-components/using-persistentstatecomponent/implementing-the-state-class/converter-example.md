---
id: sdk.persisting-state-of-components.using-persistentstatecomponent.implementing-the-state-class.converter-example
title: Persisting State of Components: Converter Example
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, converter, example]
---
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

