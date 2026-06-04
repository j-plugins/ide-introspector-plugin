---
id: sdk.plugin-configuration-file.idea-plugin.extensions.an-extension
title: Plugin Configuration File: An Extension
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, extension]
---
An extension instance registered under [&lt;extensions&gt;](#idea-plugin__extensions).


Listed attributes are basic attributes available for all extensions.
The list of actual attributes can be longer depending on the extension point implementation.



Attributes
: * `id` (optional) Unique extension identifier. It allows for referencing an extension in other attributes, for example, in `order`. To not clash with other plugins defining extensions with the same identifier, consider prepending the identifier with a prefix related to the plugin [&lt;id&gt;](#idea-plugin__id) or [&lt;name&gt;](#idea-plugin__name), for example, `id="com.example.myplugin.myExtension"`.

  * `order` (optional) Allows for ordering the extension relative to other instances of the same extension point. Supported values: * `first` - orders the extension as first. It is not guaranteed that the extension will be the first if multiple extensions are defined as `first`. * `last` - orders the extension as last. It is not guaranteed that the extension will be the last if multiple extensions are defined as `last`. * `before extension_id` - orders the extension before an extension with the given `id` * `after extension_id` - orders the extension after an extension with the given `id` Values can be combined, for example, `order="after extensionY, before extensionX"`.

  * `os` (optional) Allows restricting an extension to a given OS. Supported values: * `freebsd` * `linux` * `mac` * `unix` * `windows` For example, `os="windows"` registers the extension on Windows only.

