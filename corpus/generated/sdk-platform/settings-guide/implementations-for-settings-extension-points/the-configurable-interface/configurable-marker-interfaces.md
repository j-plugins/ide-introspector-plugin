---
id: sdk.settings-guide.implementations-for-settings-extension-points.the-configurable-interface.configurable-marker-interfaces
title: Settings Guide: Configurable` Marker Interfaces
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, configurable, marker, interfaces]
---
`Configurable` Marker Interfaces

Implementations based on `Configurable` can implement marker interfaces, which provide additional flexibility in the implementation.

`Configurable.NoScroll`
: Do not add scroll bars to the form. By default, a plugin's Settings component is put into a scrollable pane.
However, a Settings panel can have a `JTree`, which requires its own `JScrollPane`.
So the `NoScroll` interface should be used to remove the outer `JScrollPane`.

`Configurable.NoMargin`
: Do not add an empty border to the form. By default, an empty border is added for a plugin's Settings component.

`Configurable.Beta`
: (2022.3) Adds Beta label next to settings page title in Settings tree.

