---
id: sdk.extensions
title: Extensions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, extensions]
---
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

## Declaring Extensions

Tip:

Auto-completion, Quick Documentation, and other code insight features are available on extension point tags and attributes in `plugin.xml`.

Procedure: Declaring Extension

To clarify this procedure, consider the following sample section of the `plugin.xml` file that defines two extensions designed
to access the [com.intellij.appStarter](https://jb.gg/ipe?extensions=com.intellij.appStarter)
and [com.intellij.projectTemplatesFactory](https://jb.gg/ipe?extensions=com.intellij.projectTemplatesFactory)

extension points in the IntelliJ Platform,
and one extension to access the `another.plugin.myExtensionPoint` extension point in another plugin `another.plugin`:

```XML
<!--
  Declare extensions to access extension points in the IntelliJ Platform.
  These extension points have been declared using "interface".
 -->
<extensions defaultExtensionNs="com.intellij">
  <appStarter
      implementation="com.example.MyAppStarter"/>
  <projectTemplatesFactory
      implementation="com.example.MyProjectTemplatesFactory"/>
</extensions>

<!--
  Declare extensions to access extension points in a custom plugin "another.plugin".
  The "myExtensionPoint" extension point has been declared using "beanClass"
  and exposes custom properties "key" and "implementationClass".
-->
<extensions defaultExtensionNs="another.plugin">
  <myExtensionPoint
      key="keyValue"
      implementationClass="com.example.MyExtensionPointImpl"/>
</extensions>
```

Procedure: Implementing Extension

### Extension Properties Code Insight

Several tooling features are available to help configure bean class extension points in `plugin.xml`.

#### Required Properties

Properties annotated with [RequiredElement](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/extensions/RequiredElement.java) are inserted automatically and validated.

If the given property is allowed to have an explicit empty value, set `allowEmpty` to `true`.

#### Class names

Property names matching the following list will resolve to a fully qualified class name:

* `implementation`

* `className`

* ending with `Class` (case-sensitive)

* `serviceInterface`/`serviceImplementation`

A required parent type can be specified in the [extension point declaration](https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html) via [&lt;with&gt;](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__extensionPoints__extensionPoint__with):

```XML
<extensionPoint name="myExtension" beanClass="MyExtensionBean">
  <with
      attribute="psiElementClass"
      implements="com.intellij.psi.PsiElement"/>
</extensionPoint>
```

#### Custom resolve

Property name `language` (or ending in `*Language`) resolves to all present [Language](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/lang/Language.java) IDs.

Similarly, `action` and `actionId` (2024.3+) resolve to all registered [&lt;action&gt;](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__actions__action) IDs.

#### Deprecation/ApiStatus

Properties marked as `@Deprecated` or annotated with any of [ApiStatus](https://github.com/JetBrains/java-annotations/tree/24.0.0/common/src/main/java/org/jetbrains/annotations/ApiStatus.java) `@Internal`, `@Experimental`, `@ScheduledForRemoval`, or `@Obsolete` will be highlighted accordingly.

#### Enum properties

`Enum` attributes support code insight with lowerCamelCased notation. Note: The `Enum` implementation must not override `toString()`.

#### I18n

Annotating with [@Nls](https://github.com/JetBrains/java-annotations/tree/24.0.0/common/src/main/java/org/jetbrains/annotations/Nls.java) validates a UI `String` capitalization according to the text property `Capitalization` enum value.

> Source: IntelliJ Platform SDK docs — Extensions (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
