---
id: sdk.extension-points.using-extension-points
title: Extension Points: Using Extension Points
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, using, extension, points]
---
Part of `sdk.extension-points`.

To refer to all registered extension instances at runtime, declare an [ExtensionPointName](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/extensions/src/com/intellij/openapi/extensions/ExtensionPointName.kt) with private visibility passing in the fully qualified name matching its [declaration in plugin.xml](#declaring-extension-points).
If needed, provide a public method to query registered extensions (Sample: [TestSourcesFilter.isTestSources()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/projectModel-api/src/com/intellij/openapi/roots/TestSourcesFilter.java)).

`myPlugin/src/com/myplugin/MyExtensionUsingService.java`

```JAVA
@Service
public final class MyExtensionUsingService {

  private static final ExtensionPointName<MyBeanClass> EP_NAME =
      ExtensionPointName.create("my.plugin.myExtensionPoint1");

  public void useRegisteredExtensions() {
    for (MyBeanClass extension : EP_NAME.getExtensionList()) {
      String key = extension.getKey();
      String clazz = extension.getClass();
      // ...
    }
  }

}
```

A gutter icon for the `ExtensionPointName` declaration allows navigating to the corresponding [&lt;extensionPoint&gt;](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__extensionPoints__extensionPoint) declaration in `plugin.xml`.
Code insight is available for the extension point name String literal (2022.3).

### Error Handling

When processing extension implementations or registrations, there might be errors, compatibility and configuration issues.
Use [PluginException](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/diagnostic/PluginException.java) to log and correctly attribute the causing plugin for
[builtin error reporting](https://plugins.jetbrains.com/docs/intellij/ide-infrastructure.html#error-reporting).

To report use of a deprecated API, use `PluginException.reportDeprecatedUsage()` methods.

Examples:

* [CompositeFoldingBuilder.assertSameFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/lang/folding/CompositeFoldingBuilder.java)

* [InspectionProfileEntry.getDisplayName()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInspection/InspectionProfileEntry.java)

> Source: IntelliJ Platform SDK docs — Extension Points: Using Extension Points (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
