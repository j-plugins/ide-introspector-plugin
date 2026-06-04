---
id: sdk.persisting-state-of-components.legacy-api-jdomexternalizable
title: Persisting State of Components: Legacy API (`JDOMExternalizable`)
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, legacy, api, jdomexternalizable]
---
Legacy API (`JDOMExternalizable`)

Older components use the [JDOMExternalizable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/util/JDOMExternalizable.java) interface for persisting state.
It uses the `readExternal()` method for reading the state from a JDOM element, and `writeExternal()` to write the state.

Implementations can manually store the state in attributes and sub-elements or use the [DefaultJDOMExternalizer](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/util/DefaultJDOMExternalizer.java) class to store the values of all public fields automatically.

Components save their state in the following files:

* Project-level: project (`.ipr`) file. However, if the workspace option in the `[plugin.xml](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html)` file is set to `true`, then the workspace (`.iws`) file is used instead.

* Module-level: module (`.iml`) file.

