---
id: sdk.plugin-configuration-file.configuration-structure-overview
title: Plugin Configuration File: Configuration Structure Overview
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, configuration, structure, overview]
---
Part of `sdk.plugin-configuration-file`.

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

> Source: IntelliJ Platform SDK docs — Plugin Configuration File: Configuration Structure Overview (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
