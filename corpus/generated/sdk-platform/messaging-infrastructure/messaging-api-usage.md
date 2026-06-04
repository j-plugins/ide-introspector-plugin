# Messaging API Usage

The sample below assumes a Project-level topic.

### Defining a Business Interface and a Topic

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

### Subscribing to a Topic

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

### Publishing Messages

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
