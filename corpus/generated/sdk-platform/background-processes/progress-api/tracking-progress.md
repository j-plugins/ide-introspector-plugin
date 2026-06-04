---
id: sdk.background-processes.progress-api.tracking-progress
title: Background Processes: Tracking Progress
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, tracking, progress]
---
Displaying progress to the user is achieved with:

* `ProgressIndicator` - if available in the current context

* `ProgressManager` - if no indicator instance is available in the current context

To report progress with `ProgressIndicator`, use the following methods:

* `setText(String)` – sets the progress text displayed above the progress bar

* `setText2(String)` – sets the progress details text displayed under the progress bar

* `setFraction(double)` – sets the progress fraction: a number between 0.0 (nothing) and 1.0 (all) reflecting the ratio of work that has already been done. Only works for determinate indicator. The fraction should provide the user with an estimation of the time left. If this is impossible, consider making the progress indeterminate.

* `setIndeterminate(boolean)` – marks the progress indeterminate (for processes that can't estimate the amount of work to be done) or determinate (for processes that can display the fraction of the work done using `setFraction(double)`).

`ProgressManager` allows for reporting progress texts through `progress()`/`progress2()` methods, which are counterparts of `ProgressIndicator.setText()`/`setText2()`.
In addition, it exposes the `ProgressIndicator.getProgressIndicator()` method for getting an indicator instance associated with the current thread.

