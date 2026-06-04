# Sharing Settings Between IDE Installations

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

#### Backup and Sync Plugin

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

#### Settings Repository Plugin and Export Settings Feature

Warning:

The Settings Repository plugin is unbundled starting with version 2022.3 and will be no longer maintained.

Persistent components can be shared via the Settings Repository plugin and Export Settings feature, depending on the `roamingType` of the `@Storage` annotation.
See the [Defining the Storage Location](#defining-the-storage-location) for more details.
