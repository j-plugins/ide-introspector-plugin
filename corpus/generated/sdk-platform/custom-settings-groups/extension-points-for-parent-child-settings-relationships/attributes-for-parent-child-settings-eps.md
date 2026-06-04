---
id: sdk.custom-settings-groups.extension-points-for-parent-child-settings-relationships.attributes-for-parent-child-settings-eps
title: Custom Settings Groups: Attributes for Parent-Child Settings EPs
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, attributes, for, parent, child, settings]
---
There is only one unique attribute when declaring a child Settings EP.
The other attributes are the same as discussed in [Settings Declaration Attributes](https://plugins.jetbrains.com/docs/intellij/settings-guide.html#settings-declaration-attributes).

For the child of a parent, the `id` attribute becomes compound:

| Attribute |Required |Value |
------------------------------
| `id` |Y |Compound FQN of implementation based on `com.intellij.openapi.options.Configurable` in the form: `XX.YY` where:    * `XX` - the parent Settings component FQN-based ID    * `YY` - unique to the child among other siblings   |

Tip:

All children share the parent's `id` as the basis of their own `id`.
All children have an `id` suffix that is unique among their siblings.

