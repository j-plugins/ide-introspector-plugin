---
id: sdk.persisting-state-of-components.using-persistentstatecomponent.sharing-settings-between-ide-installations.backup-and-sync-plugin
title: Persisting State of Components: Backup and Sync Plugin
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, backup, sync, plugin]
---
Tip:

The Settings Sync plugin has been renamed to Backup and Sync in 2024.3.

To include a plugin's component state in the Backup and Sync plugin synchronization, the following requirements must be met:

* The `RoamingType` is defined via the `roamingType` attribute of the `@Storage` annotation and is not equal to `DISABLED`.

* The `SettingsCategory` is defined via the `category` attribute of the `@State` annotation and is not equal to `OTHER`.

* There is no other `PersistentStateComponent`, which is stored in the same XML file and has a different `RoamingType`.

If the component state is OS-dependent, the `roamingType` of the `@Storage` annotation must be set to `RoamingType.PER_OS`.

Warning:

Note that `other.xml` file is non-roamable and declaring it in the `@Storage` annotation will disable roaming of the component state.
It is recommended to use a separate XML file for the component or use another existing storage file.

