# Defining the Storage Location

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
