package com.ensoftcorp.open.pointsto.map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.indexing.providers.ToolboxIndexingStage;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.open.pointsto.analysis.JimplePointsTo;
import com.ensoftcorp.open.pointsto.common.PointsToResults;

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
//			JimplePointsTo jimplePointsTo = new JimplePointsTo();
//			jimplePointsTo.run();
//			PointsToResults.addressToInstantiation = jimplePointsTo.getAddressToInstantiation();
//			PointsToResults.addressToType = jimplePointsTo.getAddressToType();
//			PointsToResults.arrayMemoryModel = jimplePointsTo.getArrayMemoryModel();
//			PointsToResults.inferredDataFlowGraph = jimplePointsTo.getInferredDataFlowGraph();
		} catch (Exception e) {
			Log.error("Error performing points-to analysis", e);
		}
	}

}