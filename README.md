# Anhinga Security Project

Tools for static analysis discovery of resource usage and limiting resource usage at runtime.


## Premise

Modern software highly depends upon reusing components that were written by other people to grant a broad range of added logic and abilities.  However, in many cases, these libraries provide capabilities unwanted or unexpected by the user of those libraries.

[Anhinga](https://en.wikipedia.org/wiki/Anhinga) provides tooling to support discovering that capability, and then to further limit the runtime capabilities.

The tooling uses these basic concepts:

* "security resource" - sensitive information or external system.  For example, execute another process, connect to another system across a network, or read a list of users.
* "resource access API" - software interface that allows programs to access one or more security resources.


## Current State

Anhinga Security is in the initial development phase.  It's more of a research project than a real usable framework.

Expected sub-projects include:

* Code analysis to trace which resource access APIs are used by a program or library.
* Define library resource access APIs mapping to security resources.
* Construct SELinux policy to restrict a program to just the required security resources.
* Construct a per-process network firewall for DNS and IP filtering restricted to expected resource access.


## Hurdles

The biggest hurdle in any such analysis is dynamic code invocation.  For example, the [Spring Framework](https://spring.io/projects/spring-framework) in Java constructs an executable product through dynamic assembly, rendering such analysis extremely difficult.
