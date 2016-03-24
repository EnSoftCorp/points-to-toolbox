---
layout: page
title: Analysis Details
permalink: /analysis/
---

The Points-to Toolbox is under active development.  This page shows the currently supported analysis capabilities and options.

## Java

| **Challenge**                  | **Capability**                                                                                                                                                                                                                                              |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Inter-procedural Analysis      | Results are computed inter-procedurally, resolving dynamic dispatches as potential runtime types are discovered.                                                                                                                                            |
| Flow Sensitivity               | Results are locally flow sensitive using Single Static Assignment (SSA) form, respecting the order of assignments within a block, however loops are not specifically addressed.                                                                             |
| Field Sensitivity              | Results are computed field sensitively in that a field `o.a` is distinct from `o.b`, but not with respect to object instance.                                                                                                                               |
| Object Sensitivity             | Results are 1-object sensitive in most cases, however object sensitivity is not specifically addressed in the current implementation. Object instances are differentiated if the allocation site is differentiated, which depends on context sensitivity.   |
| Context Sensitivity            | Results are computed context insensitively with no respect to calling context or object instance.                                                                                                                                                           |
| Path Sensitivity               | Results are not path sensitive. Paths through branches or loops are not modeled and are conservatively assumed to be possible.                                                                                                                              |
| Primitives                     | Primitives and String literals are currently ignored. Autoboxing is not addressed.                                                                                                                                                                                                                           |
| Arrays                         | Flows through single dimensional array components are considered.  An array component merges all array elements into a single node.                                                                                                               |
| Exceptions                     | Exceptional flows are currently not considered by the analysis.                                                                                                                                                                                             |
| Standard/Third Party Libraries | Unless full source is provided for the JDK or third party library APIs, the points-to analysis will dead end where the application uses the APIs.                                                                                                           |
| Reflection/Class Loaders       | No attempt to resolve uses of reflection or use of class loaders is made by the analysis.                                                                                                                                                                   |

### Notes
Currently support for Java is supported through the Java Bytecode (Jimple) points-to analysis implementation. A more complete Java implementation will be provided in the future. Implementations for arrays, exceptional flows, and primitives will be significantly different in Java source.

## Java Bytecode (Jimple)

| **Challenge**                  | **Capability**                                                                                                                                                                                                                                              |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Inter-procedural Analysis      | Results are computed inter-procedurally, resolving dynamic dispatches as potential runtime types are discovered.                                                                                                                                            |
| Flow Sensitivity               | Results are locally flow sensitive using Single Static Assignment (SSA) form, respecting the order of assignments within a block, however loops are not specifically addressed.                                                                             |
| Field Sensitivity              | Results are computed field sensitively in that a field `o.a` is distinct from `o.b`, but not with respect to object instance.                                                                                                                               |
| Object Sensitivity             | Results are 1-object sensitive in most cases, however object sensitivity is not specifically addressed in the current implementation. Object instances are differentiated if the allocation site is differentiated, which depends on context sensitivity.   |
| Context Sensitivity            | Results are computed context insensitively with no respect to calling context or object instance.                                                                                                                                                           |
| Path Sensitivity               | Results are not path sensitive. Paths through branches or loops are not modeled and are conservatively assumed to be possible.                                                                                                                              |
| Primitives                     | Primitives and String literals are currently ignored.                                                                                                                                                                                                       |
| Arrays                         | Flows through single and multi-dimensional array components are considered.  An array component merges all array elements into a single node.                                                                                                               |
| Exceptions                     | Exceptional flows are currently not considered by the analysis.                                                                                                                                                                                             |
| Standard/Third Party Libraries | Unless full source is provided for the JDK or third party library APIs, the points-to analysis will dead end where the application uses the APIs.                                                                                                           |
| Reflection/Class Loaders       | No attempt to resolve uses of reflection or use of class loaders is made by the analysis.                                                                                                                                                                   |

## Notes
In Java bytecode, multi-dimensional arrays are treated as arrays of arrays.

## C/C++
Currently not supported.