---
id: sdk.background-processes.progress-api.cancellation.requesting-cancellation
title: Background Processes: Requesting Cancellation
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, requesting, cancellation]
---
The process can be marked as canceled by calling `ProgressIndicator.cancel()`.
This method is called by the infrastructure that started the process, for example, when the mentioned cancel button is clicked, or by code responsible for invoking code completion.

The `cancel()` method marks the process as canceled, and it is up to the running operation to actually cancel itself.
See the section below for handling cancellation.

