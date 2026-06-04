# Sample Plugin

When launched, the sample plugin adds the SDK: Convert ternary operator to if statement item to the SDK intentions group in the `Settings | Editor | Intentions`.

### Running the Plugin

See [Code Samples](https://plugins.jetbrains.com/docs/intellij/code-samples.html) on how to set up and run the plugin.

### How does it work?

The plugin analyzes symbols under the cursor in your code opened in the editor.
If the cursor is positioned on the `?` conditional operator, IntelliJ IDEA proposes to replace this conditional (ternary) operator with the "if-then-else" statement:

![Convert ternary operator intention popup](images/ternary_operator_intention.png)
Invoking SDK: Convert ternary operator to if statement intention action will result in transforming expression to the form visible in the [preview](https://plugins.jetbrains.com/docs/intellij/code-intentions-preview.html) popup (code fragment on the right).

### Intention Description and examples

The intention description is available in the UI in two places:

* under `Settings | Editor | Intentions | SDK Intentions | SDK: Convert ternary operator to if statement`.

* near the selected intention action in the [Context Actions](https://www.jetbrains.com/help/idea/intention-actions.html#apply-intention-actions) popup in the editor when [Preview](https://plugins.jetbrains.com/docs/intellij/code-intentions-preview.html) cannot be shown.

The before/after examples are available in the UI
under `Settings | Editor | Intentions | SDK Intentions | SDK: Convert ternary operator to if statement`.

The plugin provides description and before/after examples files in the `resources/intentionDescriptions/ConditionalOperatorConverter` directory:

* `description.html` - provides the general information about the intention

* `before.java.template` - shows the code fragment that intention can change

* `after.java.template` - shows the code fragment after applying the intention

By default, the intention description directory name is the same as the intention class name.
It can be customized with the `<descriptionDirectoryName>` element in `<intentionAction>` in `plugin.xml`.

Warning:

If your intention class names are obfuscated in the [plugin distribution](https://plugins.jetbrains.com/docs/intellij/plugin-content.html), always declare the `<descriptionDirectoryName>` element.

"Before" and "after" filenames pattern is `before.$LANG_FILE_EXTENSION$.template` and `after.$LANG_FILE_EXTENSION$.template` respectively.
If before/after preview is not needed, specify `<skipBeforeAfter>true</skipBeforeAfter>` in the `<intentionAction>` in `plugin.xml`.

Warning:

If a plugin project is multi-module, and it combines resources into a single JAR, make sure that all intention description files have unique names or paths.
Otherwise, only the last packed description file will exist in the distribution package.

Tip:

See the [Bundled Translations](https://plugins.jetbrains.com/docs/intellij/providing-translations.html#bundled-translations) section for information about how to provide intention description translations in plugins.

### Testing the Plugin

Note:

Note that running the test requires setting system property `idea.home.path` in the `test` task configuration of the Gradle build script.

The sample plugin contains the `ConditionalOperatorConverterTest` Java class and the test data in the `test/testData/` directory.
To perform the plugin test, run the `ConditionalOperatorConverterTest.testIntention()` method.
