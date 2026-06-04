---
id: sdk.plugin-configuration-file.idea-plugin
title: Plugin Configuration File: idea-plugin
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, idea, plugin]
---
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

### id (plugin-configuration-file/idea-plugin/id.md)
### name (plugin-configuration-file/idea-plugin/name.md)
### version (plugin-configuration-file/idea-plugin/version.md)
### product-descriptor (plugin-configuration-file/idea-plugin/product-descriptor.md)
### idea-version (plugin-configuration-file/idea-plugin/idea-version.md)
### vendor (plugin-configuration-file/idea-plugin/vendor.md)
### description (plugin-configuration-file/idea-plugin/description.md)
### change-notes (plugin-configuration-file/idea-plugin/change-notes.md)
### depends (plugin-configuration-file/idea-plugin/depends.md)
### incompatible-with (plugin-configuration-file/idea-plugin/incompatible-with.md)
### extensions (plugin-configuration-file/idea-plugin/extensions.md)
#### An Extension (plugin-configuration-file/idea-plugin/extensions/an-extension.md)
### extensionPoints (plugin-configuration-file/idea-plugin/extensionpoints.md)
#### extensionPoint (plugin-configuration-file/idea-plugin/extensionpoints/extensionpoint.md)
##### with (plugin-configuration-file/idea-plugin/extensionpoints/extensionpoint/with.md)
### resource-bundle (plugin-configuration-file/idea-plugin/resource-bundle.md)
### actions (plugin-configuration-file/idea-plugin/actions.md)
#### action (plugin-configuration-file/idea-plugin/actions/action.md)
##### add-to-group (plugin-configuration-file/idea-plugin/actions/action/add-to-group.md)
##### keyboard-shortcut (plugin-configuration-file/idea-plugin/actions/action/keyboard-shortcut.md)
##### mouse-shortcut (plugin-configuration-file/idea-plugin/actions/action/mouse-shortcut.md)
##### override-text (plugin-configuration-file/idea-plugin/actions/action/override-text.md)
##### synonym (plugin-configuration-file/idea-plugin/actions/action/synonym.md)
##### abbreviation (plugin-configuration-file/idea-plugin/actions/action/abbreviation.md)
#### group (plugin-configuration-file/idea-plugin/actions/group.md)
##### reference (plugin-configuration-file/idea-plugin/actions/group/reference.md)
##### separator (plugin-configuration-file/idea-plugin/actions/group/separator.md)
### applicationListeners (plugin-configuration-file/idea-plugin/applicationlisteners.md)
#### listener (plugin-configuration-file/idea-plugin/applicationlisteners/listener.md)
### projectListeners (plugin-configuration-file/idea-plugin/projectlisteners.md)
### xi:include (plugin-configuration-file/idea-plugin/xi-include.md)
#### xi:fallback (plugin-configuration-file/idea-plugin/xi-include/xi-fallback.md)
### application-components (plugin-configuration-file/idea-plugin/application-components.md)
#### component (plugin-configuration-file/idea-plugin/application-components/component.md)
##### implementation-class (plugin-configuration-file/idea-plugin/application-components/component/implementation-class.md)
##### interface-class (plugin-configuration-file/idea-plugin/application-components/component/interface-class.md)
##### headless-implementation-class (plugin-configuration-file/idea-plugin/application-components/component/headless-implementation-class.md)
##### option (plugin-configuration-file/idea-plugin/application-components/component/option.md)
##### loadForDefaultProject (plugin-configuration-file/idea-plugin/application-components/component/loadfordefaultproject.md)
##### skipForDefaultProject (plugin-configuration-file/idea-plugin/application-components/component/skipfordefaultproject.md)
### project-components (plugin-configuration-file/idea-plugin/project-components.md)
### module-components (plugin-configuration-file/idea-plugin/module-components.md)
