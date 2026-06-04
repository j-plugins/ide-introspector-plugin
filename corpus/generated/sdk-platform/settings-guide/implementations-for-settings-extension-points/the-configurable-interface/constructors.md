---
id: sdk.settings-guide.implementations-for-settings-extension-points.the-configurable-interface.constructors
title: Settings Guide: Constructors
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, constructors]
---
Implementations must meet several requirements for constructors.

* Application Settings implementations, declared using the [applicationConfigurable EP](#declaring-application-settings), must have a default constructor with no arguments.

* Project Settings implementations, declared using the [projectConfigurable EP](#declaring-project-settings), must declare a constructor with a single argument of type [Project](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/Project.java).

* Constructor injection (other than for `Project`) is not allowed.

For a `Configurable` implementation correctly declared using an EP, the implementation's constructor is not invoked by the IntelliJ Platform until a user chooses the corresponding Settings `displayName` in the Settings Dialog menu.

Warning:

The IntelliJ Platform may instantiate a `Configurable` implementation on a background thread, so creating Swing components in a constructor can degrade UI responsiveness.

