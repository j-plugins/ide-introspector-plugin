---
id: sdk.plugin-configuration-file.idea-plugin.vendor
title: Plugin Configuration File: vendor
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, vendor]
---
`vendor`



<tldr>
Reference: [JetBrains Marketplace: Contacts and Resources](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html#contacts-and-resources)
</tldr>





The vendor name or organization ID (if created) in the Plugins settings dialog and on
the [JetBrains Marketplace](https://plugins.jetbrains.com) plugin page.



Required
: yes; ignored in an [additional config file](#additional-plugin-configuration-files)


Attributes
: * `url` (optional) The URL to the vendor's homepage. Supports `https://` and `http://` scheme links.

  * `email` (optional) The vendor's email address.


Examples
: * Personal vendor with an email address provided: ```XML <vendor email="joe@example.com">Joe Doe</vendor> ```

  * Organizational vendor with a website URL and email address provided: ```XML <vendor url="https://mycompany.example.com" email="contact@example.com"> My Company </vendor> ```

