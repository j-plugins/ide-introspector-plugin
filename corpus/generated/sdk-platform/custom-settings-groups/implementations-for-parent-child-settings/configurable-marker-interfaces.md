---
id: sdk.custom-settings-groups.implementations-for-parent-child-settings.configurable-marker-interfaces
title: Custom Settings Groups: Configurable Marker Interfaces
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, configurable, marker, interfaces]
---
The `Configurable.Composite` interface indicates a configurable component has child components.
The preferred approach is to specify child components in the [EP declaration](#extension-points-for-parent-child-settings-relationships).
Using the `Composite` interface incurs the penalty of loading child classes while building the tree of Settings Swing components.

