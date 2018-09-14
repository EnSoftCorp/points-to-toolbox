package com.ensoftcorp.open.pointsto.codemap;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.pointsto.analysis.JavaPointsTo;
import com.ensoftcorp.open.pointsto.analysis.JimplePointsTo;
import com.ensoftcorp.open.pointsto.analysis.PointsTo;
import com.ensoftcorp.open.pointsto.log.Log;
import com.ensoftcorp.open.pointsto.preferences.PointsToPreferences;
import com.ensoftcorp.open.pointsto.utilities.GraphEnhancements;

/**
 * A hook for automatically running points-to analysis with the user's
 * preferences after Atlas creates a program graph
 * 
 * @author Ben Holland
 */
public class PointsToCodemapStage extends PrioritizedCodemapStage {

	public static final String IDENTIFIER = "com.ensoftcorp.open.pointsto";
	
	@Override
	public String getDisplayName() {
		return "Points-to Analysis";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{}; // no dependencies
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		try {
			if(PointsToPreferences.isPointsToAnalysisEnabled()){
				PointsTo pointsToAnalysis = null;
				if(PointsToPreferences.isJavaPointsToAnalysisModeEnabled()){
					if(!CommonQueries.isEmpty(Common.universe().nodes(XCSG.Language.Java))) {
						pointsToAnalysis = new JavaPointsTo();
						pointsToAnalysis.run();
					}
				} else if(PointsToPreferences.isJimplePointsToAnalysisModeEnabled()){
					if(!CommonQueries.isEmpty(Common.universe().nodes(XCSG.Language.Jimple))) {
						pointsToAnalysis = new JimplePointsTo();
						pointsToAnalysis.run();
					}
				}
				
				// make some graph enhancements
				if(pointsToAnalysis != null) {
					if(PointsToPreferences.isArrayComponentTrackingEnabled() && PointsToPreferences.isRewriteArrayComponentsEnabled()){
						long numArrayComponents = Common.universe().nodesTaggedWithAny(XCSG.ArrayComponents).eval().nodes().size();
						long numRewrittenArrayComponents = GraphEnhancements.rewriteArrayComponents(pointsToAnalysis);
						if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Rewrote " + numArrayComponents + " array components to " + numRewrittenArrayComponents + " array components.");
						
					}
					
					if(PointsToPreferences.isTagInferredDataflowsEnabled()){
						long numInferredDFEdges = GraphEnhancements.tagInferredDataFlowEdges(pointsToAnalysis);
						if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Applied " + numInferredDFEdges + " inferred data flow edge tags.");
					}
					
					if(PointsToPreferences.isTagRuntimeTypesEnabled()){
						long numInferredTypeOfEdges = GraphEnhancements.tagInferredTypeOfEdges(pointsToAnalysis);
						if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Applied " + numInferredTypeOfEdges + " inferred type of edge tags.");
					}
					
					if(PointsToPreferences.isDisposeResourcesEnabled()){
						// throw away references we don't need anymore
						if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Disposing temporary resources...");
						pointsToAnalysis.dispose();
					}
				}
			}
		} catch (Exception e) {
			Log.error("Error performing points-to analysis", e);
		}
	}

}