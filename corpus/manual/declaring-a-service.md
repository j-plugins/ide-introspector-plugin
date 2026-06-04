---
id: declaring-a-service
title: Declaring a service
source: manual
kind: skill
description: Declares an application or project level service and retrieves it without leaking lifecycle. Use when you need shared state, a cache, or a coordinator scoped to the application or a project.
when_to_use: [application service, project service, getService, shared state, cache]
tags: [service, lifecycle, dependency-injection]
---
# Declaring a service

A service is a lazily instantiated singleton owned by a container. Choose an application
service for IDE wide state and a project service for per project state.

## Declare it

Register the implementation under `<applicationService>` or `<projectService>` in
`plugin.xml`, or annotate the class with `@Service`.

## Retrieve it

Resolve the service through the container rather than constructing it. Inspect declared
services and their injection sites with the `services.*` tools.
