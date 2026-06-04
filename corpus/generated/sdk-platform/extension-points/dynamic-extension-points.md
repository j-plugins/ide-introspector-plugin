---
id: sdk.extension-points.dynamic-extension-points
title: Extension Points: Dynamic Extension Points
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, dynamic, extension, points]
---
Part of `sdk.extension-points`.

To support [Dynamic Plugins](https://plugins.jetbrains.com/docs/intellij/dynamic-plugins.html), an extension point must adhere to specific usage rules:

* extensions are enumerated on every use, and extension instances are not stored anywhere

* alternatively, an [ExtensionPointListener](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/extensions/src/com/intellij/openapi/extensions/ExtensionPointListener.kt) can perform any necessary updates of data structures (register via `ExtensionPointName.addExtensionPointListener()`)

Extension points matching these conditions can then be marked as dynamic by adding `dynamic="true"` in their declaration:

```XML
<extensionPoints>
  <extensionPoint
          name="myDynamicExtensionPoint"
          beanClass="com.example.MyBeanClass"
          dynamic="true"/>
</extensionPoints>
```

All non-dynamic extension points are highlighted via Plugin DevKit | Plugin descriptor | Plugin.xml dynamic plugin verification inspection.

> Source: IntelliJ Platform SDK docs — Extension Points: Dynamic Extension Points (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
