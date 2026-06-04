---
id: sdk.plugin-configuration-file.idea-plugin.applicationlisteners
title: Plugin Configuration File: applicationListeners
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, applicationlisteners]
---
Part of `sdk.plugin-configuration-file.idea-plugin`.

`applicationListeners`

<tldr>
Reference: [Defining Application-Level Listeners](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html#defining-application-level-listeners)
</tldr>

Defines the application-level listeners.

Required
: no

Children
: * [&lt;listener&gt;](#idea-plugin__applicationListeners__listener)

#### 

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

> Source: IntelliJ Platform SDK docs — Plugin Configuration File: applicationListeners (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
