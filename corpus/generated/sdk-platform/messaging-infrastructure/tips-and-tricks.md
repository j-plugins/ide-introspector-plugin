# Tips and Tricks

### Relief Listeners Management

Messaging infrastructure is very light-weight, so it's possible to reuse it at local sub-systems in order to relieve [Subscribers](https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern) construction.
Let's see what is necessary to do then:

1. Define business interface to work with.

2. Create shared message bus and topic that uses the interface above (shared here means that either subject or subscribers know about them).

A manual implementation would require:

1. Define listener interface (business interface).

2. Provide reference to the subject to all interested listeners.

3. Add listeners storage and listeners management methods (add/remove) to the subject.

4. Manually iterate all listeners and call target callback in all places where new event is fired.

### Avoid Shared Data Modification from Subscribers

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
