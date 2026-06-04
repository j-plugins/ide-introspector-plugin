---
id: sdk.settings-guide.extension-points-for-settings.declaring-project-settings
title: Settings Guide: Declaring Project Settings
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, declaring, project, settings]
---
Part of `sdk.settings-guide.extension-points-for-settings`.

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

> Source: IntelliJ Platform SDK docs — Settings Guide: Declaring Project Settings (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
