---
id: sdk.persisting-state-of-components
title: Persisting State of Components
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, persisting, state, components]
---
The IntelliJ Platform provides an API that allows components or services to persist their state between restarts of the IDE.
The API allows for persisting simple key-value entries and complex state classes.

Note: Split Mode

[Split plugins](https://plugins.jetbrains.com/docs/intellij/split-mode-and-remote-development.html) may need explicit frontend and backend synchronization metadata in addition to a regular `PersistentStateComponent` implementation.
See [Persistent State Component in Split Mode](https://plugins.jetbrains.com/docs/intellij/persistent-state-in-split-mode.html).

Warning:

For persisting sensitive data like passwords, see [Persisting Sensitive Data](https://plugins.jetbrains.com/docs/intellij/persisting-sensitive-data.html).

## 

Using `PersistentStateComponent`

The [PersistentStateComponent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/PersistentStateComponent.java) interface allows for persisting state classes and gives the most flexibility for defining the values to be persisted, their format, and storage location.

To use it:

* mark a [service](https://plugins.jetbrains.com/docs/intellij/plugin-services.html) (project or application-level service for storing project or application data, respectively) as implementing the `PersistentStateComponent` interface

* define the state class

* specify the [storage location](#defining-the-storage-location) using [@State](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/State.java)

Note that instances of extensions can't persist their state by implementing `PersistentStateComponent`.
If an extension needs to have a persistent state, define a separate service responsible for managing that state.

### 

Implementing the `PersistentStateComponent` Interface

Kotlin:

The recommended approach to implementing a persistent state component in Kotlin is to extend one of the base classes:

1. [SimplePersistentStateComponent](#SimplePersistentStateComponent)

2. [SerializablePersistentStateComponent](#SerializablePersistentStateComponent) (available and recommended since 2022.2)

Both classes implement [PersistentStateComponentWithModificationTracker](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/PersistentStateComponentWithModificationTracker.java) and track modifications count internally (in most cases; see details below).
The `getStateModificationCount()` method helps avoid calling `PersistentStateComponent.getState()` to check whether the state is changed and must be saved.

#### SimplePersistentStateComponent

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

#### SerializablePersistentStateComponent

[SerializablePersistentStateComponent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/SerializablePersistentStateComponent.kt) is parameterized with an immutable state data class.

State properties are exposed for reading and modification via persistent state component class' properties.
The state properties are modified by copying the state and overwriting a modified value within `SerializablePersistentStateComponent.updateState()`, which ensures atomic modification and thread safety.

The copy-on-write approach allows for full internal modification tracking.

Example:

```KOTLIN
@Service
@State(...)
class MySettings : SerializablePersistentStateComponent<MySettings.State>(State()) {

  var stringValue: String
    get() = state.stringValue
    set(value) {
      updateState {
        it.copy(stringValue = value)
      }
    }

  data class State (
    @JvmField val stringValue: String = "default value"
  )
}
```

Java:

The implementation of `PersistentStateComponent` must be parameterized with the type of state class.
The state class can either be a separate class or the class implementing `PersistentStateComponent`.

#### Persistent Component with Separate State Class

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

#### Persistent Component Being a State Class

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

### Implementing the State Class

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

### Defining the Storage Location

To specify where precisely the persisted values are stored, add the `@State` annotation to the `PersistentStateComponent` class.

It has the following fields:

* `name` (required) – specifies the name of the state (name of the root tag in XML).

* `storages` – one or more of [@Storage](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/Storage.java) annotations to specify the storage locations. Optional for project-level values – a standard project file is used in this case.

* `reloadable` (optional) – if set to false, a full project (or application) reload is required when the XML file is changed externally, and the state has changed.

The simplest ways of specifying the `@Storage` annotation are as follows:

* `@Storage(StoragePathMacros.WORKSPACE_FILE)` – for values stored in the project workspace file (project-level components only).

* `@Storage("yourName.xml")` – if a component is project-level, for `.ipr`-based projects, a standard project file is used automatically, and there is no need to specify anything.

The state is persisted in a separate file by specifying a different setting for the `value` parameter, which was the `file` parameter before 2016.x.

Note:

For application-level storage, it is strongly recommended to use a custom file.
Using of `other.xml` is deprecated.

When planning your storage location, consider its intended purpose.
A project-level custom file should be preferred for storing plugin settings.
To store cached values, use `@Storage(StoragePathMacros.CACHE_FILE)`.
Refer to [StoragePathMacros](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/components/StoragePathMacros.java) for commonly used macros.

The `roamingType` parameter of the `@Storage` annotation specifies the roaming type when the [settings are shared](#sharing-settings-between-ide-installations):

* `RoamingType.DEFAULT` – settings are shared

* `RoamingType.PER_OS` – settings are shared per operating system

* `RoamingType.DISABLED` – settings sharing is disabled

Warning:

If there are multiple components that store state in the same file, they must have the same `roamingType` attribute value.

### Sharing Settings Between IDE Installations

<tldr>
Product Help: [Share IDE settings](https://www.jetbrains.com/help/idea/sharing-your-ide-settings.html)
</tldr>

It is possible to share the persistent state of components between different IDE installations.
This allows users to have the same settings on every development machine or to share their settings within a team.

Settings can be shared via the following functionalities:

* [Backup and Sync](https://www.jetbrains.com/help/idea/sharing-your-ide-settings.html#IDE_settings_sync) (formerly Settings Sync) plugin that allows synchronizing settings on JetBrains servers. Users can select the category of settings that are synchronized.

* [Settings Repository](https://www.jetbrains.com/help/idea/sharing-your-ide-settings.html#settings-repository) plugin that allows synchronizing settings in a Git repository created and configured by a user.

* [Export Settings](https://www.jetbrains.com/help/idea/sharing-your-ide-settings.html#import-export-settings) feature that allows for the manual import and export of settings.

Tip:

Synchronization via the Backup and Sync or Settings Repository plugins only works when these plugins are installed and enabled.

The decision about making a specific component's state shareable should be made carefully.
Only the settings that aren't specific to a given machine should be shared, for example, paths to user-specific directories shouldn't be shared.
If a component contains both shareable and non-shareable data, it should be split into two separate components.

#### Backup and Sync Plugin

Tip:

The Settings Sync plugin has been renamed to Backup and Sync in 2024.3.

To include a plugin's component state in the Backup and Sync plugin synchronization, the following requirements must be met:

* The `RoamingType` is defined via the `roamingType` attribute of the `@Storage` annotation and is not equal to `DISABLED`.

* The `SettingsCategory` is defined via the `category` attribute of the `@State` annotation and is not equal to `OTHER`.

* There is no other `PersistentStateComponent`, which is stored in the same XML file and has a different `RoamingType`.

If the component state is OS-dependent, the `roamingType` of the `@Storage` annotation must be set to `RoamingType.PER_OS`.

Warning:

Note that `other.xml` file is non-roamable and declaring it in the `@Storage` annotation will disable roaming of the component state.
It is recommended to use a separate XML file for the component or use another existing storage file.

#### Settings Repository Plugin and Export Settings Feature

Warning:

The Settings Repository plugin is unbundled starting with version 2022.3 and will be no longer maintained.

Persistent components can be shared via the Settings Repository plugin and Export Settings feature, depending on the `roamingType` of the `@Storage` annotation.
See the [Defining the Storage Location](#defining-the-storage-location) for more details.

### Customizing the XML Format of Persisted Values

Note:

Consider using annotation parameters only to achieve backward compatibility.
Otherwise, feel free to file issues about specific serialization cosmetics.

If you want to use the default bean serialization but need to customize the storage format in XML (for example, for compatibility with previous versions of a plugin or externally defined XML formats), use the
[@Tag](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/Tag.java),
[@Attribute](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/Attribute.java),
[@Property](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/Property.java),
[@MapAnnotation](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/MapAnnotation.java),
[@XMap](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/XMap.java),
and [@XCollection](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/XCollection.java)
annotations.

If the state to serialize doesn't map cleanly to a JavaBean, then `org.jdom.Element` can be used as the state class.
In that case, use the `getState()` method to build an XML element with an arbitrary structure, which then is saved directly in the state XML file.
In the `loadState()` method, deserialize the JDOM element tree using any custom logic.
This is not recommended and should be avoided whenever possible.

To disable the expansion of path macros ([PathMacro](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/macro/src/com/intellij/ide/macro/PathMacro.java))
in stored values, implement [PathMacroFilter](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/jps/model-serialization/src/com/intellij/openapi/application/PathMacroFilter.java)
and register in [com.intellij.pathMacroFilter](https://jb.gg/ipe?extensions=com.intellij.pathMacroFilter) extension point
.

### Migrating Persisted Values

If the underlying persistence model or storage format has changed, a [ConverterProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-impl/src/com/intellij/conversion/ConverterProvider.java) can provide [ProjectConverter](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-impl/src/com/intellij/conversion/ProjectConverter.java), whose `getAdditionalAffectedFiles()` method returns affected files to migrate and performs programmatic migration of stored values.

### Persistent Component Lifecycle

The `PersistentStateComponent.loadState()` method is called in two cases:

1. After the component is created (only if there is some non-default state persisted for the component)

2. After the XML file with the persisted state is changed externally (for example, if the project file was updated from the version control system)

In the latter case, the component is responsible for updating the UI and other related components according to the changed state.

The `PersistentStateComponent.getState()` method is called every time the settings are saved (for example, on frame deactivation or when closing the IDE).
If the state returned from `getState()` is equal to the default state (obtained by creating the state class with a default constructor), nothing is persisted in the XML.
Otherwise, the returned state is serialized in XML and stored.

## 

Using `PropertiesComponent` for Simple Non-Roamable Persistence

If the plugin needs to persist a few simple values, the easiest way to do so is to use the [PropertiesComponent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/ide/util/PropertiesComponent.java) service.
It can save both application-level values and project-level values in the workspace file.
Roaming is disabled for `PropertiesComponent`, so use it only for temporary, non-roamable properties.

Use the `PropertiesComponent.getInstance()` method for storing application-level values and the `PropertiesComponent.getInstance(Project)` method for storing project-level values.

Since all plugins share the same namespace, it is highly recommended to prefix key names (for example, using plugin ID `com.example.myCustomSetting`).

## 

Legacy API (`JDOMExternalizable`)

Older components use the [JDOMExternalizable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/util/JDOMExternalizable.java) interface for persisting state.
It uses the `readExternal()` method for reading the state from a JDOM element, and `writeExternal()` to write the state.

Implementations can manually store the state in attributes and sub-elements or use the [DefaultJDOMExternalizer](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/util/DefaultJDOMExternalizer.java) class to store the values of all public fields automatically.

Components save their state in the following files:

* Project-level: project (`.ipr`) file. However, if the workspace option in the `[plugin.xml](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html)` file is set to `true`, then the workspace (`.iws`) file is used instead.

* Module-level: module (`.iml`) file.

> Source: IntelliJ Platform SDK docs — Persisting State of Components (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
