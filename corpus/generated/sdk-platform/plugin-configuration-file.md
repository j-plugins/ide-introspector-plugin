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

## Additional Plugin Configuration Files (plugin-configuration-file/additional-plugin-configuration-files.md)
## Useful Resources (plugin-configuration-file/useful-resources.md)
## Configuration Structure Overview (plugin-configuration-file/configuration-structure-overview.md)
## idea-plugin (plugin-configuration-file/idea-plugin.md)
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

> Source: IntelliJ Platform SDK docs — Plugin Configuration File (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
