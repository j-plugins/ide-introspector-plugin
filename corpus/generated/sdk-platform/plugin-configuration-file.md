# Plugin Configuration File

The `plugin.xml` configuration file contains all the information about the plugin, which is displayed in the [plugins' settings dialog](https://www.jetbrains.com/help/idea/managing-plugins.html), and all registered extensions, actions, listeners, etc.
The sections below describe all the elements in detail.

The example `plugin.xml` files can be found in the [IntelliJ SDK Docs Code Samples](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/README.md) repository.

## Additional Plugin Configuration Files

A plugin can contain additional configuration files beside the main `plugin.xml`.
They have the same format, and they are included with the `config-file` attribute of [&lt;depends&gt;](#idea-plugin__depends) elements specifying [plugin dependencies](https://plugins.jetbrains.com/docs/intellij/plugin-dependencies.html).
However, some elements and attributes required in `plugin.xml` are ignored in additional configuration files.
If the requirements differ, the documentation below will state it explicitly.
One use case for additional configuration files is when a plugin provides optional features that are only available in some IDEs and require [certain modules](https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html#modules-specific-to-functionality).

## Useful Resources

Please make sure to follow the guidelines from [Best practices for listing your plugin](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html) for an optimal presentation of your plugin on JetBrains Marketplace.
The Busy Plugin Developers. Episode 2 discusses [5 tips for optimizing JetBrains Marketplace plugin page](https://youtu.be/oB1GA9JeeiY?t=52) in more detail.

See also [Marketing](https://plugins.jetbrains.com/docs/intellij/marketing.html) about widgets and badges.

## Configuration Structure Overview (sdk.plugin-configuration-file.configuration-structure-overview)
## idea-plugin (sdk.plugin-configuration-file.idea-plugin)

> Source: IntelliJ Platform SDK docs — Plugin Configuration File (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
