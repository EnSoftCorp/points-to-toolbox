---
layout: page
title: Tutorials
permalink: /tutorials/
---

If you haven't already, [install](/points-to-toolbox/install) the Points-to Toolbox plugin into Eclipse.

## Configuration
After installing the `Points-to Toolbox` navigate to `Eclipse` &gt; `Preferences` (or `Window` &gt; `Preferences`). Select `Points-to Toolbox` and check the `Enable Jimple Points-to Analysis` checkbox.

Note: Currently "Jimple Points-to Analysis" analyzes both Java and Java bytecode.

![Preferences](../images/preferences.png)

Navigate to `Atlas` &gt; `Manage Project Settings`, select the project to analyze and press `OK`. Navigate to `Atlas` &gt; `Re-Map Workspace` to regenerate the program graph. If points-to analysis is enabled it will be invoked automatically after the workspace has been mapped.

After the points-to analysis is complete, a Smart View can be opened to view the results.  Navigate to `Atlas` &gt; `Open Smart View`.  In the Smart View selection window select `Points-to Aliases` or `Points-to Array Component Aliases`.

In a program graph or source editor, select the reference to display points-to results for and the Smart View will automatically update with the aliases and instantiations for the given selection as shown in the image below.

![Points-To Aliases](../images/points-to-alias.png)

Optionally, the number of aliasing steps may be revealed using the controls on the left of the Smart View windows.

![Points-To Aliasing Steps](../images/points-to-alias-steps.png)

Selecting an array shows the instatiations and aliases of the array.

![Points-To Aliasing Steps](../images/points-to-array-aliases.png)

Selecting an array with the `Points-to Array Component Aliases` Smart View shows the corresponding array component(s) for the array and the instantiations of the objects that could flow into the array. Note that an array component symbolically represents all elements of an array.

![Points-To Aliasing Steps](../images/points-to-array-component-aliases.png)