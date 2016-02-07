package com.ensoftcorp.open.pointsto.map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.indexing.providers.ToolboxIndexingStage;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.pointsto.analysis.JimplePointsTo;
import com.ensoftcorp.open.pointsto.common.Constants;
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
				GraphEnhancements.rewriteArrayComponents();
				GraphEnhancements.tagInferredEdges();
				
				// TODO: Pester Ensoft for a better workaround
				// SUPER NASTY HACK!!! Storing results in an isolated graph node
				// because class loaders are a pain in the butt...
//				PointsToResults pointsToResults = PointsToResults.getInstance();
//				pointsToResults.addressToInstantiation = jimplePointsTo.getAddressToInstantiation();
//				pointsToResults.addressToType = jimplePointsTo.getAddressToType();
//				pointsToResults.arrayMemoryModel = jimplePointsTo.getArrayMemoryModel();
//				pointsToResults.inferredDataFlowGraph = jimplePointsTo.getInferredDataFlowGraph();
				GraphElement results = Graph.U.createNode();
				results.tags().add("points-to-results");
				results.putAttr("addressToInstantiation", jimplePointsTo.getAddressToInstantiation());
				results.putAttr("addressToType", jimplePointsTo.getAddressToType());
				results.putAttr("arrayMemoryModel", jimplePointsTo.getArrayMemoryModel());
				// really this should just be inferred edges, but for now just
				// combining points-to set and connected array components graph
				// enhancement sets adding inferred edges since some may
				Q inferredDataFlowGraph = jimplePointsTo.getInferredDataFlowGraph().union(Common.universe().edgesTaggedWithAny(Constants.INFERRED));
				results.putAttr("inferredDataFlowGraph", Common.resolve(new NullProgressMonitor(), inferredDataFlowGraph));
			}
		} catch (Exception e) {
			Log.error("Error performing points-to analysis", e);
		}
	}

}