---
id: sdk.settings-guide
title: Settings Guide
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, settings, guide]
---
Settings persistently store states that control the behavior and appearance of IntelliJ Platform-based IDEs.
On this page, the term "Settings" means the same as "Preferences" on some platforms.

Plugins can create and store Settings to capture their configuration in a way that uses the IntelliJ Platform [Persistence Model](https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html).
The User Interface (UI) for these custom Settings can be added to the [IDE Settings dialog](https://www.jetbrains.com/help/idea/settings-preferences-dialog.html).
For [split plugins](https://plugins.jetbrains.com/docs/intellij/split-mode-and-remote-development.html), settings persistence may also require explicit frontend and backend synchronization.
See [Persistent State Component in Split Mode](https://plugins.jetbrains.com/docs/intellij/persistent-state-in-split-mode.html).

Settings can [affect different levels](https://www.jetbrains.com/help/idea/configuring-project-and-ide-settings.html) of scope.
This document describes adding custom Settings at the Project and Application (or Global, IDE) levels.

Note:

See [Settings Tutorial](https://plugins.jetbrains.com/docs/intellij/settings-tutorial.html) for step-by-step instructions for creating a simple set of custom Settings.

Tip:

See [Inspecting Settings](https://plugins.jetbrains.com/docs/intellij/internal-ui-inspector.html#inspecting-settings) on how to gather information in the IDE instance for Settings dialog.

## Extension Points for Settings

Custom Settings implementations are declared in the `[plugin.xml](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html)` file using one of two extension points (EP), depending on the level of the Settings.
Many [attributes](#settings-declaration-attributes) are shared between the EP declarations.

Application and Project Settings typically provide an implementation based on the [Configurable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/Configurable.java) interface because they do not have runtime dependencies.
See [Implementations for Settings Extension Points](#implementations-for-settings-extension-points) for more information.

Note:

For performance reasons, it is recommended to declare as much information as possible about a 'Settings' implementation using attributes in the EP element in the `plugin.xml` descriptor.
If it is not declared, the component must be loaded to retrieve it from the implementation, degrading UI responsiveness.

### Declaring Application Settings

The application-level settings are declared using [com.intellij.applicationConfigurable](https://jb.gg/ipe?extensions=com.intellij.applicationConfigurable) extension point
.

An example `<applicationConfigurable>` EP declaration is shown below.
The declaration indicates the settings are a child of the `tools` settings group, the implementation FQN is `com.example.ApplicationSettingsConfigurable`, the unique ID is the same as the implementation fully qualified name (FQN), and the (non-localized) title displayed to users is "My Application Settings".
See [Settings Declaration Attributes](#settings-declaration-attributes) for more information.

```XML
<extensions defaultExtensionNs="com.intellij">
  <applicationConfigurable
      parentId="tools"
      instance="com.example.ApplicationSettingsConfigurable"
      id="com.example.ApplicationSettingsConfigurable"
      displayName="My Application Settings"/>
</extensions>
```

Tip:

To [localize](https://plugins.jetbrains.com/docs/intellij/providing-translations.html) the display name, instead of the `displayName` attribute, use `key` and `bundle` attributes pointing to a key in a [message bundle](https://plugins.jetbrains.com/docs/intellij/internationalization.html#message-bundles).
The same applies for [projectConfigurable](#declaring-project-settings) extensions.

### Declaring Project Settings

The project-level settings are declared using [com.intellij.projectConfigurable](https://jb.gg/ipe?extensions=com.intellij.projectConfigurable) extension point
.

An example `<projectConfigurable>` EP declaration is shown below.
Similar to the application setting example above, but it includes the additional attribute `nonDefaultProject` indicating these settings do not apply to the [default project](https://www.jetbrains.com/help/idea/configure-project-settings.html#new-default-settings).
See [Settings Declaration Attributes](#settings-declaration-attributes) for details.

```XML
<extensions defaultExtensionNs="com.intellij">
  <projectConfigurable
      parentId="tools"
      instance="com.example.ProjectSettingsConfigurable"
      id="com.example.ProjectSettingsConfigurable"
      displayName="My Project Settings"
      nonDefaultProject="true"/>
</extensions>
```

### Settings Declaration Attributes

Readers are encouraged to review the Javadoc comments for [Configurable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/Configurable.java) because the attribute information applies to `ConfigurableProvider` as well as `Configurable`, as noted.
This section provides some additional clarification of those comments.

#### Table of Attributes

The attributes supported by [com.intellij.applicationConfigurable](https://jb.gg/ipe?extensions=com.intellij.applicationConfigurable) extension point
and [com.intellij.projectConfigurable](https://jb.gg/ipe?extensions=com.intellij.projectConfigurable) extension point
are in the table below:

| Attribute |Required |Attribute  Value   |Implementation  Basis   |
--------------------------------------------------------------------
| `instance` |yes [(1)](#attribute-notes) |FQN of implementation. See [The Configurable Interface](#the-configurable-interface) for more information. |`Configurable` |
| `provider` |yes [(1)](#attribute-notes) |FQN of implementation. See [The ConfigurableProvider Class](#the-configurableprovider-class) for more information. |`ConfigurableProvider` |
| `nonDefaultProject` |yes |Applicable only to the `com.intellij.projectConfigurable` (project Settings) EP.  `true` = show Settings for all projects except the [default project](https://www.jetbrains.com/help/idea/configure-project-settings.html#new-default-settings).  `false` = show Settings for all projects.   |`Configurable` |
| `displayName` |yes [(2)](#attribute-notes) |The non-localized Settings name visible to users, which is needed for the Settings dialog left-side menu.  For a localized visible name omit `displayName` and use the `key` and `bundle` attributes.   |`Configurable`  `ConfigurableProvider`   |
| `key` and `bundle` |yes [(2)](#attribute-notes) |The [localization](https://plugins.jetbrains.com/docs/intellij/internationalization.html#message-bundles) key and bundle for the Settings name visible to users.  For non-localized visible names omit `key` and `bundle` and use `displayName`.   |`Configurable`  `ConfigurableProvider`   |
| `id` |yes |The unique, FQN identifier for this implementation.  The FQN should be based on the plugin `id` to ensure uniqueness.   |`Configurable`  `ConfigurableProvider`   |
| `parentId` |yes |This attribute is used to create a hierarchy of Settings. This component is declared one of the specified `parentId` component's children. Typically used for placing a Settings panel within the Settings Dialog menu. Acceptable values for `parentId` are given in [Values for Parent ID Attribute](#values-for-parent-id-attribute).  `groupId` is deprecated. [(3)](#attribute-notes)   |`Configurable`  `ConfigurableProvider`   |
| `groupWeight` |no |Specifies the weight (stacking order) of this component within the group of a parent configurable component. The default weight is 0, meaning lowest in the order.  If one child in a group or a parent component has non-zero weight, all children will be sorted descending by their weight. If the weights are equal, the components will be sorted ascending by their display name.   |`Configurable`  `ConfigurableProvider`   |
| `dynamic` |no |This component's children are dynamically calculated by calling the `getConfigurables()` method.  Not recommended because it requires loading additional classes while building a Settings tree. If possible, use XML attributes instead.   |`Configurable.Composite` |
| `childrenEPName` |no |Specifies the FQN name of the Extension Point that will be used to calculate the children of this component. |`Configurable` |

##### Attribute Notes

(1) Either `instance` or `provider` must be specified depending on the implementation.

(2) Either `displayName` or `key` and `bundle` must be specified depending on whether the displayed Settings name is localized.

(3) If both `groupId` and `parentId` are specified, a warning is logged. Also, see default entry in [Values for Parent ID Attribute](#values-for-parent-id-attribute).

#### Values for Parent ID Attribute

The table below shows all Settings groups and their corresponding value for the `parentId` attribute.
See the [previous section](#table-of-attributes) for all supported attributes.

| Group |`parentId` Value |Details |
------------------------------------
| Appearance & Behavior |`appearance` |This child group contains Settings to personalize IDE appearance, such as: changing themes and font size. Also, it covers Settings to customize behavior such as keymaps, configuring plugins, and system Settings such as password policies, HTTP proxy, updates, and more. |
| Build, Execution, Deployment |`build` |Child group containing Settings to configure project integration with different build tools, modify the default compiler Settings, manage server access configurations, customize the debugger behavior, etc. |
| Build Integration |`build.tools` |A subgroup of `build`. This subgroup configures project integration with build tools such as Maven, Gradle, or Gant. |
| Editor |`editor` |Child group containing Settings to personalize source code appearance, such as fonts, highlighting styles, indents, etc. It also contains Settings to customize the editor's appearance, such as line numbers, caret placement, tabs, source code inspections, setting up templates, and file encodings. |  |
| Languages and Frameworks |`language` |Child group containing Settings related to specific language frameworks and technologies used in the project. |
| 3rd Party Settings |`tools` |Child group containing Settings to configure integration with third-party applications, specify the SSH Terminal connection Settings, manage server certificates and tasks, configure diagrams layout, etc. |
| Super Parent |`root` |The invisible parent of all existing groups. Not used except for IDEs built on top of the IntelliJ Platform, or extensive suites of Settings. You should not place settings in this group. |
| `other`  Do not use   |default |If neither `parentId` nor `groupId` attribute is set, the component is added to the `other` Settings group. This is undesirable; see `other` group description. |
| Catch-all  Deprecated   |`other` |The IntelliJ Platform no longer uses this group. Do not use this group. Use the `tools` group instead. |
| Project-related Settings  Deprecated   |`project` |The IntelliJ Platform no longer uses this group. It was intended to store some project-related settings. Do not use this group. |

## Implementations for Settings Extension Points

Implementations for [com.intellij.applicationConfigurable](https://jb.gg/ipe?extensions=com.intellij.applicationConfigurable) extension point
and [com.intellij.projectConfigurable](https://jb.gg/ipe?extensions=com.intellij.projectConfigurable) extension point
can have one of two bases:

* The [Configurable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/Configurable.java) interface, which provides a named configurable component with a Swing form. Most Settings providers are based on the `Configurable` interface or one of its sub- or supertypes.

* The [ConfigurableProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/ConfigurableProvider.java) class, which can hide a configurable component from the Settings dialog based on runtime conditions.

### 

The `Configurable` Interface

Many Settings in the `intellij-community` code base implement `Configurable` or one of its subtypes, such as [SearchableConfigurable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/SearchableConfigurable.java).
Readers are encouraged to review the Javadoc comments for `Configurable`.

#### Constructors

Implementations must meet several requirements for constructors.

* Application Settings implementations, declared using the [applicationConfigurable EP](#declaring-application-settings), must have a default constructor with no arguments.

* Project Settings implementations, declared using the [projectConfigurable EP](#declaring-project-settings), must declare a constructor with a single argument of type [Project](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/Project.java).

* Constructor injection (other than for `Project`) is not allowed.

For a `Configurable` implementation correctly declared using an EP, the implementation's constructor is not invoked by the IntelliJ Platform until a user chooses the corresponding Settings `displayName` in the Settings Dialog menu.

Warning:

The IntelliJ Platform may instantiate a `Configurable` implementation on a background thread, so creating Swing components in a constructor can degrade UI responsiveness.

#### 

IntelliJ Platform Interactions with `Configurable`

The instantiation of a generic `Configurable` implementation is documented in the interface file.
A few high-level points are reviewed here:

* The `Configurable.reset()` method is invoked immediately after `Configurable.createComponent()`. Initialization of Setting values in the constructor or `createComponent()` is unnecessary.

* See the [Constructors](#constructors) section for information about when a Settings object is instantiated.

* Once instantiated, a `Configurable` instance's lifetime continues regardless of whether the implementation's Settings are changed, or the user chooses a different entry on the Settings Dialog menu.

* A `Configurable` instance's lifetime ends when OK or Cancel is selected in the Settings Dialog. An instance's `Configurable.disposeUIResources()` is called when the Settings Dialog is closing.

To open the Settings dialog or show a specific `Configurable`, see [ShowSettingsUtil](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/options/ShowSettingsUtil.java).

#### 

`Configurable` Marker Interfaces

Implementations based on `Configurable` can implement marker interfaces, which provide additional flexibility in the implementation.

`Configurable.NoScroll`
: Do not add scroll bars to the form. By default, a plugin's Settings component is put into a scrollable pane.
However, a Settings panel can have a `JTree`, which requires its own `JScrollPane`.
So the `NoScroll` interface should be used to remove the outer `JScrollPane`.

`Configurable.NoMargin`
: Do not add an empty border to the form. By default, an empty border is added for a plugin's Settings component.

`Configurable.Beta`
: (2022.3) Adds Beta label next to settings page title in Settings tree.

#### 

Additional Interfaces Based on `Configurable`

There are classes in the IntelliJ Platform specialized in particular types of Settings.
These subtypes are based on `com.intellij.openapi.options.ConfigurableEP`.
For example, `Settings | Editor | General | Appearance` allows adding Settings via [EditorSmartKeysConfigurableEP](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-impl/src/com/intellij/application/options/editor/EditorSmartKeysConfigurableEP.java)
registered in [com.intellij.editorSmartKeysConfigurable](https://jb.gg/ipe?extensions=com.intellij.editorSmartKeysConfigurable) extension point
.

#### Examples

Existing implementations of `Configurable` in the IntelliJ Platform that can serve as a reference are:

* [ConsoleConfigurable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-impl/src/com/intellij/execution/console/ConsoleConfigurable.java) (application configurable)

* [AutoImportOptionsConfigurable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-impl/src/com/intellij/application/options/editor/AutoImportOptionsConfigurable.kt) (project configurable)

### 

The `ConfigurableProvider` Class

The [ConfigurableProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/ConfigurableProvider.java) class only provides a `Configurable` implementation if its runtime conditions are met.
The IntelliJ Platform first calls the `ConfigurableProvider.canCreateConfigurable()`, which evaluates runtime conditions to determine if Settings changes make sense in the current context.
If the Settings make sense to display, `canCreateConfigurable()` returns `true`.
In that case the IntelliJ Platform calls `ConfigurableProvider.createConfigurable()`, which returns the `Configurable` instance for its Settings implementation.

By choosing not to provide a `Configuration` implementation in some circumstances, the `ConfigurableProvider` opts out of the Settings display and modification process.
The use of `ConfigurableProvider` as a basis for a Settings implementation is declared using [attributes](#table-of-attributes) in the EP declaration.

Examples:

* [RunToolbarSettingsConfigurableProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution-impl/src/com/intellij/execution/runToolbar/RunToolbarSettingsConfigurableProvider.kt)

* [VcsManagerConfigurableProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/vcs-impl/src/com/intellij/openapi/vcs/configurable/VcsManagerConfigurableProvider.java)

> Source: IntelliJ Platform SDK docs — Settings Guide (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
