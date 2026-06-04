# Extensions

Extensions are the most common way for a plugin to extend the IntelliJ-based IDE's functionality.
They are implementations of specific interfaces or classes that are [registered](#declaring-extensions) in the plugin descriptor.
Provided extension implementations are called by the platform or other plugins to customize and extend the IDE's functionality.

## Common Extension Use Cases

The following are some of the most common tasks achieved using extensions:

* The [com.intellij.toolWindow](https://jb.gg/ipe?extensions=com.intellij.toolWindow) extension point allows plugins to add [tool windows](https://plugins.jetbrains.com/docs/intellij/tool-windows.html) (panels displayed at the sides of the IDE user interface);

* The [com.intellij.applicationConfigurable](https://jb.gg/ipe?extensions=com.intellij.applicationConfigurable) extension point and [com.intellij.projectConfigurable](https://jb.gg/ipe?extensions=com.intellij.projectConfigurable) extension point allow plugins to add pages to the [Settings dialog](https://plugins.jetbrains.com/docs/intellij/settings.html);

* [Custom language plugins](https://plugins.jetbrains.com/docs/intellij/custom-language-support.html) use many extension points to extend various language support features in the IDE.

There are more than 1700 extension points available in the platform and the bundled plugins, allowing customizing different parts of the IDE behavior.

## Exploring Available Extensions

### Documentation

* [IntelliJ Platform Extension Point and Listener List](https://plugins.jetbrains.com/docs/intellij/intellij-platform-extension-point-list.html)

* [IntelliJ Platform Plugins Extension Point and Listener List](https://plugins.jetbrains.com/docs/intellij/intellij-community-plugins-extension-point-list.html) (bundled plugins in IntelliJ IDEA)

* [Open Source Plugins Extension Point and Listener List](https://plugins.jetbrains.com/docs/intellij/oss-plugins-extension-point-list.html)

Lists for other IDEs are available under Product Specific (for example, [PhpStorm](https://plugins.jetbrains.com/docs/intellij/php-extension-point-list.html)).

### IntelliJ Platform Explorer

Browse usages inside existing implementations of open-source IntelliJ Platform plugins via [IntelliJ Platform Explorer](https://jb.gg/ipe).

### Code Insight

Alternatively (or when using 3rd party extension points), all available extension points for the specified namespace (`defaultExtensionNs`) can be listed using auto-completion inside the [&lt;extensions&gt;](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__extensions) block in `[plugin.xml](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html)`.
Use `View | Quick Documentation` in the lookup list to access more information about the extension point and implementation (if applicable).

See [Explore the IntelliJ Platform API](https://plugins.jetbrains.com/docs/intellij/explore-api.html) for more information and strategies.

## Declaring Extensions (sdk.extensions.declaring-extensions)

> Source: IntelliJ Platform SDK docs — Extensions (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
