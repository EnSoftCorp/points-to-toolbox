---
layout: page
title: Install
permalink: /install/
---

Installing the Points-to Toolbox Eclipse plugin is easy.  It is recommended to install the plugin from the provided update site, but it is also possible to install from source.
        
### Installing from Update Site (recommended)
1. Start Eclipse, then select `Help` &gt; `Install New Software`.
2. Click `Add`, in the top-right corner.
3. In the `Add Repository` dialog that appears, enter &quot;Atlas Toolboxes&quot; for the `Name` and &quot;[https://ensoftcorp.github.io/toolbox-repository/](https://ensoftcorp.github.io/toolbox-repository/)&quot; for the `Location`.
4. In the `Available Software` dialog, select the checkbox next to "Points-to Toolbox" and click `Next` followed by `OK`.
5. In the next window, you'll see a list of the tools to be downloaded. Click `Next`.
6. Read and accept the license agreements, then click `Finish`. If you get a security warning saying that the authenticity or validity of the software can't be established, click `OK`.
7. When the installation completes, restart Eclipse.

## Installing from Source
If you want to install from source for bleeding edge changes, first grab a copy of the [source](https://github.com/EnSoftCorp/points-to-toolbox) repository. In the Eclipse workspace, import the `com.ensoftcorp.open.pointsto` Eclipse project located in the source repository.  Right click on the project and select `Export`.  Select `Plug-in Development` &gt; `Deployable plug-ins and fragments`.  Select the `Install into host. Repository:` radio box and click `Finish`.  Press `OK` for the notice about unsigned software.  Once Eclipse restarts the plugin will be installed and it is advisable to close or remove the `com.ensoftcorp.open.pointsto` project from the workspace.

## Changelog
Note that version numbers are based off [Atlas](http://www.ensoftcorp.com/atlas/download/) version numbers.

### 3.6.0
- Updates for Atlas dependencies

### 3.3.0
- Updates to dependencies

### 3.1.6
- Standardized log statements
- Updated dependencies

### 3.1.0
- Updates to dependencies

### 3.0.10
- Bumped version number for Atlas 3.0.10 release

### 3.0.8
- Atlas 3.x migrations
- Major rewrites to client analysis interfaces, all results are now serialized in Atlas graph as alias tags, array memory model tags, inferred data flow edges, and inferred runtime type of edges
- Added additional utilities for may and must alias queries
- Enabled a beta Java source points-to analysis implementation

### 2.7.3
- Bug fix for ghost edges in Smart Views
- Updated dependencies

### 2.7.1
- Initial Release