---
id: sdk.plugin-configuration-file.idea-plugin.extensionpoints
title: Plugin Configuration File: extensionPoints
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, extensionpoints]
---
Part of `sdk.plugin-configuration-file.idea-plugin`.

`extensionPoints`



<tldr>
Reference: [Extension Points](https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html)
</tldr>





Extension points defined by the plugin.



Required
: no


Children
: * [&lt;extensionPoint&gt;](#idea-plugin__extensionPoints__extensionPoint)



#### 
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



##### 
`with`







Specifies the required parent type for class names provided in extension point tags or attributes.
A single [&lt;extensionPoint&gt;](#idea-plugin__extensionPoints__extensionPoint) element can contain
multiple `<with>` elements.



Required
: no


Attributes
: * `tag` (`tag` or `attribute` is required) The name of the tag holding the fully qualified name of the class which parent type will be limited by the type provided in the `implements` attribute. Only one of the `tag` and `attribute` attributes can be specified.

  * `attribute` (`tag` or `attribute` is required) The name of the attribute holding the fully qualified name of the class which parent type will be limited by the type provided in the `implements` attribute. Only one of the `tag` and `attribute` attributes can be specified.

  * `implements` (required) The fully qualified name of the parent type limiting the type provided in the place specified by `tag` or `attribute`.


Example
: An extension point which restricts the type provided in a `myClass` attribute to be an instance
of `com.example.ParentType`, and the type provided in a `someClass` element to be an instance
of `java.lang.Comparable`:
: ```XML
<extensionPoint
    name="myExtension"
    beanClass="com.example.MyExtension">
  <with
      attribute="myClass"
      implements="com.example.ParentType"/>
  <with
      tag="someClass"
      implements="java.lang.Comparable"/>
</extensionPoint>
```
: When using the above extension point, an implementation could be registered as follows:
: ```XML
<myExtension ...
myClass="com.example.MyCustomType">
<someClass>com.example.MyComparable</someClass>
</myExtension>
```
: where:
: * `com.example.MyCustomType` must be a subtype of `com.example.ParentType`

  * `com.example.MyComparable` must be a subtype of `java.lang.Comparable`

> Source: IntelliJ Platform SDK docs — Plugin Configuration File: extensionPoints (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
