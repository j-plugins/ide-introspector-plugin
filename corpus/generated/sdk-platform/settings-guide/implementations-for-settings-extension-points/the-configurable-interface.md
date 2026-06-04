# The `Configurable` Interface

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

#### IntelliJ Platform Interactions with `Configurable

IntelliJ Platform Interactions with `Configurable`

The instantiation of a generic `Configurable` implementation is documented in the interface file.
A few high-level points are reviewed here:

* The `Configurable.reset()` method is invoked immediately after `Configurable.createComponent()`. Initialization of Setting values in the constructor or `createComponent()` is unnecessary.

* See the [Constructors](#constructors) section for information about when a Settings object is instantiated.

* Once instantiated, a `Configurable` instance's lifetime continues regardless of whether the implementation's Settings are changed, or the user chooses a different entry on the Settings Dialog menu.

* A `Configurable` instance's lifetime ends when OK or Cancel is selected in the Settings Dialog. An instance's `Configurable.disposeUIResources()` is called when the Settings Dialog is closing.

To open the Settings dialog or show a specific `Configurable`, see [ShowSettingsUtil](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/options/ShowSettingsUtil.java).

#### Configurable` Marker Interfaces

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

#### Additional Interfaces Based on `Configurable

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
