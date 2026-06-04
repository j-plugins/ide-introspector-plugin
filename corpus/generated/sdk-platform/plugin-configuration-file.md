---
id: sdk.plugin-configuration-file
title: Plugin Configuration File
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, plugin, configuration, file]
---
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

## Configuration Structure Overview

Warning: Private Configuration Elements

If an element or an attribute is not documented on this page, consider them as configuration items intended to be used by JetBrains only.
They must not be used by third-party plugins.

Deprecated elements are omitted in the list below.

Note:

Elements described on this page are available in [quick
documentation](https://www.jetbrains.com/help/idea/viewing-reference-information.html#inline-quick-documentation) since IntelliJ IDEA 2025.1.

The [Plugin DevKit](https://plugins.jetbrains.com/plugin/22851-plugin-devkit) plugin must be
installed and enabled.

* [&lt;idea-plugin&gt;](#idea-plugin) * [&lt;id&gt;](#idea-plugin__id) * [&lt;name&gt;](#idea-plugin__name) * [&lt;version&gt;](#idea-plugin__version) * [&lt;product-descriptor&gt;](#idea-plugin__product-descriptor) * [&lt;idea-version&gt;](#idea-plugin__idea-version) * [&lt;vendor&gt;](#idea-plugin__vendor) * [&lt;description&gt;](#idea-plugin__description) * [&lt;change-notes&gt;](#idea-plugin__change-notes) * [&lt;depends&gt;](#idea-plugin__depends) * [&lt;incompatible-with&gt;](#idea-plugin__incompatible-with) * [&lt;extensions&gt;](#idea-plugin__extensions) * [An Extension](#idea-plugin__extensions__-) * [&lt;extensionPoints&gt;](#idea-plugin__extensionPoints) * [&lt;extensionPoint&gt;](#idea-plugin__extensionPoints__extensionPoint) * [&lt;with&gt;](#idea-plugin__extensionPoints__extensionPoint__with) * [&lt;resource-bundle&gt;](#idea-plugin__resource-bundle) * [&lt;actions&gt;](#idea-plugin__actions) * [&lt;action&gt;](#idea-plugin__actions__action) * [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group) * [&lt;keyboard-shortcut&gt;](#idea-plugin__actions__action__keyboard-shortcut) * [&lt;mouse-shortcut&gt;](#idea-plugin__actions__action__mouse-shortcut) * [&lt;override-text&gt;](#idea-plugin__actions__action__override-text) * [&lt;synonym&gt;](#idea-plugin__actions__action__synonym) * [&lt;abbreviation&gt;](#idea-plugin__actions__action__abbreviation) * [&lt;group&gt;](#idea-plugin__actions__group) * [&lt;action&gt;](#idea-plugin__actions__action) * [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group) * [&lt;keyboard-shortcut&gt;](#idea-plugin__actions__action__keyboard-shortcut) * [&lt;mouse-shortcut&gt;](#idea-plugin__actions__action__mouse-shortcut) * [&lt;override-text&gt;](#idea-plugin__actions__action__override-text) * [&lt;synonym&gt;](#idea-plugin__actions__action__synonym) * [&lt;abbreviation&gt;](#idea-plugin__actions__action__abbreviation) * [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group) * [&lt;override-text&gt;](#idea-plugin__actions__action__override-text) * [&lt;reference&gt;](#idea-plugin__actions__group__reference) * [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group) * [&lt;separator&gt;](#idea-plugin__actions__group__separator) * [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group) * [&lt;reference&gt;](#idea-plugin__actions__group__reference) * [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group) * [&lt;applicationListeners&gt;](#idea-plugin__applicationListeners) * [&lt;listener&gt;](#idea-plugin__applicationListeners__listener) * [&lt;projectListeners&gt;](#idea-plugin__projectListeners) * [&lt;listener&gt;](#idea-plugin__applicationListeners__listener) * [&lt;xi:include&gt;](#idea-plugin__xi:include) * [&lt;xi:fallback&gt;](#idea-plugin__xi:include__xi:fallback)

## 

`idea-plugin`

The `plugin.xml` file root element.

Required
: yes

Attributes
: * `url` (optional; ignored in an [additional config file](#additional-plugin-configuration-files)) The link to the plugin homepage displayed on the plugin page in the [JetBrains Marketplace](https://plugins.jetbrains.com).

* `require-restart` (optional) The boolean value determining whether the plugin installation, update, or uninstallation requires an IDE restart (see [Dynamic Plugins](https://plugins.jetbrains.com/docs/intellij/dynamic-plugins.html) for details). Default value: `false`.

Children
: * [&lt;actions&gt;](#idea-plugin__actions)

* [&lt;applicationListeners&gt;](#idea-plugin__applicationListeners)

* [&lt;change-notes&gt;](#idea-plugin__change-notes)

* [&lt;depends&gt;](#idea-plugin__depends)

* [&lt;description&gt;](#idea-plugin__description)

* [&lt;extensionPoints&gt;](#idea-plugin__extensionPoints)

* [&lt;extensions&gt;](#idea-plugin__extensions)

* [&lt;id&gt;](#idea-plugin__id)

* [&lt;idea-version&gt;](#idea-plugin__idea-version)

* [&lt;incompatible-with&gt;](#idea-plugin__incompatible-with)

* [&lt;name&gt;](#idea-plugin__name)

* [&lt;product-descriptor&gt;](#idea-plugin__product-descriptor)

* [&lt;projectListeners&gt;](#idea-plugin__projectListeners)

* [&lt;resource-bundle&gt;](#idea-plugin__resource-bundle)

* [&lt;vendor&gt;](#idea-plugin__vendor)

* [&lt;version&gt;](#idea-plugin__version)

* [&lt;xi:include&gt;](#idea-plugin__xi:include)

* [&lt;application-components&gt;](#idea-plugin__application-components) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

* [&lt;module-components&gt;](#idea-plugin__module-components) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

* [&lt;project-components&gt;](#idea-plugin__project-components) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

### 

`id`

A unique identifier of the plugin.
It should be a fully qualified name similar to Java packages and must not collide with the ID of existing plugins.
The ID is a technical value used to identify the plugin in the IDE and [JetBrains Marketplace](https://plugins.jetbrains.com).
Please use characters, numbers, and `'.'`/`'-'`/`'_'` symbols only and keep it reasonably short.

Warning:

Make sure to pick a stable ID, as the value cannot be changed later after public release.

Required
: no; ignored in an [additional config file](#additional-plugin-configuration-files)

It is highly recommended to set in `plugin.xml` file.

The element can be skipped in the source `plugin.xml` file if the Gradle plugin `patchPluginXml` task
([2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#patchPluginXml),
[1.x](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-patchpluginxml))
is enabled and configured.

Default value
: Value of the [&lt;name&gt;](#idea-plugin__name) element.

Example
: ```XML
<id>com.example.framework</id>
```





### 
`name`



<tldr>
Reference: [JetBrains Marketplace: Plugin Name](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html#plugin-name)
</tldr>





The user-visible plugin display name (Title Case).



Required
: yes; ignored in an [additional config file](#additional-plugin-configuration-files)


Example
: ```XML
<name>My Framework Support</name>
```

### 

`version`

<tldr>
Reference: [JetBrains Marketplace: Semantic Versioning](https://plugins.jetbrains.com/docs/marketplace/semver.html)
</tldr>

The plugin version displayed in the Plugins settings dialog and on the
[JetBrains Marketplace](https://plugins.jetbrains.com) plugin page.
Plugins uploaded to the JetBrains Marketplace must follow semantic versioning.

Required
: yes; ignored in an [additional config file](#additional-plugin-configuration-files)

The element can be skipped in the source `plugin.xml` file if the Gradle plugin `patchPluginXml` task
([2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#patchPluginXml),
[1.x](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-patchpluginxml))
is enabled and configured.

Example
: ```XML
<version>1.3.18</version>
```





### 
`product-descriptor`



<tldr>
Reference: [JetBrains Marketplace: How to add required parameters for paid plugins](https://plugins.jetbrains.com/docs/marketplace/add-required-parameters.html)
</tldr>





[Paid](https://plugins.jetbrains.com/build-and-market) or
[Freemium](https://plugins.jetbrains.com/docs/marketplace/freemium.html) plugin descriptor.



Required
: only for paid or freemium plugins; ignored in an [additional config file](#additional-plugin-configuration-files)

Do not add `<product-descriptor>` element in a free plugin.


Attributes
: * `code` (required) The plugin product code used in the JetBrains Sales System. The code must be agreed with JetBrains in advance and follow [the requirements](https://plugins.jetbrains.com/docs/marketplace/add-required-parameters.html).

  * `release-date` (required) Date of the major version release in the `YYYYMMDD` format.

  * `release-version` (required) A major version in a specific number format, for example, `20242` for the 2024.2 major release. See [release-version constraints](https://plugins.jetbrains.com/docs/marketplace/versioning-of-paid-plugins.html#release-version-constraints) for more details.

  * `optional` (optional) The boolean value determining whether the plugin is a [Freemium](https://plugins.jetbrains.com/docs/marketplace/freemium.html) plugin. Default value: `false`.

  * `eap` (optional) Specifies the boolean value determining whether the plugin is an EAP release. Default value: `false`.





### 
`idea-version`



<tldr>
Reference: [Build Number Ranges](https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html)
</tldr>





The plugin's range of compatible IntelliJ-based IDE versions.



Required
: yes; ignored in an [additional config file](#additional-plugin-configuration-files)

The element can be skipped in the source `plugin.xml` file if the Gradle plugin `patchPluginXml` task
([2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#patchPluginXml),
[1.x](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-patchpluginxml))
is enabled and configured.


Attributes
: * `since-build` (required) The lowest IDE version compatible with the plugin.

  * `until-build` (optional) The highest version of the IDE the plugin is compatible with. It's highly recommended not to set this attribute, so the plugin will be compatible with all IDE versions since the version specified by the `since-build`. If it becomes necessary to specify the highest compatible IDE version later, it'll be possible to do that via JetBrains Marketplace. Only if the publishing process for the plugin is configured to upload a new version for each major IDE version, it makes sense to limit the highest compatible IDE version from the beginning. In that case, use `strict-until-build` instead.

  * `strict-until-build` (optional; available since 2025.3) The highest version of the IDE the plugin is compatible with. Use this attribute only if the publishing process for the plugin is configured to upload a new version for each major IDE version. Otherwise, skip this attribute. If it becomes necessary to specify the highest compatible IDE version later, it'll be possible to do that via JetBrains Marketplace.


Examples
: * Compatibility with a specific build number (2021.3.3) and higher versions: ```XML <idea-version since-build="213.7172.25"/> ```

  * Compatibility with versions from any of `213` branches to any of `221` branches: ```XML <idea-version since-build="213" until-build="221.*"/> ```





### 
`vendor`



<tldr>
Reference: [JetBrains Marketplace: Contacts and Resources](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html#contacts-and-resources)
</tldr>





The vendor name or organization ID (if created) in the Plugins settings dialog and on
the [JetBrains Marketplace](https://plugins.jetbrains.com) plugin page.



Required
: yes; ignored in an [additional config file](#additional-plugin-configuration-files)


Attributes
: * `url` (optional) The URL to the vendor's homepage. Supports `https://` and `http://` scheme links.

  * `email` (optional) The vendor's email address.


Examples
: * Personal vendor with an email address provided: ```XML <vendor email="joe@example.com">Joe Doe</vendor> ```

  * Organizational vendor with a website URL and email address provided: ```XML <vendor url="https://mycompany.example.com" email="contact@example.com"> My Company </vendor> ```





### 
`description`



<tldr>
Reference: [JetBrains Marketplace: Plugin Description](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html#plugin-description)
</tldr>





The plugin description displayed on the [JetBrains Marketplace](https://plugins.jetbrains.com) plugin page and in
the Plugins settings dialog.


Simple HTML elements, like text formatting, paragraphs, lists, etc., are allowed and must be wrapped into
`<![CDATA[` ... `]]>` section.



Required
: yes; ignored in an [additional config file](#additional-plugin-configuration-files)

The element can be skipped in the source `plugin.xml` file if the Gradle plugin `patchPluginXml` task
([2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#patchPluginXml),
[1.x](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-patchpluginxml))
is enabled and configured.


Example
: ```XML
<description><![CDATA[
Provides support for My Framework.
The support includes:
<ul>
  <li>code completion</li>
  <li>references</li>
</ul>
For more information visit the
<a href="https://example.com">project site</a>.
]]></description>
```

### 

`change-notes`

<tldr>
Reference: [JetBrains Marketplace: Change Notes](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html#change-notes)
</tldr>

A short summary of new features, bugfixes, and changes provided with the latest plugin version.
Change notes are displayed on the [JetBrains Marketplace](https://plugins.jetbrains.com) plugin page and in
the Plugins settings dialog.

Simple HTML elements, like text formatting, paragraphs, lists, etc., are allowed and must be wrapped into
`<![CDATA[` ... `]]>` section.

Required
: no; ignored in an [additional config file](#additional-plugin-configuration-files)

The element can be skipped in the source `plugin.xml` file if the Gradle plugin `patchPluginXml` task
([2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#patchPluginXml),
[1.x](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-patchpluginxml))
is enabled and configured.

Example
: ```XML
<change-notes><![CDATA[
<h2>New Features</h2>
<ul>
<li>Feature 1</li>
<li>Feature 2</li>
</ul>
<h2>Bug Fixes</h2>
<ul>
<li>Fixed issue 1</li>
<li>Fixed issue 2</li>
</ul>
]]></change-notes>
```





### 
`depends`



<tldr>
Reference: [Plugin Dependencies](https://plugins.jetbrains.com/docs/intellij/plugin-dependencies.html)
, [Modules Specific to Functionality](https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html#modules-specific-to-functionality)
</tldr>





Specifies a dependency on another plugin or a module of an IntelliJ Platform-based product.
A single [&lt;idea-plugin&gt;](#idea-plugin) element can contain multiple `<depends>` elements.



Required
: no; in most cases dependency on the
[platform](https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html#modules-available-in-all-products)
module is needed


Attributes
: * `optional` (optional) Boolean value defining whether the dependency is optional to load the plugin in the IDE. If the dependency plugin is not installed in the current IDE, and `optional` is: * `true` - the plugin will be loaded * `false` (default) - the plugin will not be loaded

  * `config-file` (required when `optional` is `true`) Relative path to an [additional configuration file](#additional-plugin-configuration-files), loaded only if the dependency plugin is installed in the current IDE.


Examples
: * Required plugin dependency: ```XML <depends>com.example.dependencypluginid</depends> ```

  * Required dependency on the IntelliJ IDEA Java Module: ```XML <depends>com.intellij.modules.java</depends> ```

  * Required module dependency with additional configuration: ```XML <depends config-file="myPluginId-withJava.xml"> com.intellij.modules.java </depends> ```

  * Optional module dependency with additional configuration: ```XML <depends optional="true" config-file="myPluginId-withKotlin.xml"> org.jetbrains.kotlin </depends> ```





### 
`incompatible-with`



<tldr>
Reference: [Declaring Incompatibility with Plugin](https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html#declaring-incompatibility-with-module)
</tldr>






The [ID](#idea-plugin__id) or alias of the plugin the current plugin is incompatible with.
The plugin is not loaded if the incompatible plugin is installed in the current IDE.



Required
: no; ignored in an [additional config file](#additional-plugin-configuration-files)


Examples
: * Incompatibility with the Java plugin: ```XML <incompatible-with> com.intellij.java </incompatible-with> ```

  * Incompatibility with the AppCode plugin referenced via its alias: ```XML <incompatible-with> com.intellij.modules.appcode.ide </incompatible-with> ```





### 
`extensions`



<tldr>
Reference: [Extensions](https://plugins.jetbrains.com/docs/intellij/plugin-extensions.html)
</tldr>





Defines the plugin extensions.



Required
: no


Attributes
: * `defaultExtensionNs` (optional) Default extensions namespace. It allows skipping the common prefix in fully qualified extension point names. Usually, the `com.intellij` namespace is used when the plugin implements IntelliJ Platform extensions.


Children
: The children elements are registrations of instances
of [extension points](#idea-plugin__extensionPoints__extensionPoint) provided by the IntelliJ Platform or plugins.


An extension element name is defined by its extension point via
`name`
or `qualifiedName` attributes.


An extension element attributes depend on the extension point implementation, but all extensions support basic attributes:
`id`, `order`,
and `os`.


Examples
: * Extensions' declaration with a default namespace: ```XML <extensions defaultExtensionNs="com.intellij"> <applicationService serviceImplementation="com.example.Service"/> </extensions> ```

  * Extensions' declaration using the fully qualified extension name: ```XML <extensions> <com.example.vcs.myExtension implementation="com.example.MyExtension"/> </extensions> ```



#### An Extension





An extension instance registered under [&lt;extensions&gt;](#idea-plugin__extensions).


Listed attributes are basic attributes available for all extensions.
The list of actual attributes can be longer depending on the extension point implementation.



Attributes
: * `id` (optional) Unique extension identifier. It allows for referencing an extension in other attributes, for example, in `order`. To not clash with other plugins defining extensions with the same identifier, consider prepending the identifier with a prefix related to the plugin [&lt;id&gt;](#idea-plugin__id) or [&lt;name&gt;](#idea-plugin__name), for example, `id="com.example.myplugin.myExtension"`.

  * `order` (optional) Allows for ordering the extension relative to other instances of the same extension point. Supported values: * `first` - orders the extension as first. It is not guaranteed that the extension will be the first if multiple extensions are defined as `first`. * `last` - orders the extension as last. It is not guaranteed that the extension will be the last if multiple extensions are defined as `last`. * `before extension_id` - orders the extension before an extension with the given `id` * `after extension_id` - orders the extension after an extension with the given `id` Values can be combined, for example, `order="after extensionY, before extensionX"`.

  * `os` (optional) Allows restricting an extension to a given OS. Supported values: * `freebsd` * `linux` * `mac` * `unix` * `windows` For example, `os="windows"` registers the extension on Windows only.







### 
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









### 
`resource-bundle`







A resource bundle to be used with message key attributes in extension declarations and for
[action and group localization](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups).
A single [&lt;idea-plugin&gt;](#idea-plugin) element can contain multiple `<resource-bundle>` elements.



Required
: no


Example
: To load the content of `messages/Bundle.properties` bundle, declare:
: ```XML
<resource-bundle>messages.Bundle</resource-bundle>
```

### 

`actions`

<tldr>
Reference: [Actions](https://plugins.jetbrains.com/docs/intellij/action-system.html)
</tldr>

Defines the plugin actions.

Required
: no

Attributes
: * `resource-bundle` (optional; available since 2020.1) Defines the dedicated actions resource bundle. See [Localizing Actions and Groups](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups) for more details.

Children
: * [&lt;action&gt;](#idea-plugin__actions__action)

* [&lt;group&gt;](#idea-plugin__actions__group)

* [&lt;reference&gt;](#idea-plugin__actions__group__reference)

Example
: ```XML
<actions resource-bundle="messages.ActionsBundle">
<!--
Actions/Groups defined here will use keys
from the ActionsBundle.properties bundle.
-->
</actions>
```



#### 
`action`



<tldr>
Reference: [Registering Actions in plugin.xml](https://plugins.jetbrains.com/docs/intellij/action-system.html#registering-actions-in-pluginxml)
</tldr>





A single action entry of the [&lt;actions&gt;](#idea-plugin__actions) implemented by the plugin.
A single `<actions>` element can contain multiple `<action>` elements.



Required
: no


Attributes
: * `id` (optional; defaults to the action class short name if not specified) A unique action identifier. It is recommended to specify the `id` attribute explicitly. The action identifier must be unique across different plugins. To ensure uniqueness, consider prepending it with the value of the plugin's [&lt;id&gt;](#idea-plugin__id).

  * `class` (required) The fully qualified name of the action implementation class.

  * `text` (required if the action is not [localized](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups)) The default long-version text to be displayed for the action (tooltip for toolbar button or text for menu item).

  * `description` (optional) The text which is displayed in the status bar when the action is focused.

  * `icon` (optional) The icon that is displayed on the toolbar button or next to the action menu item. See [Working with Icons](https://plugins.jetbrains.com/docs/intellij/icons.html) for more information about defining and using icons.

  * `use-shortcut-of` (optional) The ID of the action whose keyboard shortcut this action will use.


Children
: * [&lt;abbreviation&gt;](#idea-plugin__actions__action__abbreviation)

  * [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group)

  * [&lt;keyboard-shortcut&gt;](#idea-plugin__actions__action__keyboard-shortcut)

  * [&lt;mouse-shortcut&gt;](#idea-plugin__actions__action__mouse-shortcut)

  * [&lt;override-text&gt;](#idea-plugin__actions__action__override-text)

  * [&lt;synonym&gt;](#idea-plugin__actions__action__synonym)


Examples
: * Action declaring explicit `text`: ```XML <action id="com.example.myframeworksupport.MyAction" class="com.example.impl.MyAction" text="Do Action" description="Do something with the code" icon="AllIcons.Actions.GC"> <!-- action children elements --> </action> ```

  * Action without the `text` attribute must use the texts from the resource bundle declared with the [&lt;resource-bundle&gt;](#idea-plugin__resource-bundle) element, or the `resource-bundle` attribute of the [&lt;actions&gt;](#idea-plugin__actions) element: ```XML <action id="com.example.myframeworksupport.MyAction" class="com.example.impl.MyAction" icon="AllIcons.Actions.GC"/> ```



##### 
`add-to-group`







Specifies that the action should be added to an existing [&lt;group&gt;](#idea-plugin__actions__group).
A single action can be added to multiple groups.



Required
: no


Attributes
: * `group-id` (required) Specifies the ID of the [&lt;group&gt;](#idea-plugin__actions__group) to which the action is added. The group must be an implementation of the [DefaultActionGroup](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/actionSystem/DefaultActionGroup.java) class.

  * `anchor` (optional) Specifies the position of the action relative to other actions. Allowed values: * `first` - the action is placed as the first in the group * `last` (default) - the action is placed as the last in the group * `before` - the action is placed before the action specified by the `relative-to-action` attribute * `after` - the action is placed after the action specified by the `relative-to-action` attribute

  * `relative-to-action` (required if `anchor` is `before`/`after`) The action before or after which the current action is inserted.


Example
: ```XML
<add-to-group
    group-id="ToolsMenu"
    anchor="after"
    relative-to-action="GenerateJavadoc"/>
```

##### 

`keyboard-shortcut`

Specifies the keyboard shortcut for the action.
A single action can have several keyboard shortcuts.

Required
: no

Attributes
: * `keymap` (required) Specifies the keymap for which the action shortcut is active. IDs of the standard keymaps are defined as constants in the [KeymapManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/keymap/KeymapManager.java) class.

* `first-keystroke` (required) Specifies the first keystroke of the action shortcut. The keystrokes are specified according to the regular Swing rules.

* `second-keystroke` (optional) Specifies the second keystroke of the action shortcut.

* `remove` (optional) Removes a shortcut from the specified action.

* `replace-all` (optional) Removes all keyboard and mouse shortcuts from the specified action before adding the specified shortcut.

Examples
: * Add the first and second keystrokes to all keymaps: ```XML <keyboard-shortcut keymap="$default" first-keystroke="control alt G" second-keystroke="C"/> ```

* Remove the given shortcut from the Mac OS X keymap: ```XML <keyboard-shortcut keymap="Mac OS X" first-keystroke="control alt G" second-keystroke="C" remove="true"/> ```

* Remove all existing keyboard and mouse shortcuts and register one for the Mac OS X 10.5+ keymap only: ```XML <keyboard-shortcut keymap="Mac OS X 10.5+" first-keystroke="control alt G" second-keystroke="C" replace-all="true"/> ```

##### 

`mouse-shortcut`

Specifies the mouse shortcut for the action.
A single action can have several mouse shortcuts.

Required
: no

Attributes
: * `keymap` (required) Specifies the keymap for which the action shortcut is active. IDs of the standard keymaps are defined as constants in the [KeymapManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/keymap/KeymapManager.java) class.

* `keystroke` (required) Specifies the clicks and modifiers for the action. It is defined as a sequence of words separated by spaces: * modifier keys: `shift`, `control`, `meta`, `alt`, `altGraph` * mouse buttons: `button1`, `button2`, `button3` * button double-click: `doubleClick`

* `remove` (optional) Removes a shortcut from the specified action.

* `replace-all` (optional) Removes all keyboard and mouse shortcuts from the specified action before adding the specified shortcut.

Examples
: * Add the shortcut to all keymaps: ```XML <mouse-shortcut keymap="$default" keystroke="control button3 doubleClick"/> ```

* Remove the given shortcut from the Mac OS X keymap: ```XML <mouse-shortcut keymap="Mac OS X" keystroke="control button3 doubleClick" remove="true"/> ```

* Remove all existing keyboard and mouse shortcuts and register one for the Mac OS X 10.5+ keymap only: ```XML <mouse-shortcut keymap="Mac OS X 10.5+" keystroke="control button3 doubleClick" replace-all="true"/> ```

##### 

`override-text`

Defines an alternate menu action or group text depending on context: menu location, toolbar, and other.

Supported
: 2020.1+ for actions

2020.3+ for groups

Required
: no

Attributes
: * `place` (required) Declares where the alternate text should be used.

* `text` (`text` or `use-text-of-place` is required) Defines the text to be displayed for the action.

* `use-text-of-place` (`text` or `use-text-of-place` is required) Defines a location whose text should be displayed for this action.

Examples
: * Explicitly overridden text: ```XML <!-- Default action text: "Garbage Collector: Collect _Garbage" --> <action class="com.example.CollectGarbage" text="Garbage Collector: Collect _Garbage" ...> <!-- Alternate text displayed anywhere in the main menu: "Collect _Garbage" --> <override-text place="MainMenu" text="Collect _Garbage"/> </action> ```

* Overridden text reused from the `MainMenu` place: ```XML <override-text place="EditorPopup" use-text-of-place="MainMenu"/> ```

##### 

`synonym`

Defines an alternative text for searching the action in `Help | Find Action...` or
`Navigate | Search Everywhere` popups.
A single action can have multiple synonyms.

Required
: no

Attributes
: * `key` (`key` or `text` is required) The key of the synonym text provided in a [message bundle](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups).

* `text` (`key` or `text` is required) The synonym text.

Example
: ```XML
<!-- Default action text: Delete Element -->
<synonym key="my.action.text.remove.element"/>
<synonym text="Remove Element"/>
```





##### 
`abbreviation`







Defines an abbreviation for searching the action in `Help | Find Action...` or
`Navigate | Search Everywhere` popups.
A single action can have multiple abbreviations.



Required
: no


Attributes
: * `value` (required) The abbreviation value.


Example
: ```XML
<!-- Default action text: UI Inspector -->
<abbreviation value="uii"/>
```

#### 

`group`

<tldr>
Reference: [Grouping Actions](https://plugins.jetbrains.com/docs/intellij/action-system.html#grouping-actions)
</tldr>

Defines an action group.
The [&lt;action&gt;](#idea-plugin__actions__action), `<group>` and [&lt;separator&gt;](#idea-plugin__actions__group__separator) elements defined inside the group are automatically included in it.
The `<group>` elements can be nested.

Required
: no

Attributes
: * `id` (required) A unique group identifier. The group identifier must be unique between different plugins. Thus, it is recommended to prepend it with the value of the plugin [&lt;id&gt;](#idea-plugin__id).

* `class` (optional) The fully qualified name of the group implementation class. If not specified, [DefaultActionGroup](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/actionSystem/DefaultActionGroup.java) is used.

* `text` (required if the `popup` is `true` and the group is not [localized](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups)) The default long-version text to be displayed for the group (text for the menu item showing the submenu).

* `description` (optional) The text which is displayed in the status bar when the group is focused.

* `icon` (optional) The icon that is displayed next to the group menu item. See [Working with Icons](https://plugins.jetbrains.com/docs/intellij/icons.html) for more information about defining and using icons.

* `popup` (optional) Boolean flag defining whether the group items are presented in the submenu popup. * `true` - group actions are placed in a submenu * `false` (default) - actions are displayed as a section of the same menu delimited by separators

* `compact` (optional) Boolean flag defining whether disabled actions within this group are hidden. If the value is: * `true` - disabled actions are hidden * `false` (default) - disabled actions are visible

* `use-shortcut-of` (optional) The ID of the action whose keyboard shortcut this group will use.

* `searchable` (optional; available since 2020.3) Boolean flag defining whether the group is displayed in `Help | Find Action...` or `Navigate | Search Everywhere` popups. Default value: `true`.

Children
: * [&lt;action&gt;](#idea-plugin__actions__action)

* [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group)

* [&lt;group&gt;](#idea-plugin__actions__group)

* [&lt;override-text&gt;](#idea-plugin__actions__action__override-text)

* [&lt;reference&gt;](#idea-plugin__actions__group__reference)

* [&lt;separator&gt;](#idea-plugin__actions__group__separator)

Examples
: * Group declaring explicit `text`: ```XML <group id="com.example.myframeworksupport.MyGroup" popup="true" text="My Tools"> <!-- group children elements --> </group> ```

* A popup group without the `text` attribute must use the texts from the resource bundle declared with the [&lt;resource-bundle&gt;](#idea-plugin__resource-bundle) element, or the `resource-bundle` attribute of the [&lt;actions&gt;](#idea-plugin__actions) element: ```XML <group id="com.example.myframeworksupport.MyGroup" popup="true"/> ```

* A group with custom implementation and icon: ```XML <group id="com.example.myframeworksupport.MyGroup" class="com.example.impl.MyGroup" icon="AllIcons.Actions.GC"/> ```

##### 

`reference`

Allows adding an existing action to the group.
The element can be used directly under the [&lt;actions&gt;](#idea-plugin__actions) element, or in
the [&lt;group&gt;](#idea-plugin__actions__group) element.

Required
: no

Attributes
: * `ref` (required) The ID of the action to add to a group.

* `id` (optional) Deprecated: Use `ref` instead. The ID of the action to add to a group.

Children
: * [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group)

Examples
: * An action reference in a group: ```XML <group ...> <reference ref="EditorCopy"/> </group> ```

* An action reference registered directly in the [&lt;actions&gt;](#idea-plugin__actions) element: ```XML <actions> <reference ref="com.example.MyAction"> <add-to-group group-id="ToolsMenu"/> </reference> </group> ```

##### 

`separator`

Defines a separator between actions in a group.
The element can be used directly under the [&lt;actions&gt;](#idea-plugin__actions) element with the child
[&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group) element defining the target group, or in the
[&lt;group&gt;](#idea-plugin__actions__group) element.

Required
: no

Attributes
: * `text` (optional) Text displayed on the separator. Separator text is displayed only in specific contexts such as popup menus, toolbars, etc.

* `key` (optional) The [message key](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups) for the separator text. The message bundle for use should be registered via the `resource-bundle` attribute of the [&lt;actions&gt;](#idea-plugin__actions) element. The attribute is ignored if the `text` attribute is specified.

Children
: * [&lt;add-to-group&gt;](#idea-plugin__actions__action__add-to-group)

Examples
: * A separator dividing two actions in a group: ```XML <group ...> <action .../> <separator/> <action .../> </group> ```

* A separator registered directly in the [&lt;actions&gt;](#idea-plugin__actions) element: ```XML <actions> <separator> <add-to-group group-id="com.example.MyGroup" anchor="first"/> </separator> </group> ```

* A separator with a defined text: ```XML <separator text="Group By"/> ```

* A separator with a text defined by a message key: ```XML <separator key="message.key"/> ```

### 

`applicationListeners`

<tldr>
Reference: [Defining Application-Level Listeners](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html#defining-application-level-listeners)
</tldr>

Defines the application-level listeners.

Required
: no

Children
: * [&lt;listener&gt;](#idea-plugin__applicationListeners__listener)

#### 

`listener`

<tldr>
Reference: [Listeners](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html)
</tldr>

Defines a single application or project-level listener.
A single [&lt;applicationListeners&gt;](#idea-plugin__applicationListeners) or
[&lt;projectListeners&gt;](#idea-plugin__projectListeners) can contain multiple `<listener>` elements.

Required
: no

Attributes
: * `topic` (required) The fully qualified name of the listener interface corresponding to the type of received events.

* `class` (required) The fully qualified name of the class implementing the listener interface that receives and handles the events.

* `os` (optional; available since 2020.1) Restricts listener instantiation to a specific operating system. Allowed values: * `freebsd` * `mac` * `linux` * `unix` * `windows`

* `activeInTestMode` (optional) Boolean flag defining whether the listener should be instantiated in test mode. Default value: `true`.

* `activeInHeadlessMode` (optional) Boolean flag defining whether the listener should be instantiated in headless mode. Default value: `true`.

Example
: ```XML
<listener
topic="com.intellij.ide.AppLifecycleListener"
class="com.example.MyListener"
os="mac"
activeInTestMode="false"/>
```







### 
`projectListeners`



<tldr>
Reference: [Defining Project-Level Listeners](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html#defining-project-level-listeners)
</tldr>






Defines the project-level listeners.



Required
: no


Children
: * [&lt;listener&gt;](#idea-plugin__applicationListeners__listener)





### 
`xi:include`







Allows including content of another plugin descriptor in this descriptor with
[XInclude](http://www.w3.org/2001/XInclude) standard.



Namespace
: `xi="http://www.w3.org/2001/XInclude"`


Required
: no


Attributes
: * `href` (optional) Path of the plugin descriptor file to include.

  * `xpointer` (optional) Deprecated since 2021.2: The `xpointer` attribute must be `xpointer(/idea-plugin/*)` or not defined. Elements pointer to include. Default value: `xpointer(/idea-plugin/*)`.


Children
: * [&lt;xi:fallback&gt;](#idea-plugin__xi:include__xi:fallback)


Example
: Given a plugin descriptor:
: ```XML
<idea-plugin xmlns:xi="http://www.w3.org/2001/XInclude">
  <id>com.example.myplugin</id>
  <name>Example</name>
  <xi:include href="/META-INF/another-plugin.xml"/>
  ...
</idea-plugin>
```
: and `/META-INF/another-plugin.xml`:
: ```XML
<idea-plugin>
<extensions>...</extensions>
<actions>...</actions>
</idea-plugin>
```
: The effective plugin descriptor loaded to memory will contain the following elements:
: ```XML
<idea-plugin xmlns:xi="http://www.w3.org/2001/XInclude">
  <id>com.example.myplugin</id>
  <name>Example</name>
  <extensions>...</extensions>
  <actions>...</actions>
  ...
</idea-plugin>
```

#### 

`xi:fallback`

Indicates that including the specified file is optional.

If the file referenced in `href` is not found and the `xi:fallback`
element
is missing, the plugin will fail to load.

Namespace
: `xi="http://www.w3.org/2001/XInclude"`

Required
: no

Example
: ```XML
<idea-plugin xmlns:xi="http://www.w3.org/2001/XInclude">
...
<xi:include href="/META-INF/optional-plugin.xml">
<xi:fallback/>
</xi:include>
...
</idea-plugin>
```







### 
`application-components`








Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





Defines a list of application [components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html).



Deprecated
: since 2020.1


Required
: no


Children
: * [&lt;component&gt;](#idea-plugin__application-components__component) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)



#### 
`component`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





Defines a single application, project, or
module [component](https://plugins.jetbrains.com/docs/intellij/plugin-components.html).
A single [&lt;application-components&gt;](#idea-plugin__application-components),
[&lt;project-components&gt;](#idea-plugin__project-components), or [&lt;module-components&gt;](#idea-plugin__module-components)
element can contain multiple `<component>` elements.



Deprecated
: since 2020.1


Required
: no


Children
: * [&lt;headless-implementation-class&gt;](#idea-plugin__application-components__component__headless-implementation-class) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

  * [&lt;implementation-class&gt;](#idea-plugin__application-components__component__implementation-class) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

  * [&lt;interface-class&gt;](#idea-plugin__application-components__component__interface-class) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

  * [&lt;loadForDefaultProject&gt;](#idea-plugin__application-components__component__loadForDefaultProject) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

  * [&lt;option&gt;](#idea-plugin__application-components__component__option) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

  * [&lt;skipForDefaultProject&gt;](#idea-plugin__application-components__component__skipForDefaultProject) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)



##### 
`implementation-class`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





The fully qualified name of the component implementation class.



Deprecated
: since 2020.1


Required
: yes





##### 
`interface-class`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





The fully qualified name of the component interface class. If not specified, the interface will be the same as
defined by [&lt;implementation-class&gt;](#idea-plugin__application-components__component__interface-class) element.



Deprecated
: since 2020.1


Required
: no





##### 
`headless-implementation-class`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





The fully qualified name of the component implementation class to be used when the IDE runs in headless mode.



Deprecated
: since 2020.1


Required
: no





##### 
`option`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





Allows to provide additional component options.
A single [&lt;component&gt;](#idea-plugin__application-components__component) element can contain multiple `<option>` elements.



Deprecated
: since 2020.1


Required
: no


Attributes
: * `name` (required) Option name.

  * `value` (required) Option value.





##### 
`loadForDefaultProject`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





If present, the component is instantiated also for the default project. It takes effect only when used inside
[&lt;project-components&gt;](#idea-plugin__project-components) element.



Deprecated
: since 2020.1


Required
: no





##### 
`skipForDefaultProject`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





In the past, if present, the component was not loaded for the default project.


Currently, project components aren't loaded in the default project by default, so this element has no effect.
Use [&lt;loadForDefaultProject&gt;](#idea-plugin__application-components__component__loadForDefaultProject)
if it is required to load a component in the default project.



Deprecated
: since 2020.1


Required
: no









### 
`project-components`








Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





Defines a list of project [components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html).



Deprecated
: since 2020.1


Required
: no


Children
: * [&lt;component&gt;](#idea-plugin__application-components__component) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)





### 
`module-components`








Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





Defines a list of module [components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html).



Deprecated
: since 2020.1


Required
: no


Children
: * [&lt;component&gt;](#idea-plugin__application-components__component) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

> Source: IntelliJ Platform SDK docs — Plugin Configuration File (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
