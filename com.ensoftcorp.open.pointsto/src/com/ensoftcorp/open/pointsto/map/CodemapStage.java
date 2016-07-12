package com.ensoftcorp.open.pointsto.map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.indexing.providers.ToolboxIndexingStage;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
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
				if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Enhancing graph with points-to results...");
				
				long numMemoryModels = GraphEnhancements.serializeArrayMemoryModels(jimplePointsTo);
				if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Attributed " + numMemoryModels + " array memory models.");
				
				long numArrayComponents = Common.universe().nodesTaggedWithAny(XCSG.ArrayComponents).eval().nodes().size();
				long numRewrittenArrayComponents = GraphEnhancements.rewriteArrayComponents(jimplePointsTo);
				if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Rewrote " + numArrayComponents + " array components to " + numRewrittenArrayComponents + " array components.");
				
				long numInferredDFEdges = GraphEnhancements.tagInferredEdges(jimplePointsTo);
				if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Inferred " + numInferredDFEdges + " data flow edges.");
				
				long numTaggedAliases = GraphEnhancements.serializeAliases(jimplePointsTo);
				if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Applied " + numTaggedAliases + " aliasing tags.");
				
				// throw away references we don't need anymore
				if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Disposing temporary resources...");
				jimplePointsTo.dispose();
			}
		} catch (Exception e) {
			Log.error("Error performing points-to analysis", e);
		}
	}

}