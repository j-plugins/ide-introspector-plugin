# Extension Points for Settings

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

### Settings Declaration Attributes (sdk.settings-guide.extension-points-for-settings.settings-declaration-attributes)
