---
id: sdk.messaging-infrastructure.messaging-api-usage.defining-a-business-interface-and-a-topic
title: Messaging Infrastructure: Defining a Business Interface and a Topic
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, defining, business, interface, topic]
---
Create an interface with the business methods and a topic field bound to the business interface:

```JAVA
public interface ChangeActionNotifier {

  @Topic.ProjectLevel
  Topic<ChangeActionNotifier> CHANGE_ACTION_TOPIC =
      Topic.create("custom name", ChangeActionNotifier.class);

  void beforeAction(Context context);
  void afterAction(Context context);
}
```

