package com.ensoftcorp.open.pointsto.map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.indexing.providers.ToolboxIndexingStage;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.open.pointsto.analysis.JimplePointsTo;

public class CodemapStage implements ToolboxIndexingStage {

	@Override
	public String displayName() {
		return "Points-to Analysis";
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		try {
			JimplePointsTo jimplePointsTo = new JimplePointsTo();
			jimplePointsTo.run();
		} catch (Exception e) {
			Log.error("Error performing points-to analysis", e);
		}

	}

}