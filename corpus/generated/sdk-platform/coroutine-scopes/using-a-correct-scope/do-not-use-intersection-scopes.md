---
id: sdk.coroutine-scopes.using-a-correct-scope.do-not-use-intersection-scopes
title: Coroutine Scopes: Do Not Use Intersection Scopes
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, not, use, intersection, scopes]
---
There is no API for retrieving Application×Plugin and Project×Plugin [intersection scopes](#intersection-scopes),
but let's assume there is a method exposing the Project×Plugin scope:

```KOTLIN
/**
 * Returns the correct intersection scope for the project and plugin
 * by a given plugin class.
 */
fun Project.getCoroutineScope(pluginClass: Class<*>): CoroutineScope
```

Using this scope could lead to a plugin leak:

```KOTLIN
project.getCoroutineScope(PluginBService::class.java).launch {
  project.getService(PluginAService::class.java)
}
```

Unloading of Plugin A cancels its scope.
The Project × Plugin B scope remains active, and the Plugin A classes are leaked.

```MERMAID
gantt
    dateFormat X
    %% do not remove trailing space in axisFormat
    axisFormat ‎
    section Lifetimes
        Application Scope                               : done, 0, 10
        Project Scope                                   : done, 2, 9
        Plugin A Scope                                  : active, done, 1, 6
        Project × Plugin A Scope                        : active, done, 2, 6
        Plugin B Scope                                  : 4, 10
        Project × Plugin B Scope                        : 4, 9
        PluginAService leak                             : crit, 5, 9
        Correct PluginAService lifetime should end here : active, milestone, 6, 6
```

Note: Something missing?

If a topic is not covered in the above sections,
let us know via the Feedback widget displayed on the right,
or [other channels](https://plugins.jetbrains.com/docs/intellij/getting-help.html#problems-with-the-guide).

Be specific about the topics and reasons for adding them and leave your email in case we need
more details. Thanks for your feedback!

