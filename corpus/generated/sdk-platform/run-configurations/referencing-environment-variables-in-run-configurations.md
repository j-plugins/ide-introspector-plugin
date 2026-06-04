---
id: sdk.run-configurations.referencing-environment-variables-in-run-configurations
title: Run Configurations: Referencing Environment Variables in Run Configurations
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, referencing, environment, variables, run, configurations]
---
Run configurations can define user environment variables specific to a given run configuration and include system environment variables.
Sometimes, it is convenient to reference existing variables in newly created variables, e.g., if a user creates an `EXTENDED_PATH` variable and builds it from a custom entry and the system `PATH` variable, they should reference it in the value by surrounding it with the `$` character: `/additional/entry:$PATH$`.

To substitute variable references with the actual references, it is required to call [EnvironmentUtil.inlineParentOccurrences()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/EnvironmentUtil.java) (available since 2023.2).

