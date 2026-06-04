---
id: sdk.plugin-configuration-file.idea-plugin.application-components
title: Plugin Configuration File: application-components
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, application, components]
---
Part of `sdk.plugin-configuration-file.idea-plugin`.

`application-components`








Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





Defines a list of application [components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html).



Deprecated
: since 2020.1


Required
: no


Children
: * [&lt;component&gt;](#idea-plugin__application-components__component) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)



#### 
`component`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





Defines a single application, project, or
module [component](https://plugins.jetbrains.com/docs/intellij/plugin-components.html).
A single [&lt;application-components&gt;](#idea-plugin__application-components),
[&lt;project-components&gt;](#idea-plugin__project-components), or [&lt;module-components&gt;](#idea-plugin__module-components)
element can contain multiple `<component>` elements.



Deprecated
: since 2020.1


Required
: no


Children
: * [&lt;headless-implementation-class&gt;](#idea-plugin__application-components__component__headless-implementation-class) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

  * [&lt;implementation-class&gt;](#idea-plugin__application-components__component__implementation-class) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

  * [&lt;interface-class&gt;](#idea-plugin__application-components__component__interface-class) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

  * [&lt;loadForDefaultProject&gt;](#idea-plugin__application-components__component__loadForDefaultProject) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

  * [&lt;option&gt;](#idea-plugin__application-components__component__option) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

  * [&lt;skipForDefaultProject&gt;](#idea-plugin__application-components__component__skipForDefaultProject) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)



##### 
`implementation-class`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





The fully qualified name of the component implementation class.



Deprecated
: since 2020.1


Required
: yes





##### 
`interface-class`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





The fully qualified name of the component interface class. If not specified, the interface will be the same as
defined by [&lt;implementation-class&gt;](#idea-plugin__application-components__component__interface-class) element.



Deprecated
: since 2020.1


Required
: no





##### 
`headless-implementation-class`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





The fully qualified name of the component implementation class to be used when the IDE runs in headless mode.



Deprecated
: since 2020.1


Required
: no





##### 
`option`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





Allows to provide additional component options.
A single [&lt;component&gt;](#idea-plugin__application-components__component) element can contain multiple `<option>` elements.



Deprecated
: since 2020.1


Required
: no


Attributes
: * `name` (required) Option name.

  * `value` (required) Option value.





##### 
`loadForDefaultProject`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





If present, the component is instantiated also for the default project. It takes effect only when used inside
[&lt;project-components&gt;](#idea-plugin__project-components) element.



Deprecated
: since 2020.1


Required
: no





##### 
`skipForDefaultProject`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





In the past, if present, the component was not loaded for the default project.


Currently, project components aren't loaded in the default project by default, so this element has no effect.
Use [&lt;loadForDefaultProject&gt;](#idea-plugin__application-components__component__loadForDefaultProject)
if it is required to load a component in the default project.



Deprecated
: since 2020.1


Required
: no

> Source: IntelliJ Platform SDK docs — Plugin Configuration File: application-components (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
