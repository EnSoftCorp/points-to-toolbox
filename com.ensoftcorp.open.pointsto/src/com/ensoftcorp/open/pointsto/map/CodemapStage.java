package com.ensoftcorp.open.pointsto.map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.indexing.providers.ToolboxIndexingStage;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.open.pointsto.analysis.JimplePointsTo;
import com.ensoftcorp.open.pointsto.ui.PointsToPreferences;
import com.ensoftcorp.open.pointsto.utilities.GraphEnhancements;

/**
 * A hook for automatically running points-to analysis with the user's
 * preferences after Atlas creates a program graph
 * 
 * @author Ben Holland
 */
public class CodemapStage implements ToolboxIndexingStage {

	@Override
	public String displayName() {
		return "Points-to Analysis";
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		try {
			if(PointsToPreferences.isJimplePointsToAnalysisEnabled()){
				JimplePointsTo jimplePointsTo = new JimplePointsTo();
				jimplePointsTo.run();
				
				// make some graph enhancements
				GraphEnhancements.rewriteArrayComponents(jimplePointsTo);
				GraphEnhancements.tagInferredEdges(jimplePointsTo);
				GraphEnhancements.serializeAliases(jimplePointsTo);
			}
		} catch (Exception e) {
			Log.error("Error performing points-to analysis", e);
		}
	}

}