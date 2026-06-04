---
id: the-extension-point-model
title: The extension point model
source: manual
kind: concept
description: Explains how the platform discovers, orders, and instantiates extensions so that contributions stay lazy and side effect free. Use when reasoning about ordering, lazy loading, or why counting extensions is expensive.
when_to_use: [extension ordering, lazy instantiation, extension adapters, why extensionList is expensive]
tags: [extension-point, architecture, lazy-loading]
---
# The extension point model

The platform stores each contribution as an adapter, not as an instantiated object. The
instance is created only when something reads it, which keeps startup cheap and contributions
side effect free.

## Why counting is expensive

Reading the full extension list forces instantiation of every contributor and can surface
latent registration bugs in unrelated plugins. Prefer the adapter count when you only need a
number.
