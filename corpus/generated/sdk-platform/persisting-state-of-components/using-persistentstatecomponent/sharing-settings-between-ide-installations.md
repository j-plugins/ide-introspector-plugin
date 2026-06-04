---
id: sdk.persisting-state-of-components.using-persistentstatecomponent.sharing-settings-between-ide-installations
title: Persisting State of Components: Sharing Settings Between IDE Installations
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, sharing, settings, between, ide, installations]
---
<tldr>
Product Help: [Share IDE settings](https://www.jetbrains.com/help/idea/sharing-your-ide-settings.html)
</tldr>

It is possible to share the persistent state of components between different IDE installations.
This allows users to have the same settings on every development machine or to share their settings within a team.

Settings can be shared via the following functionalities:

* [Backup and Sync](https://www.jetbrains.com/help/idea/sharing-your-ide-settings.html#IDE_settings_sync) (formerly Settings Sync) plugin that allows synchronizing settings on JetBrains servers. Users can select the category of settings that are synchronized.

* [Settings Repository](https://www.jetbrains.com/help/idea/sharing-your-ide-settings.html#settings-repository) plugin that allows synchronizing settings in a Git repository created and configured by a user.

* [Export Settings](https://www.jetbrains.com/help/idea/sharing-your-ide-settings.html#import-export-settings) feature that allows for the manual import and export of settings.

Tip:

Synchronization via the Backup and Sync or Settings Repository plugins only works when these plugins are installed and enabled.

The decision about making a specific component's state shareable should be made carefully.
Only the settings that aren't specific to a given machine should be shared, for example, paths to user-specific directories shouldn't be shared.
If a component contains both shareable and non-shareable data, it should be split into two separate components.

#### Backup and Sync Plugin (persisting-state-of-components/using-persistentstatecomponent/sharing-settings-between-ide-installations/backup-and-sync-plugin.md)
#### Settings Repository Plugin and Export Settings Feature (persisting-state-of-components/using-persistentstatecomponent/sharing-settings-between-ide-installations/settings-repository-plugin-and-export-settings-feature.md)
