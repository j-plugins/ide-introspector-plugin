---
id: sdk.plugin-configuration-file.idea-plugin.product-descriptor
title: Plugin Configuration File: product-descriptor
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, product, descriptor]
---
Part of `sdk.plugin-configuration-file.idea-plugin`.

`product-descriptor`



<tldr>
Reference: [JetBrains Marketplace: How to add required parameters for paid plugins](https://plugins.jetbrains.com/docs/marketplace/add-required-parameters.html)
</tldr>





[Paid](https://plugins.jetbrains.com/build-and-market) or
[Freemium](https://plugins.jetbrains.com/docs/marketplace/freemium.html) plugin descriptor.



Required
: only for paid or freemium plugins; ignored in an [additional config file](#additional-plugin-configuration-files)

Do not add `<product-descriptor>` element in a free plugin.


Attributes
: * `code` (required) The plugin product code used in the JetBrains Sales System. The code must be agreed with JetBrains in advance and follow [the requirements](https://plugins.jetbrains.com/docs/marketplace/add-required-parameters.html).

  * `release-date` (required) Date of the major version release in the `YYYYMMDD` format.

  * `release-version` (required) A major version in a specific number format, for example, `20242` for the 2024.2 major release. See [release-version constraints](https://plugins.jetbrains.com/docs/marketplace/versioning-of-paid-plugins.html#release-version-constraints) for more details.

  * `optional` (optional) The boolean value determining whether the plugin is a [Freemium](https://plugins.jetbrains.com/docs/marketplace/freemium.html) plugin. Default value: `false`.

  * `eap` (optional) Specifies the boolean value determining whether the plugin is an EAP release. Default value: `false`.

> Source: IntelliJ Platform SDK docs — Plugin Configuration File: product-descriptor (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
