---
layout: default
---

## Overview
The Points-to Toolbox project is an Atlas native points-to analysis implementation  and utilities for using the points-to analysis results for other client analyses. This plugin implements an Andersen-style points-to analysis for Java and Java bytecode (leveraging Atlas for Jimple).  Additional details about the points-to analysis capabilities can be found on the [analysis details](/points-to-toolbox/analysis) page.

## Features
- Automatic analysis following Atlas program graph generation
- User configurable analysis preferences
- Atlas Smart Views for viewing points-to results on-demand
- Program Graph Enhancements: 
	- Rewrites data flows through array components
	- Refines Class Hierarchy Analysis (CHA) based call graphs and interprodecural data flow invocations with inference tags
- Client analysis access to: 
	- *Points-To Aliases* 
	- *Points-To Instantiations*
	- *Points-To Types*

## Getting Started
Ready to get started?

1. First [install](/points-to-toolbox/install) the Points-to Toolbox plugin
2. Then check out the provided [tutorials](/points-to-toolbox/tutorials) to jump start your analysis

## Source Code
Need additional resources?  Checkout the [Javadocs](/points-to-toolbox/javadoc/index.html) or grab a copy of the [source](https://github.com/benjholla/points-to-toolbox).