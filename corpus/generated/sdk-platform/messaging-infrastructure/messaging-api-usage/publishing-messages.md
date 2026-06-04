---
id: sdk.messaging-infrastructure.messaging-api-usage.publishing-messages
title: Messaging Infrastructure: Publishing Messages
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, publishing, messages]
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
(*) --> "Get message\nbus reference"
  --> "Ask the bus\nfor a particular\ntopic's publisher"
  --> "Call target\nmethod on\npublisher"
  --> "Messaging calls\nthe same method\non target handlers"
--> (*)
@enduml
```

```JAVA
public void doChange(Context context) {
  ChangeActionNotifier publisher = project.getMessageBus()
      .syncPublisher(ChangeActionNotifier.CHANGE_ACTION_TOPIC);
  publisher.beforeAction(context);
  try {
    // do action
  } finally {
    publisher.afterAction(context);
  }
}
```

