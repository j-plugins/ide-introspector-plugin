---
id: sdk.messaging-infrastructure.tips-and-tricks.avoid-shared-data-modification-from-subscribers
title: Messaging Infrastructure: Avoid Shared Data Modification from Subscribers
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, avoid, shared, data, modification, from]
---
We had a problem in a situation when two subscribers tried to modify the same document
([IDEA-71701](https://youtrack.jetbrains.com/issue/IDEA-71701)).

The thing is that every document change is performed by the following scenario:

1. before change event is sent to all document listeners and some of them publish new messages during that;

2. actual change is performed;

3. after change event is sent to all document listeners;

We had the following then:

1. message1 is sent to the topic with two subscribers;

2. message1 is queued for both subscribers;

3. message1 delivery starts;

4. subscriber1 receives message1;

5. subscriber1 issues document modification request at particular range (e.g. document.delete(startOffset, endOffset));

6. before change notification is sent to the document listeners;

7. message2 is sent by one of the standard document listeners to another topic within the same message bus during before change processing;

8. the bus tries to deliver all pending messages before queuing message2;

9. subscriber2 receives message1 and also modifies a document;

10. the call stack is unwound and actual change phase of document modification operation requested by subscriber1 begins;

The problem is that document range used by subscriber1 for initial modification request is invalid if subscriber2 has changed document's range before it.

