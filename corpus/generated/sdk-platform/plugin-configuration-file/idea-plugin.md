---
id: sdk.plugin-configuration-file.idea-plugin
title: Plugin Configuration File: idea-plugin
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, idea, plugin]
---
Part of `sdk.plugin-configuration-file`.

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

## Subtopics

- id — `sdk.plugin-configuration-file.idea-plugin.id`
- name — `sdk.plugin-configuration-file.idea-plugin.name`
- version — `sdk.plugin-configuration-file.idea-plugin.version`
- product-descriptor — `sdk.plugin-configuration-file.idea-plugin.product-descriptor`
- idea-version — `sdk.plugin-configuration-file.idea-plugin.idea-version`
- vendor — `sdk.plugin-configuration-file.idea-plugin.vendor`
- description — `sdk.plugin-configuration-file.idea-plugin.description`
- change-notes — `sdk.plugin-configuration-file.idea-plugin.change-notes`
- depends — `sdk.plugin-configuration-file.idea-plugin.depends`
- incompatible-with — `sdk.plugin-configuration-file.idea-plugin.incompatible-with`
- extensions — `sdk.plugin-configuration-file.idea-plugin.extensions`
- extensionPoints — `sdk.plugin-configuration-file.idea-plugin.extensionpoints`
- resource-bundle — `sdk.plugin-configuration-file.idea-plugin.resource-bundle`
- actions — `sdk.plugin-configuration-file.idea-plugin.actions`
- applicationListeners — `sdk.plugin-configuration-file.idea-plugin.applicationlisteners`
- projectListeners — `sdk.plugin-configuration-file.idea-plugin.projectlisteners`
- xi:include — `sdk.plugin-configuration-file.idea-plugin.xi-include`
- application-components — `sdk.plugin-configuration-file.idea-plugin.application-components`
- project-components — `sdk.plugin-configuration-file.idea-plugin.project-components`
- module-components — `sdk.plugin-configuration-file.idea-plugin.module-components`

> Source: IntelliJ Platform SDK docs — Plugin Configuration File: idea-plugin (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
