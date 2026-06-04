---
id: sdk.messaging-infrastructure.messaging-api-usage.subscribing-to-a-topic
title: Messaging Infrastructure: Subscribing to a Topic
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, subscribing, topic]
---
```PLANTUML
@startuml

skinparam monochrome true
skinparam DefaultFontName JetBrains Sans
skinparam DefaultFontSize 14
skinparam DefaultTextAlignment center
skinparam ActivityBorderThickness 1

left to right direction

' Define the activity
(*) --> if "" then
  --> [no connection] "Get a message\nbus reference"
  --> "Create\na connection\nto the bus"
  --> "Subscribe"
else
  --> [connection exists] "Subscribe"
endif
--> (*)
@enduml
```

Note:

Use [declarative registration](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html) whenever possible.

```JAVA
project.getMessageBus().connect().subscribe(
    ChangeActionNotifier.CHANGE_ACTION_TOPIC,
    new ChangeActionNotifier() {
        @Override
        public void beforeAction(Context context) {
          // Process 'before action' event.
        }
        @Override
        public void afterAction(Context context) {
          // Process 'after action' event.
        }
});
```

`MessageBus` instances are available via [ComponentManager.getMessageBus()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/extensions/src/com/intellij/openapi/components/ComponentManager.java).
Many standard interfaces implement returning a message bus, e.g., [Application.getMessageBus()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java) and [Project.getMessageBus()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/Project.java).

