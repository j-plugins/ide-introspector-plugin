---
id: sdk.settings-guide.extension-points-for-settings.declaring-application-settings
title: Settings Guide: Declaring Application Settings
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, declaring, application, settings]
---
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

