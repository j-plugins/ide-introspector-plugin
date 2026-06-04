---
id: registering-an-extension-point
title: Registering an extension point
source: manual
kind: skill
description: Declares a new extension point in plugin.xml and contributes to it from Kotlin. Use when adding an extension point, exposing a plugin API, or choosing between an interface and a bean extension point.
when_to_use: [add extension point, expose plugin api, interface vs bean ep, dynamic extension point]
tags: [extension-point, plugin-xml, api]
related_eps: [com.intellij.postStartupActivity]
---
# Registering an extension point

An extension point is a typed slot other plugins fill. Use an interface extension point for
behaviour and a bean extension point for declarative data.

## Declare it

Declare the extension point in `plugin.xml` under `<extensionPoints>`, giving it a name and
either an `interface` or a `beanClass`.

## Contribute to it

Other plugins (or your own) contribute through `<extensions>` with a matching tag. Enumerate
the live contributors with the `arch.*` tools to see real registrations in the running IDE.
