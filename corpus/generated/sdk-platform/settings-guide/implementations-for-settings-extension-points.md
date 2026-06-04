# Implementations for Settings Extension Points

Implementations for [com.intellij.applicationConfigurable](https://jb.gg/ipe?extensions=com.intellij.applicationConfigurable) extension point
and [com.intellij.projectConfigurable](https://jb.gg/ipe?extensions=com.intellij.projectConfigurable) extension point
can have one of two bases:

* The [Configurable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/Configurable.java) interface, which provides a named configurable component with a Swing form. Most Settings providers are based on the `Configurable` interface or one of its sub- or supertypes.

* The [ConfigurableProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/ConfigurableProvider.java) class, which can hide a configurable component from the Settings dialog based on runtime conditions.

### The `Configurable` Interface (sdk.settings-guide.implementations-for-settings-extension-points.the-configurable-interface)
### The `ConfigurableProvider` Class

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
