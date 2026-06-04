---
id: sdk.extension-points
title: Extension Points
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, extension, points]
---
Note:

See [Plugin Extensions](https://plugins.jetbrains.com/docs/intellij/plugin-extensions.html) for using extension points in your plugin.

By defining extension points in your plugin, you can allow other plugins to extend your plugin's functionality.
There are two types of extension points:

* Interface extension points allow other plugins to extend your plugins with code. When defining an interface extension point, specify an interface, and other plugins will provide classes implementing that interface. The providing plugin can then invoke methods on this interface. In most cases, the interface can be annotated with `@ApiStatus.OverrideOnly` (see [Override-Only API](https://plugins.jetbrains.com/docs/intellij/verifying-plugin-compatibility.html#override-only-api)).

* Bean extension points allow other plugins to extend a plugin with data. Specify the fully qualified name of an extension class, and other plugins will provide data that will be turned into instances of that class.

Procedure: Declaring Extension Point

The plugin that contributes to the extension point will read the specified properties from the `plugin.xml` file.

If extension implementations are filtered according to [dumb mode](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html#dumb-mode), the base class should be
marked with [PossiblyDumbAware](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/PossiblyDumbAware.java) to highlight this.
Use [DumbService.getDumbAwareExtensions()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/DumbService.kt) to retrieve dumb-aware implementations.

Base classes for extensions requiring a key:

* [LanguageExtension](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/lang/LanguageExtension.java)

* [FileTypeExtension](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/fileTypes/FileTypeExtension.java)

* [ClassExtension](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/util/ClassExtension.java)

* [KeyedExtensionCollector](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/util/KeyedExtensionCollector.java)

Note:

See the [Bundling Plugin API Sources](https://plugins.jetbrains.com/docs/intellij/bundling-plugin-openapi-sources.html) section explaining how to expose extension points sources to other plugins.

## Example (extension-points/example.md)
## Using Extension Points (extension-points/using-extension-points.md)
### Error Handling (extension-points/using-extension-points/error-handling.md)
## Dynamic Extension Points (extension-points/dynamic-extension-points.md)

> Source: IntelliJ Platform SDK docs — Extension Points (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
