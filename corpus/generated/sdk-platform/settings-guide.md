---
id: sdk.settings-guide
title: Settings Guide
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, settings, guide]
---
Settings persistently store states that control the behavior and appearance of IntelliJ Platform-based IDEs.
On this page, the term "Settings" means the same as "Preferences" on some platforms.

Plugins can create and store Settings to capture their configuration in a way that uses the IntelliJ Platform [Persistence Model](https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html).
The User Interface (UI) for these custom Settings can be added to the [IDE Settings dialog](https://www.jetbrains.com/help/idea/settings-preferences-dialog.html).
For [split plugins](https://plugins.jetbrains.com/docs/intellij/split-mode-and-remote-development.html), settings persistence may also require explicit frontend and backend synchronization.
See [Persistent State Component in Split Mode](https://plugins.jetbrains.com/docs/intellij/persistent-state-in-split-mode.html).

Settings can [affect different levels](https://www.jetbrains.com/help/idea/configuring-project-and-ide-settings.html) of scope.
This document describes adding custom Settings at the Project and Application (or Global, IDE) levels.

Note:

See [Settings Tutorial](https://plugins.jetbrains.com/docs/intellij/settings-tutorial.html) for step-by-step instructions for creating a simple set of custom Settings.

Tip:

See [Inspecting Settings](https://plugins.jetbrains.com/docs/intellij/internal-ui-inspector.html#inspecting-settings) on how to gather information in the IDE instance for Settings dialog.

## Extension Points for Settings (settings-guide/extension-points-for-settings.md)
### Declaring Application Settings (settings-guide/extension-points-for-settings/declaring-application-settings.md)
### Declaring Project Settings (settings-guide/extension-points-for-settings/declaring-project-settings.md)
### Settings Declaration Attributes (settings-guide/extension-points-for-settings/settings-declaration-attributes.md)
#### Table of Attributes (settings-guide/extension-points-for-settings/settings-declaration-attributes/table-of-attributes.md)
##### Attribute Notes (settings-guide/extension-points-for-settings/settings-declaration-attributes/table-of-attributes/attribute-notes.md)
#### Values for Parent ID Attribute (settings-guide/extension-points-for-settings/settings-declaration-attributes/values-for-parent-id-attribute.md)
## Implementations for Settings Extension Points (settings-guide/implementations-for-settings-extension-points.md)
### The `Configurable` Interface (settings-guide/implementations-for-settings-extension-points/the-configurable-interface.md)
#### Constructors (settings-guide/implementations-for-settings-extension-points/the-configurable-interface/constructors.md)
#### IntelliJ Platform Interactions with `Configurable (settings-guide/implementations-for-settings-extension-points/the-configurable-interface/intellij-platform-interactions-with-configurable.md)
#### Configurable` Marker Interfaces (settings-guide/implementations-for-settings-extension-points/the-configurable-interface/configurable-marker-interfaces.md)
#### Additional Interfaces Based on `Configurable (settings-guide/implementations-for-settings-extension-points/the-configurable-interface/additional-interfaces-based-on-configurable.md)
#### Examples (settings-guide/implementations-for-settings-extension-points/the-configurable-interface/examples.md)
### The `ConfigurableProvider` Class (settings-guide/implementations-for-settings-extension-points/the-configurableprovider-class.md)

> Source: IntelliJ Platform SDK docs — Settings Guide (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
