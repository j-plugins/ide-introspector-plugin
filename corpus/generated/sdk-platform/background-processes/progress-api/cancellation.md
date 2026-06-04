---
id: sdk.background-processes.progress-api.cancellation
title: Background Processes: Cancellation
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, cancellation]
---
The most important feature of Progress API is the ability to cancel a process if the result of the computation gets irrelevant.
Cancellation can be performed either by a user (pressing a cancel button) or from code when the current operation becomes obsolete due to some changes in the project.
Examples:

* Cancelling the search for symbol usages (cancellation by user): 1. The user triggers the Find Usages action in a large project. 2. Results are being calculated and gradually presented to the user. 3. The user sees the place they were interested in or realizes that they don't need these results anymore. 4. The user clicks the cancel button in the status bar, and the operation is canceled.

* Code completion (cancellation from code): 1. The user types a letter in the editor. 2. Computation of results for code completion is started. 3. User types another letter. 4. The computation started in 2. is now outdated and is canceled to start computation for the new input.

Being prepared for cancellation requests in plugin code is crucial for saving CPU resources and responsiveness of the IDE.

#### Requesting Cancellation (background-processes/progress-api/cancellation/requesting-cancellation.md)
#### Handling Cancellation (background-processes/progress-api/cancellation/handling-cancellation.md)
