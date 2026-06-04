---
id: sdk.plugin-configuration-file.idea-plugin.extensions
title: Plugin Configuration File: extensions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, extensions]
---
Part of `sdk.plugin-configuration-file.idea-plugin`.

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

> Source: IntelliJ Platform SDK docs — Plugin Configuration File: extensions (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
