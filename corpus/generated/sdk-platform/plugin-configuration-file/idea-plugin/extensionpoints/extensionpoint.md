---
id: sdk.plugin-configuration-file.idea-plugin.extensionpoints.extensionpoint
title: Plugin Configuration File: extensionPoint
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, extensionpoint]
---
`extensionPoint`



<tldr>
Reference: [Declaring Extension Points](https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html#declaring-extension-points)
</tldr>





A single extension point entry of the [&lt;extensionPoints&gt;](#idea-plugin__extensionPoints) defined by the plugin.
A single [&lt;extensionPoints&gt;](#idea-plugin__extensionPoints) element can contain multiple `<extensionPoint>` elements.



Required
: no


Attributes
: * `name` (`name` or `qualifiedName` is required) The extension point name that should be unique in the scope of the plugin, e.g., `myExtension`. The fully qualified name of the extension point is built at runtime by prepending the value of the `name` attribute with the plugin [&lt;id&gt;](#idea-plugin__id) + `.` prefix. Example: when the `name` is `myExtension` and plugin ID is `com.example.myplugin`, the fully qualified name of the EP will be `com.example.myplugin.myExtension`. Only one of the `name` and `qualifiedName` attributes can be specified.

  * `qualifiedName` (`name` or `qualifiedName` is required) The fully qualified name of the extension point. It should be unique between different plugins, and it is recommended to include a plugin ID to guarantee uniqueness, e.g., `com.example.myplugin.myExtension`. Only one of the `name` and `qualifiedName` attributes can be specified.

  * `interface` (`interface` or `beanClass` is required) The fully qualified name of the interface to be implemented for extending the plugin's functionality. Only one of the `interface` and `beanClass` attributes can be specified.

  * `beanClass` (`interface` or `beanClass` is required) The fully qualified name of the extension point bean class providing additional information to the plugin. The bean class specifies one or several properties annotated with the [@Attribute](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/Attribute.java) annotation. Note that bean classes do not follow the JavaBean standard. Implement [PluginAware](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/extensions/src/com/intellij/openapi/extensions/PluginAware.java) to obtain information about the plugin providing the actual extension (see [Error Handling](https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html#error-handling)). Only one of the `interface` and `beanClass` attributes can be specified.

  * `dynamic` (optional) Boolean value defining whether the extension point meets the requirements to be [dynamic](https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html#dynamic-extension-points), which is a prerequisite for [dynamic plugins](https://plugins.jetbrains.com/docs/intellij/dynamic-plugins.html). Default value: `false`.

  * `area` (optional) The scope in which the [extension](https://plugins.jetbrains.com/docs/intellij/plugin-extensions.html) is instantiated. Allowed values: * `IDEA_APPLICATION` (default) * `IDEA_PROJECT` * `IDEA_MODULE` (deprecated) It is strongly recommended not to introduce new project- and module-level extension points. If an extension point needs to operate on a `Project` or `Module` instance, declare an application-level extension point and pass the instance as a method parameter.


Children
: * [&lt;with&gt;](#idea-plugin__extensionPoints__extensionPoint__with)

##### with (plugin-configuration-file/idea-plugin/extensionpoints/extensionpoint/with.md)
