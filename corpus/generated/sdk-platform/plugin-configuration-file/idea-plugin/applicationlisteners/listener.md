---
id: sdk.plugin-configuration-file.idea-plugin.applicationlisteners.listener
title: Plugin Configuration File: listener
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, listener]
---
`listener`

<tldr>
Reference: [Listeners](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html)
</tldr>

Defines a single application or project-level listener.
A single [&lt;applicationListeners&gt;](#idea-plugin__applicationListeners) or
[&lt;projectListeners&gt;](#idea-plugin__projectListeners) can contain multiple `<listener>` elements.

Required
: no

Attributes
: * `topic` (required) The fully qualified name of the listener interface corresponding to the type of received events.

* `class` (required) The fully qualified name of the class implementing the listener interface that receives and handles the events.

* `os` (optional; available since 2020.1) Restricts listener instantiation to a specific operating system. Allowed values: * `freebsd` * `mac` * `linux` * `unix` * `windows`

* `activeInTestMode` (optional) Boolean flag defining whether the listener should be instantiated in test mode. Default value: `true`.

* `activeInHeadlessMode` (optional) Boolean flag defining whether the listener should be instantiated in headless mode. Default value: `true`.

Example
: ```XML
<listener
topic="com.intellij.ide.AppLifecycleListener"
class="com.example.MyListener"
os="mac"
activeInTestMode="false"/>
```

