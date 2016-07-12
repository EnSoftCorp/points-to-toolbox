package com.ensoftcorp.open.pointsto.map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.indexing.providers.ToolboxIndexingStage;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.open.pointsto.analysis.JimplePointsTo;
import com.ensoftcorp.open.pointsto.preferences.PointsToPreferences;
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
			// TODO: when a Java source specific implementation is available, run it instead of jimple version
			// the jimple version produces mostly correct results, but misses a few source only edge cases
			if(PointsToPreferences.isJimplePointsToAnalysisEnabled() || PointsToPreferences.isJavaPointsToAnalysisEnabled()){
				JimplePointsTo jimplePointsTo = new JimplePointsTo();
				jimplePointsTo.run();
				
				// make some graph enhancements
				Log.info("Serializing points-to results...");
				GraphEnhancements.serializeArrayMemoryModels(jimplePointsTo);
				GraphEnhancements.rewriteArrayComponents(jimplePointsTo);
				GraphEnhancements.tagInferredEdges(jimplePointsTo);
				GraphEnhancements.serializeAliases(jimplePointsTo);
				
				// throw away references we don't need anymore
				jimplePointsTo.dispose();
			}
		} catch (Exception e) {
			Log.error("Error performing points-to analysis", e);
		}
	}

}