---
id: sdk.plugin-configuration-file.idea-plugin.extensionpoints.extensionpoint.with
title: Plugin Configuration File: with
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, with]
---
`with`







Specifies the required parent type for class names provided in extension point tags or attributes.
A single [&lt;extensionPoint&gt;](#idea-plugin__extensionPoints__extensionPoint) element can contain
multiple `<with>` elements.



Required
: no


Attributes
: * `tag` (`tag` or `attribute` is required) The name of the tag holding the fully qualified name of the class which parent type will be limited by the type provided in the `implements` attribute. Only one of the `tag` and `attribute` attributes can be specified.

  * `attribute` (`tag` or `attribute` is required) The name of the attribute holding the fully qualified name of the class which parent type will be limited by the type provided in the `implements` attribute. Only one of the `tag` and `attribute` attributes can be specified.

  * `implements` (required) The fully qualified name of the parent type limiting the type provided in the place specified by `tag` or `attribute`.


Example
: An extension point which restricts the type provided in a `myClass` attribute to be an instance
of `com.example.ParentType`, and the type provided in a `someClass` element to be an instance
of `java.lang.Comparable`:
: ```XML
<extensionPoint
    name="myExtension"
    beanClass="com.example.MyExtension">
  <with
      attribute="myClass"
      implements="com.example.ParentType"/>
  <with
      tag="someClass"
      implements="java.lang.Comparable"/>
</extensionPoint>
```
: When using the above extension point, an implementation could be registered as follows:
: ```XML
<myExtension ...
myClass="com.example.MyCustomType">
<someClass>com.example.MyComparable</someClass>
</myExtension>
```
: where:
: * `com.example.MyCustomType` must be a subtype of `com.example.ParentType`

  * `com.example.MyComparable` must be a subtype of `java.lang.Comparable`

