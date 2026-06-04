---
id: sdk.settings-guide.extension-points-for-settings.settings-declaration-attributes.table-of-attributes.attribute-notes
title: Settings Guide: Attribute Notes
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, attribute, notes]
---
(1) Either `instance` or `provider` must be specified depending on the implementation.

(2) Either `displayName` or `key` and `bundle` must be specified depending on whether the displayed Settings name is localized.

(3) If both `groupId` and `parentId` are specified, a warning is logged. Also, see default entry in [Values for Parent ID Attribute](#values-for-parent-id-attribute).

