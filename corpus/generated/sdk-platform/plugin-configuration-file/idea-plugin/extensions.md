---
id: sdk.plugin-configuration-file.idea-plugin.extensions
title: Plugin Configuration File: extensions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, extensions]
---
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

#### An Extension (plugin-configuration-file/idea-plugin/extensions/an-extension.md)
